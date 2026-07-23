package com.hrms.business.approval.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.business.approval.config.ApprovalNodeDef;
import com.hrms.business.approval.dto.OperateResultVO;
import com.hrms.business.approval.entity.ApprovalInstanceEntity;
import com.hrms.business.approval.entity.ApprovalTaskEntity;
import com.hrms.business.approval.enums.ApprovalStatusEnum;
import com.hrms.business.approval.enums.ApproveResultEnum;
import com.hrms.business.approval.enums.TaskStatusEnum;
import com.hrms.business.approval.mapper.ApprovalInstanceMapper;
import com.hrms.business.approval.mapper.ApprovalTaskMapper;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.approval.service.ApprovalTemplateLoader;
import com.hrms.business.approval.service.ApproverResolver;
import com.hrms.business.approval.service.event.ApprovalCompletedEvent;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 审批流程引擎实现
 * <p>
 * 使用乐观锁（UPDATE ... WHERE id=? AND task_status=0）保证并发安全。
 * 审批完成时通过 Spring Event 发布 ApprovalCompletedEvent，供其他模块回调。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalEngineImpl implements ApprovalEngine {

    private final ApprovalInstanceMapper instanceMapper;
    private final ApprovalTaskMapper taskMapper;
    private final ApprovalTemplateLoader templateLoader;
    private final ApproverResolver approverResolver;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发起审批流程
     * <p>
     * 流程：加载审批模板 → 生成审批单号 → 创建审批实例 → 创建第一个待办任务。
     * 使用 Spring 声明式事务，任何异常触发回滚。
     * </p>
     *
     * @param approvalType       审批业务类型（如 ENTRY、LEAVE 等）
     * @param bizId              业务主键 ID
     * @param formData           表单数据 JSON 快照
     * @param applicantUserId    申请人用户 ID
     * @param applicantDeptId    申请人部门 ID
     * @param applicantEmployeeId 申请人员工 ID（可选）
     * @return 审批实例 ID
     * @throws GlobalException 模板为空或无可用审批人时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startApproval(String approvalType, Long bizId, String formData,
                              Long applicantUserId, Long applicantDeptId, Long applicantEmployeeId) {
        // 1. 加载审批模板
        List<ApprovalNodeDef> nodes = templateLoader.loadTemplate(approvalType);
        if (nodes.isEmpty()) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "审批模板为空：" + approvalType);
        }

        // 2. 生成审批单号
        String approvalNo = generateApprovalNo();

        // 3. 创建审批实例
        ApprovalInstanceEntity instance = new ApprovalInstanceEntity();
        instance.setApprovalNo(approvalNo);
        instance.setApprovalType(approvalType);
        instance.setBizId(bizId);
        instance.setTitle(buildTitle(approvalType, bizId));
        instance.setApplicantUserId(applicantUserId);
        instance.setApplicantEmployeeId(applicantEmployeeId);
        instance.setApprovalStatus(ApprovalStatusEnum.PENDING.getCode());
        instance.setFormJson(formData);
        instance.setApplyTime(LocalDateTime.now());
        instanceMapper.insert(instance);

        // 4. 逐个解析审批人，创建第一个待办任务（后续节点由 advanceToNext 创建）
        ApprovalNodeDef firstNode = createNextTask(instance, nodes, 0, applicantDeptId);
        if (firstNode == null) {
            // 所有节点都无可用的审批人
            instance.setApprovalStatus(ApprovalStatusEnum.WITHDRAWN.getCode());
            instance.setFinishTime(LocalDateTime.now());
            instanceMapper.updateById(instance);
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "无法解析任何审批节点");
        }
        instance.setCurrentNodeName(firstNode.getNodeName());
        instanceMapper.updateById(instance);

        log.info("审批发起成功: approvalNo={}, type={}, instanceId={}", approvalNo, approvalType, instance.getId());
        return instance.getId();
    }

    /**
     * 处理审批操作
     * <p>
     * 支持 approve（通过）、reject（驳回）、transfer（转交）三种操作。
     * 使用乐观锁（UPDATE ... WHERE task_status = 0）保证并发安全，
     * 更新行数为 0 时表示任务已被他人处理。
     * </p>
     *
     * @param taskId      审批任务 ID
     * @param action      操作类型（approve / reject / transfer）
     * @param comment     审批意见
     * @param targetUserId 转交目标用户 ID（仅 transfer 时有效）
     * @return 操作结果 VO（含任务状态、实例状态、下一节点信息）
     * @throws GlobalException 任务不存在、已被处理或不支持的操作类型时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperateResultVO processAction(Long taskId, String action, String comment, Long targetUserId) {
        // 1. 查询当前任务
        ApprovalTaskEntity task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "审批任务不存在");
        }
        if (task.getTaskStatus() != TaskStatusEnum.PENDING.getCode()) {
            throw new GlobalException(ErrorCode.CONFLICT, "审批任务已被处理");
        }

        // 2. 乐观锁：根据操作类型设置不同的字段
        int rows;
        if ("transfer".equals(action)) {
            rows = taskMapper.update(null, Wrappers.lambdaUpdate(ApprovalTaskEntity.class)
                    .set(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.TRANSFERRED.getCode())
                    .set(ApprovalTaskEntity::getApproveResult, ApproveResultEnum.TRANSFER.getCode())
                    .set(ApprovalTaskEntity::getApproveTime, LocalDateTime.now())
                    .set(ApprovalTaskEntity::getApproveComment, comment)
                    .eq(ApprovalTaskEntity::getId, taskId)
                    .eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
            );
        } else {
            int resultCode = "approve".equals(action)
                    ? ApproveResultEnum.APPROVE.getCode()
                    : ApproveResultEnum.REJECT.getCode();
            rows = taskMapper.update(null, Wrappers.lambdaUpdate(ApprovalTaskEntity.class)
                    .set(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PROCESSED.getCode())
                    .set(ApprovalTaskEntity::getApproveResult, resultCode)
                    .set(ApprovalTaskEntity::getApproveTime, LocalDateTime.now())
                    .set(ApprovalTaskEntity::getApproveComment, comment)
                    .eq(ApprovalTaskEntity::getId, taskId)
                    .eq(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.PENDING.getCode())
            );
        }
        if (rows == 0) {
            throw new GlobalException(ErrorCode.CONFLICT, "审批任务已被他人处理");
        }

        // 3. 根据操作类型处理后续流转
        switch (action) {
            case "approve":
                handleApprove(task);
                break;
            case "reject":
                handleReject(task);
                break;
            case "transfer":
                handleTransfer(task, targetUserId);
                break;
            default:
                throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "不支持的操作类型：" + action);
        }

        // 4. 重新查询最新状态，构建操作结果
        task = taskMapper.selectById(taskId);
        ApprovalInstanceEntity instance = instanceMapper.selectById(task.getInstanceId());
        return buildOperateResult(task, instance);
    }

    /**
     * 构建审批操作结果
     */
    private OperateResultVO buildOperateResult(ApprovalTaskEntity task, ApprovalInstanceEntity instance) {
        OperateResultVO result = new OperateResultVO();
        result.setSuccess(true);

        // 任务状态
        result.setTaskStatus(task.getTaskStatus() == TaskStatusEnum.PROCESSED.getCode() ? "PROCESSED" : "TRANSFERRED");

        // 实例状态
        ApprovalStatusEnum statusEnum = ApprovalStatusEnum.fromCode(instance.getApprovalStatus());
        result.setInstanceStatus(statusEnum != null ? statusEnum.name() : "UNKNOWN");

        // 下一节点：如果实例还在审批中，currentNodeName 为下一节点；否则为 null（已结束）
        if (ApprovalStatusEnum.PENDING.getCode() == instance.getApprovalStatus()) {
            result.setNextNodeName(instance.getCurrentNodeName());
        } else {
            result.setNextNodeName(null);
        }

        return result;
    }

    // ========== 内部方法 ==========

    /**
     * 处理通过：流转到下一节点，或完成审批
     */
    private void handleApprove(ApprovalTaskEntity task) {
        ApprovalInstanceEntity instance = instanceMapper.selectById(task.getInstanceId());

        // 获取模板，定位下一节点
        List<ApprovalNodeDef> nodes = templateLoader.loadTemplate(instance.getApprovalType());
        int nextIndex = task.getSortNo(); // sortNo 从1开始，nextIndex 是下一个节点的模板下标

        // 尝试创建下一个任务（跳过无可审批人的可选节点）
        ApprovalNodeDef nextNode = createNextTask(instance, nodes, nextIndex, null);

        if (nextNode != null) {
            // 有下一节点
            instance.setCurrentNodeName(nextNode.getNodeName());
            instanceMapper.updateById(instance);
            log.info("审批流转下一节点: instanceId={}, nextNode={}", instance.getId(), nextNode.getNodeName());
        } else {
            // 所有节点已完成，审批通过
            completeInstance(instance, ApprovalStatusEnum.APPROVED);
        }
    }

    /**
     * 处理驳回：审批实例终止
     */
    private void handleReject(ApprovalTaskEntity task) {
        ApprovalInstanceEntity instance = instanceMapper.selectById(task.getInstanceId());
        completeInstance(instance, ApprovalStatusEnum.REJECTED);
    }

    /**
     * 处理转交：创建新任务给目标用户
     */
    private void handleTransfer(ApprovalTaskEntity task, Long targetUserId) {
        if (targetUserId == null) {
            throw new GlobalException(ErrorCode.PARAM_VALIDATION_FAILED, "转交目标用户不能为空");
        }

        // 将原任务标记为已转交
        taskMapper.update(null, Wrappers.lambdaUpdate(ApprovalTaskEntity.class)
                .set(ApprovalTaskEntity::getTaskStatus, TaskStatusEnum.TRANSFERRED.getCode())
                .set(ApprovalTaskEntity::getApproveResult, ApproveResultEnum.TRANSFER.getCode())
                .eq(ApprovalTaskEntity::getId, task.getId())
        );

        // 创建新任务给目标用户，同一节点
        ApprovalTaskEntity newTask = new ApprovalTaskEntity();
        newTask.setInstanceId(task.getInstanceId());
        newTask.setNodeCode(task.getNodeCode());
        newTask.setNodeName(task.getNodeName());
        newTask.setApproverUserId(targetUserId);
        newTask.setOriginalApproverId(task.getApproverUserId());
        newTask.setDelegateFlag(0);
        newTask.setTaskStatus(TaskStatusEnum.PENDING.getCode());
        newTask.setSortNo(task.getSortNo());
        newTask.setReceiveTime(LocalDateTime.now());
        taskMapper.insert(newTask);

        log.info("审批转交: taskId={}, from={}, to={}", task.getId(), task.getApproverUserId(), targetUserId);
    }

    /**
     * 创建下一个待办任务
     *
     * @param instance  审批实例
     * @param nodes     模板节点列表
     * @param startIdx  起始节点下标
     * @param deptId    申请人部门ID（可选）
     * @return 创建的任务对应节点，无可用节点时返回 null
     */
    private ApprovalNodeDef createNextTask(ApprovalInstanceEntity instance, List<ApprovalNodeDef> nodes,
                                           int startIdx, Long deptId) {
        for (int i = startIdx; i < nodes.size(); i++) {
            ApprovalNodeDef node = nodes.get(i);

            // 解析审批人
            Long approverUserId = approverResolver.resolveApprover(node.getApproverType(), deptId, instance.getBizId());

            // 可选节点且无审批人 → 跳过
            if (approverUserId == null && node.isOptional()) {
                log.info("跳过可选节点: node={}, 无审批人", node.getNodeName());
                continue;
            }

            // 非可选节点且无审批人 → 不可跳过，抛异常
            if (approverUserId == null) {
                if ("PARENT_DEPT_HEAD".equals(node.getApproverType()) || "DEPT_HEAD".equals(node.getApproverType())) {
                    throw new GlobalException(ErrorCode.NO_SUPERIOR_APPROVER, "无上级审批人");
                }
                throw new GlobalException(ErrorCode.BUSINESS_ERROR,
                        "无法解析审批人：" + node.getNodeName());
            }

            // 检查委托关系
            Long delegateUserId = approverResolver.checkDelegation(approverUserId);
            boolean isDelegate = delegateUserId != null;

            // 自审批拦截：审批人与申请人相同时，视为无上级审批人
            if (!isDelegate && instance.getApplicantUserId() != null
                    && approverUserId.equals(instance.getApplicantUserId())) {
                log.warn("自审批拦截: node={}, 审批人={} 与申请人相同",
                        node.getNodeName(), approverUserId);
                throw new GlobalException(ErrorCode.NO_SUPERIOR_APPROVER, "提交失败：没有上级审批人");
            }

            // 创建任务
            ApprovalTaskEntity task = new ApprovalTaskEntity();
            task.setInstanceId(instance.getId());
            task.setNodeCode(node.getNodeCode());
            task.setNodeName(node.getNodeName());
            task.setApproverUserId(isDelegate ? delegateUserId : approverUserId);
            task.setOriginalApproverId(isDelegate ? approverUserId : null);
            task.setDelegateFlag(isDelegate ? 1 : 0);
            task.setTaskStatus(TaskStatusEnum.PENDING.getCode());
            task.setSortNo(i + 1);
            task.setReceiveTime(LocalDateTime.now());
            taskMapper.insert(task);

            return node;
        }

        return null; // 所有节点都跳过了
    }

    /**
     * 完成审批实例
     */
    private void completeInstance(ApprovalInstanceEntity instance, ApprovalStatusEnum finalStatus) {
        instance.setApprovalStatus(finalStatus.getCode());
        instance.setFinishTime(LocalDateTime.now());
        instanceMapper.updateById(instance);

        // 发布审批完成事件（供其他模块回调）
        eventPublisher.publishEvent(new ApprovalCompletedEvent(
                instance.getId(),
                instance.getApprovalType(),
                instance.getBizId(),
                finalStatus.getCode()
        ));

        log.info("审批完成: instanceId={}, status={}", instance.getId(), finalStatus.getDesc());
    }

    /**
     * 生成审批单号：APR + yyyyMMddHHmmss
     */
    private String generateApprovalNo() {
        return "APR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 构建审批标题
     */
    private String buildTitle(String approvalType, Long bizId) {
        // TODO: 优化标题生成，可能需要业务名称
        return approvalType + " - " + bizId;
    }

}

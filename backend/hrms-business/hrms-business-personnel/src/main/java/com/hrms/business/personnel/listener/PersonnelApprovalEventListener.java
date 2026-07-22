package com.hrms.business.personnel.listener;

import com.hrms.business.approval.enums.ApprovalStatusEnum;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.service.event.ApprovalCompletedEvent;
import com.hrms.business.employee.dto.EmployeeApprovalSyncUpdateDTO;
import com.hrms.business.employee.enums.EmploymentStatusEnum;
import com.hrms.business.employee.service.EmployeeService;
import com.hrms.business.personnel.common.cache.PersonnelCacheKeys;
import com.hrms.business.personnel.common.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.common.enums.RegularEvaluateResultEnum;
import com.hrms.business.personnel.entity.EntryApplicationEntity;
import com.hrms.business.personnel.entity.LeaveApplicationEntity;
import com.hrms.business.personnel.entity.RegularApplicationEntity;
import com.hrms.business.personnel.entity.TransferApplicationEntity;
import com.hrms.business.personnel.mapper.EntryApplicationMapper;
import com.hrms.business.personnel.mapper.LeaveApplicationMapper;
import com.hrms.business.personnel.mapper.RegularApplicationMapper;
import com.hrms.business.personnel.mapper.TransferApplicationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

/**
 * 人员审批完成事件监听器
 * <p>
 * 监听入职、转正、调岗、离职审批完成事件，并同步更新申请单状态与员工档案信息。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersonnelApprovalEventListener {

    private final EntryApplicationMapper entryApplicationMapper;
    private final RegularApplicationMapper regularApplicationMapper;
    private final TransferApplicationMapper transferApplicationMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final EmployeeService employeeService;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    /**
     * 处理审批完成事件。
     *
     * @param event 审批完成事件
     */
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handleApprovalCompleted(ApprovalCompletedEvent event) {
        log.info("收到人员审批完成事件: instanceId={}, type={}, bizId={}, status={}",
                event.getInstanceId(), event.getApprovalType(), event.getBizId(), event.getInstanceStatus());

        ApprovalTypeEnum approvalType = ApprovalTypeEnum.fromCode(event.getApprovalType());
        if (approvalType == null) {
            log.debug("忽略非人员模块审批事件: {}", event.getApprovalType());
            return;
        }

        switch (approvalType) {
            case ENTRY -> handleEntryCompleted(event);
            case REGULAR -> handleRegularCompleted(event);
            case TRANSFER -> handleTransferCompleted(event);
            case LEAVE -> handleLeaveCompleted(event);
            default -> log.debug("忽略非人员模块审批事件: {}", event.getApprovalType());
        }
    }

    /**
     * 处理入职审批完成联动。
     *
     * @param event 审批完成事件
     */
    private void handleEntryCompleted(ApprovalCompletedEvent event) {
        EntryApplicationEntity entity = entryApplicationMapper.selectById(event.getBizId());
        if (entity == null) {
            log.warn("入职申请不存在，忽略联动: entryId={}", event.getBizId());
            return;
        }

        // 入职审批完成后仅同步申请单状态，正式入职仍需手动执行“确认入职”。
        entity.setApprovalStatus(resolveApplicationStatus(event.getInstanceStatus()));
        entryApplicationMapper.updateById(entity);
        evictEntryCaches(entity.getId());
    }

    /**
     * 处理转正审批完成联动。
     *
     * @param event 审批完成事件
     */
    private void handleRegularCompleted(ApprovalCompletedEvent event) {
        RegularApplicationEntity entity = regularApplicationMapper.selectById(event.getBizId());
        if (entity == null) {
            log.warn("转正申请不存在，忽略联动: regularId={}", event.getBizId());
            return;
        }

        // 先同步申请单审批状态，保证列表页状态与审批结果一致。
        entity.setApprovalStatus(resolveApplicationStatus(event.getInstanceStatus()));

        // 审批通过且评估结果为转正时，立即将员工状态由试用改为正式。
        if (ApprovalStatusEnum.APPROVED.getCode() == event.getInstanceStatus()
                && RegularEvaluateResultEnum.PASS.getCode() == entity.getEvaluateResult()) {
            entity.setRegularDate(LocalDate.now());
            employeeService.syncEmployeeForApproval(entity.getEmployeeId(),
                    EmployeeApprovalSyncUpdateDTO.builder()
                            .employmentStatus(EmploymentStatusEnum.FORMAL.getCode())
                            .build());
        }

        regularApplicationMapper.updateById(entity);
        evictCacheByPattern(PersonnelCacheKeys.regularPagePattern());
    }

    /**
     * 处理调岗审批完成联动。
     *
     * @param event 审批完成事件
     */
    private void handleTransferCompleted(ApprovalCompletedEvent event) {
        TransferApplicationEntity entity = transferApplicationMapper.selectById(event.getBizId());
        if (entity == null) {
            log.warn("调岗申请不存在，忽略联动: transferId={}", event.getBizId());
            return;
        }

        // 先同步申请单审批状态，保证调岗记录与审批引擎状态一致。
        entity.setApprovalStatus(resolveApplicationStatus(event.getInstanceStatus()));

        // 调岗审批通过后，同步员工的部门、职位、职级与汇报人。
        if (ApprovalStatusEnum.APPROVED.getCode() == event.getInstanceStatus()) {
            employeeService.syncEmployeeForApproval(entity.getEmployeeId(),
                    EmployeeApprovalSyncUpdateDTO.builder()
                            .deptId(entity.getToDeptId())
                            .postId(entity.getToPostId())
                            .jobLevel(entity.getToJobLevel())
                            .leaderId(entity.getToLeaderId())
                            .build());
        }

        transferApplicationMapper.updateById(entity);
        evictCacheByPattern(PersonnelCacheKeys.transferPagePattern());
    }

    /**
     * 处理离职审批完成联动。
     *
     * @param event 审批完成事件
     */
    private void handleLeaveCompleted(ApprovalCompletedEvent event) {
        LeaveApplicationEntity entity = leaveApplicationMapper.selectById(event.getBizId());
        if (entity == null) {
            log.warn("离职申请不存在，忽略联动: leaveId={}", event.getBizId());
            return;
        }

        // 先同步申请单审批状态，保证离职记录与审批结果一致。
        entity.setApprovalStatus(resolveApplicationStatus(event.getInstanceStatus()));

        // 离职审批通过后直接标记为已离职，不经过待离职状态。
        if (ApprovalStatusEnum.APPROVED.getCode() == event.getInstanceStatus()) {
            employeeService.syncEmployeeForApproval(entity.getEmployeeId(),
                    EmployeeApprovalSyncUpdateDTO.builder()
                            .employmentStatus(EmploymentStatusEnum.LEFT.getCode())
                            .build());
        }

        leaveApplicationMapper.updateById(entity);
        evictCacheByPattern(PersonnelCacheKeys.leavePagePattern());
    }

    /**
     * 将审批实例状态映射为人员申请状态。
     *
     * @param instanceStatus 审批实例状态
     * @return 人员申请状态
     */
    private Integer resolveApplicationStatus(Integer instanceStatus) {
        if (ApprovalStatusEnum.APPROVED.getCode() == instanceStatus) {
            return ApplicationStatusEnum.APPROVED.getCode();
        }
        if (ApprovalStatusEnum.REJECTED.getCode() == instanceStatus) {
            return ApplicationStatusEnum.REJECTED.getCode();
        }
        return ApplicationStatusEnum.APPROVING.getCode();
    }

    /**
     * 清理入职申请相关缓存。
     *
     * @param entryApplicationId 入职申请ID
     */
    private void evictEntryCaches(Long entryApplicationId) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.delete(PersonnelCacheKeys.entryDetail(entryApplicationId));
        evictCacheByPattern(PersonnelCacheKeys.entryStatsPattern());
    }

    /**
     * 按模式清理缓存，保证审批完成后列表展示立即刷新。
     *
     * @param keyPattern Redis Key 模式
     */
    private void evictCacheByPattern(String keyPattern) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        Set<String> keys = redisTemplate.keys(keyPattern);
        if (keys == null || keys.isEmpty()) {
            return;
        }
        redisTemplate.delete(keys);
    }
}

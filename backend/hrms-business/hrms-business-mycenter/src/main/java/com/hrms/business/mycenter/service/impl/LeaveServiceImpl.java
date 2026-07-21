package com.hrms.business.mycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.business.approval.service.ApprovalService;
import com.hrms.business.mycenter.dto.LeaveBalanceVO;
import com.hrms.business.mycenter.dto.LeaveRequestDTO;
import com.hrms.business.mycenter.dto.LeaveVO;
import com.hrms.business.mycenter.entity.LeaveBalanceEntity;
import com.hrms.business.mycenter.entity.LeaveRequestEntity;
import com.hrms.business.mycenter.mapper.MyCenterLeaveBalanceMapper;
import com.hrms.business.mycenter.mapper.MyCenterLeaveRequestMapper;
import com.hrms.business.mycenter.service.LeaveService;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 请假服务实现
 * <p>
 * 提供请假申请、取消、记录查询和余额查询功能。
 * 请假申请通过 {@link com.hrms.business.approval.service.ApprovalService} 发起 LEAVE_REQUEST 类型审批。
 * 余额数据从 hr_leave_balance 表按月统计。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final MyCenterLeaveRequestMapper leaveRequestMapper;
    private final MyCenterLeaveBalanceMapper leaveBalanceMapper;
    private final ApprovalService approvalService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createLeave(Long employeeId, LeaveRequestDTO request) {
        // 1. 创建请假记录
        LeaveRequestEntity entity = new LeaveRequestEntity();
        entity.setEmployeeId(employeeId);
        entity.setLeaveType(request.getLeaveType());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setTotalDays(request.getTotalDays());
        entity.setTotalHours(request.getTotalHours());
        entity.setLeaveReason(request.getLeaveReason());
        entity.setAttachmentUrl(request.getAttachmentUrl());
        entity.setApprovalStatus(0); // 草稿
        leaveRequestMapper.insert(entity);

        // 2. 构建表单快照
        String formDataJson = buildLeaveFormData(request);

        // 3. 发起审批
        try {
            Long instanceId = approvalService.startApproval("LEAVE_REQUEST", entity.getId(), formDataJson);

            // 4. 回填审批实例ID，更新状态为"审批中"
            entity.setApprovalInstanceId(instanceId);
            entity.setApprovalStatus(1);
            leaveRequestMapper.updateById(entity);

            log.info("请假提交并发起审批成功: leaveId={}, instanceId={}", entity.getId(), instanceId);
        } catch (Exception e) {
            log.error("请假发起审批失败: leaveId={}, error={}", entity.getId(), e.getMessage(), e);
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "发起审批失败：" + e.getMessage());
        }
    }

    /**
     * 构建请假表单快照 JSON
     */
    private String buildLeaveFormData(LeaveRequestDTO request) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"leaveType\":\"").append(escapeJson(request.getLeaveType())).append("\",");
        json.append("\"startTime\":\"").append(request.getStartTime()).append("\",");
        json.append("\"endTime\":\"").append(request.getEndTime()).append("\",");
        json.append("\"totalDays\":").append(request.getTotalDays()).append(",");
        json.append("\"totalHours\":").append(request.getTotalHours() != null ? request.getTotalHours() : "null").append(",");
        json.append("\"leaveReason\":\"").append(escapeJson(request.getLeaveReason())).append("\"");
        json.append("}");
        return json.toString();
    }

    /**
     * 简单的 JSON 字符串转义
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    /**
     * 查询请假记录列表
     *
     * @param employeeId 员工 ID
     * @return 请假记录 VO 列表
     */
    @Override
    public List<LeaveVO> listLeaves(Long employeeId) {
        List<LeaveRequestEntity> list = leaveRequestMapper.selectList(
                new LambdaQueryWrapper<LeaveRequestEntity>()
                        .eq(LeaveRequestEntity::getEmployeeId, employeeId)
                        .orderByDesc(LeaveRequestEntity::getCreateTime)
        );

        return list.stream().map(this::toLeaveVO).collect(Collectors.toList());
    }

    /**
     * 取消请假
     * <p>
     * 仅允许本人取消自己的请假记录，仅草稿（状态 0）或审批中（状态 1）的请假可取消。
     * 取消后状态变更为"已撤回"（状态 4）。
     * </p>
     *
     * @param employeeId 员工 ID
     * @param leaveId    请假记录 ID
     * @throws GlobalException 记录不存在、非本人或状态不可取消时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelLeave(Long employeeId, Long leaveId) {
        LeaveRequestEntity entity = leaveRequestMapper.selectById(leaveId);
        if (entity == null) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "请假记录不存在");
        }
        if (!entity.getEmployeeId().equals(employeeId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "只能取消自己的请假");
        }
        if (entity.getApprovalStatus() != 0 && entity.getApprovalStatus() != 1) {
            throw new GlobalException(ErrorCode.BUSINESS_ERROR, "仅草稿或审批中的请假可取消");
        }

        entity.setApprovalStatus(4); // 已撤回
        leaveRequestMapper.updateById(entity);
    }

    /**
     * 查询当前年度的请假余额
     * <p>
     * 查询本年度的年假、病假、调休的总额度、已使用和剩余天数。
     * 若某假期类型未配置余额，则返回 0。
     * </p>
     *
     * @param employeeId 员工 ID
     * @return 请假余额 VO
     */
    @Override
    public LeaveBalanceVO getLeaveBalance(Long employeeId) {
        int currentYear = LocalDate.now().getYear();

        List<LeaveBalanceEntity> balances = leaveBalanceMapper.selectList(
                new LambdaQueryWrapper<LeaveBalanceEntity>()
                        .eq(LeaveBalanceEntity::getEmployeeId, employeeId)
                        .eq(LeaveBalanceEntity::getBalanceYear, currentYear)
                        .eq(LeaveBalanceEntity::getStatus, 1)
        );

        LeaveBalanceVO vo = new LeaveBalanceVO();

        // 年假
        LeaveBalanceEntity annual = findBalance(balances, "ANNUAL");
        if (annual != null) {
            vo.setAnnualTotal(annual.getTotalDays());
            vo.setAnnualUsed(annual.getUsedDays());
            vo.setAnnualRemaining(annual.getRemainingDays());
        } else {
            vo.setAnnualTotal(BigDecimal.ZERO);
            vo.setAnnualUsed(BigDecimal.ZERO);
            vo.setAnnualRemaining(BigDecimal.ZERO);
        }

        // 病假
        LeaveBalanceEntity sick = findBalance(balances, "SICK");
        if (sick != null) {
            vo.setSickTotal(sick.getTotalDays());
            vo.setSickUsed(sick.getUsedDays());
            vo.setSickRemaining(sick.getRemainingDays());
        } else {
            vo.setSickTotal(BigDecimal.ZERO);
            vo.setSickUsed(BigDecimal.ZERO);
            vo.setSickRemaining(BigDecimal.ZERO);
        }

        // 调休
        LeaveBalanceEntity compassionate = findBalance(balances, "COMPASSIONATE");
        if (compassionate != null) {
            vo.setCompassionateTotal(compassionate.getTotalDays());
            vo.setCompassionateUsed(compassionate.getUsedDays());
            vo.setCompassionateRemaining(compassionate.getRemainingDays());
        } else {
            vo.setCompassionateTotal(BigDecimal.ZERO);
            vo.setCompassionateUsed(BigDecimal.ZERO);
            vo.setCompassionateRemaining(BigDecimal.ZERO);
        }

        return vo;
    }

    /**
     * 从余额列表中查找指定类型的余额
     */
    private LeaveBalanceEntity findBalance(List<LeaveBalanceEntity> balances, String leaveType) {
        return balances.stream()
                .filter(b -> leaveType.equals(b.getLeaveType()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 实体转 VO
     */
    private LeaveVO toLeaveVO(LeaveRequestEntity entity) {
        LeaveVO vo = new LeaveVO();
        vo.setId(entity.getId());
        vo.setLeaveType(entity.getLeaveType());
        vo.setLeaveTypeDesc(getLeaveTypeDesc(entity.getLeaveType()));
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setTotalDays(entity.getTotalDays());
        vo.setLeaveReason(entity.getLeaveReason());
        vo.setApprovalStatus(entity.getApprovalStatus());
        vo.setApprovalStatusDesc(getApprovalStatusDesc(entity.getApprovalStatus()));
        vo.setApprovalInstanceId(entity.getApprovalInstanceId());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private String getLeaveTypeDesc(String type) {
        if (type == null) return "";
        return switch (type) {
            case "ANNUAL" -> "年假";
            case "COMPASSIONATE" -> "调休";
            case "SICK" -> "病假";
            case "PERSONAL" -> "事假";
            case "MARRIAGE" -> "婚假";
            case "MATERNITY" -> "产假";
            case "FUNERAL" -> "丧假";
            default -> type;
        };
    }

    private String getApprovalStatusDesc(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "审批中";
            case 2 -> "已通过";
            case 3 -> "已拒绝";
            case 4 -> "已撤回";
            default -> "未知";
        };
    }
}

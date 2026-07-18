package com.hrms.business.mycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.business.approval.service.ApprovalService;
import com.hrms.business.mycenter.dto.LeaveBalanceVO;
import com.hrms.business.mycenter.dto.LeaveRequestDTO;
import com.hrms.business.mycenter.dto.LeaveVO;
import com.hrms.business.mycenter.entity.LeaveRequestEntity;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final MyCenterLeaveRequestMapper leaveRequestMapper;
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

    @Override
    public List<LeaveVO> listLeaves(Long employeeId) {
        List<LeaveRequestEntity> list = leaveRequestMapper.selectList(
                new LambdaQueryWrapper<LeaveRequestEntity>()
                        .eq(LeaveRequestEntity::getEmployeeId, employeeId)
                        .orderByDesc(LeaveRequestEntity::getCreateTime)
        );

        return list.stream().map(this::toLeaveVO).collect(Collectors.toList());
    }

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

    @Override
    public LeaveBalanceVO getLeaveBalance(Long employeeId) {
        int currentYear = LocalDate.now().getYear();

        // 统计本年已审批通过的年假天数
        BigDecimal annualUsed = getApprovedSum(employeeId, "ANNUAL", currentYear);

        // 统计本年已通过的调休小时数
        BigDecimal compassionateUsed = getApprovedSumForCompassionate(employeeId, currentYear);

        // 统计本年已申请的调休小时数（含审批中）
        BigDecimal compassionatePending = getPendingCompassionate(employeeId, currentYear);

        LeaveBalanceVO vo = new LeaveBalanceVO();
        // 年假：固定总额 15 天（简化，实际需对接工龄规则）
        vo.setAnnualTotal(new BigDecimal("15"));
        vo.setAnnualUsed(annualUsed);
        vo.setAnnualRemaining(new BigDecimal("15").subtract(annualUsed));

        // 调休：按已用+审批中计算剩余
        BigDecimal totalCompassionate = new BigDecimal("40"); // 简化：40小时
        BigDecimal compassionateTotalUsed = compassionateUsed.add(compassionatePending);
        vo.setCompassionateTotal(totalCompassionate);
        vo.setCompassionateUsed(compassionateTotalUsed);
        vo.setCompassionateRemaining(totalCompassionate.subtract(compassionateTotalUsed));

        return vo;
    }

    /**
     * 统计某年某类型已通过审批的总天数
     */
    private BigDecimal getApprovedSum(Long employeeId, String leaveType, int year) {
        List<LeaveRequestEntity> list = leaveRequestMapper.selectList(
                new LambdaQueryWrapper<LeaveRequestEntity>()
                        .eq(LeaveRequestEntity::getEmployeeId, employeeId)
                        .eq(LeaveRequestEntity::getLeaveType, leaveType)
                        .eq(LeaveRequestEntity::getApprovalStatus, 2) // 已通过
                        .apply("YEAR(create_time) = {0}", year)
        );
        return list.stream()
                .map(e -> e.getTotalDays() != null ? e.getTotalDays() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 统计某年已通过调休总小时数
     */
    private BigDecimal getApprovedSumForCompassionate(Long employeeId, int year) {
        List<LeaveRequestEntity> list = leaveRequestMapper.selectList(
                new LambdaQueryWrapper<LeaveRequestEntity>()
                        .eq(LeaveRequestEntity::getEmployeeId, employeeId)
                        .eq(LeaveRequestEntity::getLeaveType, "COMPASSIONATE")
                        .eq(LeaveRequestEntity::getApprovalStatus, 2) // 已通过
                        .apply("YEAR(create_time) = {0}", year)
        );
        return list.stream()
                .map(e -> e.getTotalHours() != null ? e.getTotalHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 统计某年审批中的调休总小时数
     */
    private BigDecimal getPendingCompassionate(Long employeeId, int year) {
        List<LeaveRequestEntity> list = leaveRequestMapper.selectList(
                new LambdaQueryWrapper<LeaveRequestEntity>()
                        .eq(LeaveRequestEntity::getEmployeeId, employeeId)
                        .eq(LeaveRequestEntity::getLeaveType, "COMPASSIONATE")
                        .in(LeaveRequestEntity::getApprovalStatus, 0, 1) // 草稿或审批中
                        .apply("YEAR(create_time) = {0}", year)
        );
        return list.stream()
                .map(e -> e.getTotalHours() != null ? e.getTotalHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

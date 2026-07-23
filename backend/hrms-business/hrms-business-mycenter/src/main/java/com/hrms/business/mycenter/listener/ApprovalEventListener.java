package com.hrms.business.mycenter.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.business.approval.service.event.ApprovalCompletedEvent;
import com.hrms.business.mycenter.entity.AttendanceCorrectionEntity;
import com.hrms.business.mycenter.entity.AttendanceOvertimeEntity;
import com.hrms.business.mycenter.entity.LeaveBalanceEntity;
import com.hrms.business.mycenter.entity.LeaveRequestEntity;
import com.hrms.business.mycenter.entity.MyAttendanceRecordEntity;
import com.hrms.business.mycenter.mapper.AttendanceOvertimeMapper;
import com.hrms.business.mycenter.mapper.MyAttendanceRecordMapper;
import com.hrms.business.mycenter.mapper.MyCenterAttendanceCorrectionMapper;
import com.hrms.business.mycenter.mapper.MyCenterLeaveBalanceMapper;
import com.hrms.business.mycenter.mapper.MyCenterLeaveRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审批完成事件监听器
 * <p>
 * 监听 {@link ApprovalCompletedEvent}，处理个人中心相关业务的审批回调：
 * <ul>
 *   <li>LEAVE_REQUEST — 请假申请审批通过/驳回后更新请假记录状态</li>
 *   <li>CORRECTION — 补卡申请审批通过/驳回后更新补卡记录状态</li>
 *   <li>OVERTIME — 加班申请审批通过/驳回后更新加班记录状态</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalEventListener {

    private final MyCenterLeaveRequestMapper leaveRequestMapper;
    private final MyCenterLeaveBalanceMapper leaveBalanceMapper;
    private final MyCenterAttendanceCorrectionMapper correctionMapper;
    private final AttendanceOvertimeMapper overtimeMapper;
    private final MyAttendanceRecordMapper attendanceRecordMapper;

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handleApprovalCompleted(ApprovalCompletedEvent event) {
        log.info("收到审批完成事件: instanceId={}, type={}, bizId={}, status={}",
                event.getInstanceId(), event.getApprovalType(), event.getBizId(), event.getInstanceStatus());

        switch (event.getApprovalType()) {
            case "LEAVE_REQUEST" -> handleLeaveCompleted(event);
            case "CORRECTION" -> handleCorrectionCompleted(event);
            case "OVERTIME" -> handleOvertimeCompleted(event);
            default -> log.debug("忽略非本模块的审批类型: {}", event.getApprovalType());
        }
    }

    /**
     * 处理请假审批完成：根据审批结果更新请假记录状态，通过时扣减假期余额
     */
    private void handleLeaveCompleted(ApprovalCompletedEvent event) {
        LeaveRequestEntity leave = leaveRequestMapper.selectById(event.getBizId());
        if (leave == null) {
            log.warn("请假记录不存在: id={}", event.getBizId());
            return;
        }

        // instanceStatus: 2=已通过, 3=已驳回
        int newStatus = event.getInstanceStatus() == 2 ? 2 : 3;
        leave.setApprovalStatus(newStatus);
        leaveRequestMapper.updateById(leave);

        // 审批通过 → 扣减假期余额
        if (newStatus == 2) {
            deductLeaveBalance(leave);
        }

        log.info("请假审批完成: leaveId={}, newStatus={}", event.getBizId(), newStatus);
    }

    /**
     * 扣减假期余额：根据请假天数更新对应假期类型的已用/剩余天数
     */
    private void deductLeaveBalance(LeaveRequestEntity leave) {
        int year = leave.getStartTime() != null
                ? leave.getStartTime().getYear()
                : LocalDate.now().getYear();
        BigDecimal days = leave.getTotalDays();
        if (days == null || days.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("请假天数为0或空，跳过扣减: leaveId={}", leave.getId());
            return;
        }

        LeaveBalanceEntity balance = leaveBalanceMapper.selectOne(
                new LambdaQueryWrapper<LeaveBalanceEntity>()
                        .eq(LeaveBalanceEntity::getEmployeeId, leave.getEmployeeId())
                        .eq(LeaveBalanceEntity::getLeaveType, leave.getLeaveType())
                        .eq(LeaveBalanceEntity::getBalanceYear, year)
                        .eq(LeaveBalanceEntity::getStatus, 1)
        );

        if (balance == null) {
            log.warn("假期余额记录不存在，无法扣减: employeeId={}, leaveType={}, year={}",
                    leave.getEmployeeId(), leave.getLeaveType(), year);
            return;
        }

        balance.setUsedDays(balance.getUsedDays().add(days));
        balance.setRemainingDays(balance.getTotalDays()
                .subtract(balance.getUsedDays())
                .subtract(balance.getFrozenDays()));
        leaveBalanceMapper.updateById(balance);

        log.info("假期余额扣减成功: employeeId={}, leaveType={}, used={}, remaining={}",
                leave.getEmployeeId(), leave.getLeaveType(),
                balance.getUsedDays(), balance.getRemainingDays());
    }

    /**
     * 处理补卡审批完成：根据审批结果更新补卡记录状态，通过时同步更新考勤记录
     */
    private void handleCorrectionCompleted(ApprovalCompletedEvent event) {
        AttendanceCorrectionEntity correction = correctionMapper.selectById(event.getBizId());
        if (correction == null) {
            log.warn("补卡记录不存在: id={}", event.getBizId());
            return;
        }

        // instanceStatus: 2=已通过, 3=已驳回
        int newStatus = event.getInstanceStatus() == 2 ? 2 : 3;
        correction.setApprovalStatus(newStatus);
        correctionMapper.updateById(correction);

        // 审批通过 → 同步更新考勤记录
        if (newStatus == 2) {
            // 1. 先通过 recordId 查找考勤记录（提交申请时关联的）
            MyAttendanceRecordEntity record = null;
            if (correction.getRecordId() != null) {
                record = attendanceRecordMapper.selectById(correction.getRecordId());
            }
            // 2. 找不到则按员工+日期查找（可能当天完全无打卡记录）
            if (record == null) {
                record = attendanceRecordMapper.selectOne(
                        new LambdaQueryWrapper<MyAttendanceRecordEntity>()
                                .eq(MyAttendanceRecordEntity::getEmployeeId, correction.getEmployeeId())
                                .eq(MyAttendanceRecordEntity::getRecordDate, correction.getCorrectionDate())
                );
            }
            // 3. 仍然找不到 → 创建一条新考勤记录
            boolean isNewRecord = false;
            if (record == null) {
                record = new MyAttendanceRecordEntity();
                record.setEmployeeId(correction.getEmployeeId());
                record.setRecordDate(correction.getCorrectionDate());
                Long groupId = attendanceRecordMapper.selectDefaultAttendanceGroupId();
                record.setGroupId(groupId);
                attendanceRecordMapper.insert(record);
                isNewRecord = true;
                log.info("补卡审批通过，已新建考勤记录: employeeId={}, date={}",
                        correction.getEmployeeId(), correction.getCorrectionDate());
            }

            if (isNewRecord) {
                // 新建记录：当天完全无打卡数据，补卡审批通过意味着全天出勤有效
                record.setClockInStatus("NORMAL");
                record.setClockOutStatus("NORMAL");
            } else {
                // 已有记录：仅更新补卡对应的状态，保留另一方原始状态（如 LATE / EARLY_LEAVE）
                if ("CLOCK_IN".equals(correction.getCorrectionType())) {
                    record.setClockInStatus("NORMAL");
                } else if ("CLOCK_OUT".equals(correction.getCorrectionType())) {
                    record.setClockOutStatus("NORMAL");
                }
            }
            record.setCorrectionStatus("APPROVED");
            attendanceRecordMapper.updateById(record);

            log.info("补卡审批通过，已同步更新考勤记录: recordId={}, correctionType={}",
                    record.getId(), correction.getCorrectionType());
        }

        log.info("补卡审批完成: correctionId={}, newStatus={}", event.getBizId(), newStatus);
    }

    /**
     * 处理加班审批完成：根据审批结果更新加班记录状态
     */
    private void handleOvertimeCompleted(ApprovalCompletedEvent event) {
        AttendanceOvertimeEntity overtime = overtimeMapper.selectById(event.getBizId());
        if (overtime == null) {
            log.warn("加班记录不存在: id={}", event.getBizId());
            return;
        }

        // instanceStatus: 2=已通过, 3=已驳回
        int newStatus = event.getInstanceStatus() == 2 ? 2 : 3;
        overtime.setApprovalStatus(newStatus);
        overtimeMapper.updateById(overtime);

        log.info("加班审批完成: overtimeId={}, newStatus={}", event.getBizId(), newStatus);
    }
}

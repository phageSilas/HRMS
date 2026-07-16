package com.hrms.business.mycenter.listener;

import com.hrms.business.approval.service.event.ApprovalCompletedEvent;
import com.hrms.business.mycenter.entity.AttendanceCorrectionEntity;
import com.hrms.business.mycenter.entity.LeaveRequestEntity;
import com.hrms.business.mycenter.mapper.MyCenterAttendanceCorrectionMapper;
import com.hrms.business.mycenter.mapper.MyCenterLeaveRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * </ul>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalEventListener {

    private final MyCenterLeaveRequestMapper leaveRequestMapper;
    private final MyCenterAttendanceCorrectionMapper correctionMapper;

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handleApprovalCompleted(ApprovalCompletedEvent event) {
        log.info("收到审批完成事件: instanceId={}, type={}, bizId={}, status={}",
                event.getInstanceId(), event.getApprovalType(), event.getBizId(), event.getInstanceStatus());

        switch (event.getApprovalType()) {
            case "LEAVE_REQUEST" -> handleLeaveCompleted(event);
            case "CORRECTION" -> handleCorrectionCompleted(event);
            default -> log.debug("忽略非本模块的审批类型: {}", event.getApprovalType());
        }
    }

    /**
     * 处理请假审批完成：根据审批结果更新请假记录状态
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

        log.info("请假审批完成: leaveId={}, newStatus={}", event.getBizId(), newStatus);
    }

    /**
     * 处理补卡审批完成：根据审批结果更新补卡记录状态
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

        log.info("补卡审批完成: correctionId={}, newStatus={}", event.getBizId(), newStatus);
    }
}

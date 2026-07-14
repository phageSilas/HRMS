package com.hrms.business.attendance.convert;

import com.hrms.business.attendance.entity.AttendanceGroupEntity;
import com.hrms.business.attendance.vo.AttendanceGroupPageVO;

/**
 * 考勤组对象转换器。
 */
public final class AttendanceGroupConvert {

    private AttendanceGroupConvert() {
    }

    /**
     * 将考勤组实体转换为分页响应 VO。
     *
     * @param entity 考勤组实体
     * @return 考勤组分页响应
     * 本方法使用的工具类: 无
     */
    public static AttendanceGroupPageVO toPageVO(AttendanceGroupEntity entity) {
        if (entity == null) {
            return null;
        }
        AttendanceGroupPageVO vo = new AttendanceGroupPageVO();
        vo.setId(entity.getId());
        vo.setGroupName(entity.getGroupName());
        vo.setShiftType(entity.getShiftType());
        vo.setWorkStartTime(entity.getWorkStartTime());
        vo.setWorkEndTime(entity.getWorkEndTime());
        vo.setLateThresholdMinutes(entity.getLateThresholdMinutes());
        vo.setEarlyLeaveThresholdMinutes(entity.getEarlyLeaveThresholdMinutes());
        vo.setMonthlyCorrectionLimit(entity.getMonthlyCorrectionLimit());
        vo.setStatus(entity.getStatus());
        vo.setStatusText(Integer.valueOf(1).equals(entity.getStatus()) ? "启用" : "禁用");
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}

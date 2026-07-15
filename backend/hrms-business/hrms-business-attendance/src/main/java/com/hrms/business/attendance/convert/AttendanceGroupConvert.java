package com.hrms.business.attendance.convert;

import cn.hutool.json.JSONUtil;
import com.hrms.business.attendance.dto.AttendanceGroupCreateOrUpdateRequestDTO;
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
        return AttendanceGroupPageVO.builder()
                .id(entity.getId())
                .groupName(entity.getGroupName())
                .shiftType(entity.getShiftType())
                .workStartTime(entity.getWorkStartTime())
                .workEndTime(entity.getWorkEndTime())
                .lateThresholdMinutes(entity.getLateThresholdMinutes())
                .earlyLeaveThresholdMinutes(entity.getEarlyLeaveThresholdMinutes())
                .monthlyCorrectionLimit(entity.getMonthlyCorrectionLimit())
                .status(entity.getStatus())
                .statusText(Integer.valueOf(1).equals(entity.getStatus()) ? "鍚敤" : "绂佺敤")
                .createTime(entity.getCreateTime())
                .build();
    }

    /**
     * 将创建或更新请求转换为考勤组实体。
     *
     * @param requestDTO 创建或更新请求
     * @return 考勤组实体
     * 本方法使用的工具类: JSONUtil(hutool)
     */
    public static AttendanceGroupEntity toEntity(AttendanceGroupCreateOrUpdateRequestDTO requestDTO) {
        AttendanceGroupEntity entity = new AttendanceGroupEntity();
        fillEntity(entity, requestDTO);
        return entity;
    }

    /**
     * 使用请求参数填充考勤组实体。
     *
     * @param entity     考勤组实体
     * @param requestDTO 创建或更新请求
     * 本方法使用的工具类: JSONUtil(hutool)
     */
    public static void fillEntity(AttendanceGroupEntity entity, AttendanceGroupCreateOrUpdateRequestDTO requestDTO) {
        entity.setGroupName(requestDTO.getGroupName());
        entity.setShiftType(normalizeShiftType(requestDTO.getShiftType()));
        entity.setWorkStartTime(requestDTO.getClockInTime());
        entity.setWorkEndTime(requestDTO.getClockOutTime());
        entity.setRestStartTime(requestDTO.getRestStartTime());
        entity.setRestEndTime(requestDTO.getRestEndTime());
        entity.setFlexibleStartTime(requestDTO.getFlexibleStartTime());
        entity.setFlexibleEndTime(requestDTO.getFlexibleEndTime());
        entity.setLateThresholdMinutes(defaultInt(requestDTO.getLateThreshold()));
        entity.setEarlyLeaveThresholdMinutes(defaultInt(requestDTO.getEarlyLeaveThreshold()));
        entity.setMonthlyCorrectionLimit(defaultInt(requestDTO.getMaxCorrectionCount()));
        entity.setClockIpWhitelist(requestDTO.getIpWhitelist());
        entity.setClockGpsScope(requestDTO.getLocationRange() == null ? null : JSONUtil.toJsonStr(requestDTO.getLocationRange()));
        entity.setStatus(requestDTO.getStatus() == null ? 1 : requestDTO.getStatus());
    }

    private static String normalizeShiftType(String shiftType) {
        if (shiftType == null) {
            return null;
        }
        return shiftType.trim().toUpperCase();
    }

    private static Integer defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}

package com.hrms.business.mycenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.business.mycenter.entity.MyAttendanceRecordEntity;
import org.apache.ibatis.annotations.Select;

/**
 * 考勤记录 Mapper（个人中心只读视角）
 */
public interface MyAttendanceRecordMapper extends BaseMapper<MyAttendanceRecordEntity> {

    /**
     * 查询员工默认考勤组ID
     */
    @Select("SELECT id FROM hr_attendance_group WHERE status = 1 AND is_deleted = 0 ORDER BY id ASC LIMIT 1")
    Long selectDefaultAttendanceGroupId();

    /**
     * 查询考勤组时间配置
     *
     * @param groupId 考勤组ID
     * @return 时间配置（workStartTime / workEndTime / lateThresholdMinutes / earlyLeaveThresholdMinutes）
     */
    @Select("SELECT work_start_time, work_end_time, late_threshold_minutes, early_leave_threshold_minutes FROM hr_attendance_group WHERE id = #{groupId}")
    com.hrms.business.mycenter.dto.AttendanceGroupConfigDTO selectGroupTimeConfig(Long groupId);

}

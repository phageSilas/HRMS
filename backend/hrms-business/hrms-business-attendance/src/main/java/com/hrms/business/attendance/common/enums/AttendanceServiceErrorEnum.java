package com.hrms.business.attendance.common.enums;

import com.hrms.common.exception.ErrorCode;

/**
 * 考勤服务错误码定义。
 */
public enum AttendanceServiceErrorEnum {
    ;

    /**
     * 考勤组不存在。
     */
    public static final ErrorCode ATTENDANCE_GROUP_NOT_FOUND = new ErrorCode(40052, "考勤组不存在");

    /**
     * 当前用户未关联员工档案。
     */
    public static final ErrorCode ATTENDANCE_EMPLOYEE_NOT_FOUND = new ErrorCode(40053, "当前用户未关联员工档案");

    /**
     * 当前时段已打卡。
     */
    public static final ErrorCode ATTENDANCE_CLOCK_DUPLICATE = new ErrorCode(40054, "当前时段已打卡");

    /**
     * 不在允许的打卡范围内。
     */
    public static final ErrorCode ATTENDANCE_CLOCK_RANGE_INVALID = new ErrorCode(40055, "不在允许的打卡范围内");

    /**
     * 今天为法定节假日，无需打卡。
     */
    public static final ErrorCode ATTENDANCE_CLOCK_HOLIDAY_SKIP = new ErrorCode(40060, "今天是法定节假日,无需打卡");

    /**
     * 当前日期和类型已有审批中的补卡申请。
     */
    public static final ErrorCode ATTENDANCE_CORRECTION_DUPLICATE = new ErrorCode(40056, "当前日期和类型已有审批中的补卡申请");

    /**
     * 请假天数必须大于0且不超过30天。
     */
    public static final ErrorCode LEAVE_DAYS_INVALID = new ErrorCode(40057, "请假天数必须大于0且不超过30天");

    /**
     * 考勤组已关联成员，无法删除。
     */
    public static final ErrorCode ATTENDANCE_GROUP_MEMBER_EXISTS = new ErrorCode(40058, "考勤组已关联成员，无法删除");

    /**
     * 考勤组已产生打卡记录，无法删除。
     */
    public static final ErrorCode ATTENDANCE_GROUP_RECORD_EXISTS = new ErrorCode(40059, "考勤组已产生打卡记录，无法删除");
}

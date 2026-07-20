package com.hrms.business.personnel.common.enums;

import com.hrms.common.exception.ErrorCode;

public enum ServiceErrorCodeEnum {
    ;
    public static final ErrorCode ENTRY_APPLICATION_PHONE_DUPLICATE = new ErrorCode(40045, "手机号已存在入职申请");

    public static final ErrorCode ENTRY_APPLICATION_NOT_FOUND = new ErrorCode(40041, "入职申请不存在");

    public static final ErrorCode ENTRY_APPLICATION_NOT_DRAFT = new ErrorCode(40042, "非草稿状态无法修改");

    public static final ErrorCode ENTRY_APPLICATION_NOT_APPROVED = new ErrorCode(40044, "审批未通过，无法确认入职");

    public static final ErrorCode ENTRY_APPLICATION_EMPLOYEE_MISSING = new ErrorCode(40046, "已入职申请缺少员工回写信息");

    public static final ErrorCode EMPLOYEE_NOT_FOUND = new ErrorCode(40060, "员工不存在");

    public static final ErrorCode LEAVE_APPLICATION_DUPLICATE = new ErrorCode(40081, "员工已有进行中的离职申请");

    public static final ErrorCode REGULAR_APPLICATION_DUPLICATE = new ErrorCode(40061, "员工已有进行中的转正申请");

    public static final ErrorCode REGULAR_EXTEND_MONTH_REQUIRED = new ErrorCode(40062, "延长试用时必须填写延长月数");

    public static final ErrorCode TRANSFER_APPLICATION_DUPLICATE = new ErrorCode(40071, "员工已有进行中的调岗申请");




}

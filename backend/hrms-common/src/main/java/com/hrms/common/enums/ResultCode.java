package com.hrms.common.enums;

/**
 * 定义全系统统一业务响应码。
 */
public enum ResultCode {

    SUCCESS("0000", "操作成功"),
    PARAM_ERROR("1000", "请求参数错误"),
    UNAUTHORIZED("2000", "用户未认证"),
    FORBIDDEN("2001", "用户无权限"),
    SYSTEM_USER_NOT_FOUND("3001", "系统用户不存在"),
    ROLE_NOT_FOUND("3002", "角色不存在"),
    DEPARTMENT_NOT_FOUND("4003", "部门不存在"),
    POSITION_NOT_FOUND("4004", "职位不存在"),
    EMPLOYEE_NOT_FOUND("4001", "员工不存在"),
    EMPLOYEE_NO_DUPLICATED("4002", "工号重复"),
    APPROVAL_TASK_NOT_FOUND("5001", "审批单不存在"),
    APPROVAL_PERMISSION_DENIED("5002", "当前节点无审批权限"),
    ATTENDANCE_RECORD_NOT_FOUND("6001", "考勤记录不存在"),
    SALARY_RECORD_NOT_FOUND("7001", "薪资记录不存在"),
    INTERNAL_ERROR("9000", "系统内部异常");

    private final String code;
    private final String message;

    /**
     * 创建统一业务响应码枚举。
     *
     * @param code 业务响应码
     * @param message 响应描述
     */
    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取业务响应码。
     *
     * @return 业务响应码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取响应描述。
     *
     * @return 响应描述
     */
    public String getMessage() {
        return message;
    }
}

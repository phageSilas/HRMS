package com.hrms.common.exception;

/**
 * 系统统一错误码。
 *
 * <p>遵循全局技术底座契约的码段划分：</p>
 * <ul>
 *   <li>{@code 0} —— 成功</li>
 *   <li>{@code 40001-40099} —— 客户端参数错误</li>
 *   <li>{@code 40100-40199} —— 认证与授权错误</li>
 *   <li>{@code 50001-50099} —— 系统内部错误</li>
 *   <li>{@code 60001-60999} —— 业务逻辑异常，由各业务模块自行分配：
 *     <ul>
 *       <li>60001-60099 档案</li>
 *       <li>60100-60199 组织</li>
 *       <li>60200-60299 入离职</li>
 *       <li>60300-60399 考勤</li>
 *       <li>60400-60499 薪资</li>
 *       <li>60500-60599 审批</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public enum ErrorCode {

    /** 操作成功。 */
    SUCCESS(0, "操作成功"),

    /** 参数缺失。 */
    PARAM_MISSING(40001, "参数缺失"),
    /** 参数格式错误。 */
    PARAM_INVALID(40002, "参数格式错误"),

    /** 未登录。 */
    UNAUTHORIZED(40100, "未登录"),
    /** 无权限。 */
    FORBIDDEN(40101, "无权限"),

    /** 数据库异常。 */
    DB_ERROR(50001, "数据库异常"),
    /** 缓存异常。 */
    CACHE_ERROR(50002, "缓存异常"),
    /** 远程调用超时。 */
    REMOTE_TIMEOUT(50003, "远程调用超时"),
    /** 系统内部错误。 */
    INTERNAL_ERROR(50099, "系统内部错误");

    private final int code;
    private final String message;

    /**
     * 创建错误码。
     *
     * @param code 状态码
     * @param message 状态描述
     */
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取状态码数值。
     *
     * @return 状态码数值
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取状态描述信息。
     *
     * @return 状态描述信息
     */
    public String getMessage() {
        return message;
    }
}

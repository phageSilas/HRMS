package com.hrms.common.enums;

/**
 * 定义系统统一响应状态码。
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    INTERNAL_ERROR(500, "系统内部错误");

    private final int code;
    private final String message;

    /**
     * 创建统一响应状态码枚举。
     *
     * @param code 状态码
     * @param message 状态描述
     */
    ResultCode(int code, String message) {
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

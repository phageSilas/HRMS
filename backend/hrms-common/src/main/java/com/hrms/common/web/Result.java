package com.hrms.common.web;

import com.hrms.common.exception.ErrorCode;

/**
 * 系统统一接口返回结构。
 *
 * <p>遵循全局技术底座契约：{@code code} 为 0 表示成功，非 0 表示异常。</p>
 *
 * @param <T> 返回数据类型
 */
public class Result<T> {

    /** 成功状态码。 */
    public static final int CODE_SUCCESS = 0;

    private final int code;
    private final String message;
    private final T data;

    /**
     * 创建统一返回对象。
     *
     * @param code 响应状态码
     * @param message 响应消息
     * @param data 响应数据
     */
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构建成功响应且返回数据。
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 统一返回对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_SUCCESS, ErrorCode.SUCCESS.getMessage(), data);
    }

    /**
     * 构建成功响应且不返回业务数据。
     *
     * @return 统一返回对象
     */
    public static Result<Void> success() {
        return new Result<>(CODE_SUCCESS, ErrorCode.SUCCESS.getMessage(), null);
    }

    /**
     * 构建失败响应，使用错误码自带描述。
     *
     * @param errorCode 错误码
     * @param <T> 数据类型
     * @return 统一返回对象
     */
    public static <T> Result<T> failure(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 构建失败响应，覆盖错误码描述。
     *
     * @param errorCode 错误码
     * @param message 响应消息
     * @param <T> 数据类型
     * @return 统一返回对象
     */
    public static <T> Result<T> failure(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }

    /**
     * 构建失败响应，直接指定状态码与消息。
     *
     * @param code 状态码
     * @param message 响应消息
     * @param <T> 数据类型
     * @return 统一返回对象
     */
    public static <T> Result<T> failure(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 获取响应状态码。
     *
     * @return 响应状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取响应消息。
     *
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取响应数据。
     *
     * @return 响应数据
     */
    public T getData() {
        return data;
    }

    /**
     * 判断是否为成功响应。
     *
     * @return 成功返回 true
     */
    public boolean isSuccess() {
        return code == CODE_SUCCESS;
    }
}

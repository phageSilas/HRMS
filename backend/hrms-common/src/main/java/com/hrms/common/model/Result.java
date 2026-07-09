package com.hrms.common.model;

import com.hrms.common.enums.ResultCode;

/**
 * 定义系统统一接口返回结构。
 *
 * @param <T> 返回数据类型
 */
public class Result<T> {

    private final String code;
    private final String message;
    private final T data;

    /**
     * 创建统一返回对象。
     *
     * @param code 业务响应码
     * @param message 响应消息
     * @param data 响应数据
     */
    private Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构建成功响应并返回数据。
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 统一返回对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 构建成功响应且不返回业务数据。
     *
     * @return 统一返回对象
     */
    public static Result<Void> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 构建失败响应。
     *
     * @param resultCode 统一业务响应码
     * @param message 响应消息
     * @param <T> 数据类型
     * @return 统一返回对象
     */
    public static <T> Result<T> failure(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    /**
     * 构建失败响应并使用默认消息。
     *
     * @param resultCode 统一业务响应码
     * @param <T> 数据类型
     * @return 统一返回对象
     */
    public static <T> Result<T> failure(ResultCode resultCode) {
        return failure(resultCode, resultCode.getMessage());
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
}

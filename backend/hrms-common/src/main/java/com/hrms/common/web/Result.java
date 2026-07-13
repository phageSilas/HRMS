package com.hrms.common.web;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回体
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功码
     */
    public static final int SUCCESS_CODE = 20000;

    /**
     * 错误码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return Result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "success", data);
    }

    /**
     * 成功返回（无数据）
     *
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, "success", null);
    }

    /**
     * 失败返回
     *
     * @param message 消息
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> failure(String message) {
        return new Result<>(50000, message, null);
    }

    /**
     * 失败返回
     *
     * @param code    错误码
     * @param message 消息
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> failure(int code, String message) {
        return new Result<>(code, message, null);
    }

}

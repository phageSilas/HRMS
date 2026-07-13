package com.hrms.common.exception;

import lombok.Data;

import java.io.Serializable;

/**
 * 全局业务异常
 */
@Data
public class GlobalException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     */
    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   自定义消息
     */
    public GlobalException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param cause     原始异常
     */
    public GlobalException(ErrorCode errorCode, Throwable cause) {
        super(message(errorCode.getMessage(), cause), cause);
        this.errorCode = errorCode;
    }

    private static String message(String customMessage, Throwable cause) {
        return customMessage != null ? customMessage : cause.getMessage();
    }

}

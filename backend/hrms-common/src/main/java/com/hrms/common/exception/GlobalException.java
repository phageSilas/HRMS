package com.hrms.common.exception;

/**
 * 系统全局业务异常。
 *
 * <p>业务层抛出该异常时携带 {@link ErrorCode}，由
 * {@link com.hrms.common.handler.GlobalExceptionHandler} 统一转换为响应体。</p>
 */
public class GlobalException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 创建全局异常，使用错误码自带描述。
     *
     * @param errorCode 错误码
     */
    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 创建全局异常，覆盖错误码描述。
     *
     * @param errorCode 错误码
     * @param message 异常消息
     */
    public GlobalException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 创建全局异常并携带原因。
     *
     * @param errorCode 错误码
     * @param message 异常消息
     * @param cause 原始异常
     */
    public GlobalException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取异常错误码。
     *
     * @return 错误码
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

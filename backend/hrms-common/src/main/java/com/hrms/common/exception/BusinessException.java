package com.hrms.common.exception;

/**
 * 业务异常。
 *
 * <p>用于业务层抛出的异常，由 GlobalExceptionHandler 统一处理。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 * throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名不能为空");
 * </pre>
 */
public class BusinessException extends GlobalException {

    /**
     * 使用错误码构造业务异常。
     *
     * @param errorCode 错误码
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 使用错误码和自定义消息构造业务异常。
     *
     * @param errorCode 错误码
     * @param message 自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
package com.hrms.common.exception;

import com.hrms.common.enums.ResultCode;

/**
 * 定义系统统一业务异常。
 */
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    /**
     * 创建业务异常对象。
     *
     * @param resultCode 统一状态码
     * @param message 异常消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    /**
     * 获取异常状态码。
     *
     * @return 统一状态码
     */
    public ResultCode getResultCode() {
        return resultCode;
    }
}

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
     * @param resultCode 统一业务响应码
     */
    public BusinessException(ResultCode resultCode) {
        this(resultCode, resultCode.getMessage());
    }

    /**
     * 创建业务异常对象并覆盖异常消息。
     *
     * @param resultCode 统一业务响应码
     * @param message 异常消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    /**
     * 获取异常业务响应码。
     *
     * @return 统一业务响应码
     */
    public ResultCode getResultCode() {
        return resultCode;
    }
}

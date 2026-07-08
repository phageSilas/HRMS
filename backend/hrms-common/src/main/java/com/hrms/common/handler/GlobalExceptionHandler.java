package com.hrms.common.handler;

import com.hrms.common.enums.ResultCode;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.model.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 处理系统全局异常并统一返回结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常对象
     * @return 统一返回对象
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        return Result.failure(exception.getResultCode(), exception.getMessage());
    }

    /**
     * 处理未捕获的系统异常。
     *
     * @param exception 系统异常对象
     * @return 统一返回对象
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        return Result.failure(ResultCode.INTERNAL_ERROR, exception.getMessage());
    }
}

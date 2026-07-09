package com.hrms.common.handler;

import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.web.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    @ExceptionHandler(GlobalException.class)
    public Result<Void> handleGlobalException(GlobalException exception) {
        return Result.failure(exception.getErrorCode(), exception.getMessage());
    }

    /**
     * 处理请求体参数校验异常。
     *
     * @param exception 校验异常对象
     * @return 统一返回对象
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : ErrorCode.PARAM_INVALID.getMessage();
        return Result.failure(ErrorCode.PARAM_INVALID, message);
    }

    /**
     * 处理表单参数校验异常。
     *
     * @param exception 校验异常对象
     * @return 统一返回对象
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : ErrorCode.PARAM_INVALID.getMessage();
        return Result.failure(ErrorCode.PARAM_INVALID, message);
    }

    /**
     * 处理未捕获的系统异常。
     *
     * @param exception 系统异常对象
     * @return 统一返回对象
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        return Result.failure(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getMessage());
    }
}

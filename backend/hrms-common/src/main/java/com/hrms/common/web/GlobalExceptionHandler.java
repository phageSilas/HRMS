package com.hrms.common.web;

import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(GlobalException.class)
    public Result<Void> handleGlobalException(GlobalException e) {
        return Result.failure(e.getErrorCode().getCode(), e.getMessage());
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return Result.failure(40005, "不支持的请求方法");
    }

    /**
     * 处理参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return Result.failure(40001, "缺少必需参数: " + e.getParameterName());
    }

    /**
     * 处理路径变量缺失异常
     */
    @ExceptionHandler(MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingPathVariableException(MissingPathVariableException e) {
        return Result.failure(40001, "缺少必需路径变量: " + e.getVariableName());
    }

    /**
     * 处理请求体缺失或格式错误异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String detail = e.getMessage();
        if (detail != null && detail.contains("Required request body is missing")) {
            return Result.failure(40002, "请求体不能为空，请检查是否传递了正确的 JSON 数据");
        }
        return Result.failure(40002, "请求体格式错误，请确认 Content-Type 为 application/json 且数据格式正确");
    }

    /**
     * 处理请求参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + " " + Objects.requireNonNull(fieldError.getDefaultMessage());
                    }
                    return Objects.requireNonNull(error.getDefaultMessage());
                })
                .findFirst()
                .orElse("参数校验失败");
        return Result.failure(40001, message);
    }

    /**
     * 处理绑定异常（@Validated）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + " " + Objects.requireNonNull(fieldError.getDefaultMessage());
                    }
                    return Objects.requireNonNull(error.getDefaultMessage());
                })
                .findFirst()
                .orElse("参数校验失败");
        return Result.failure(40001, message);
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        e.printStackTrace();
        return Result.failure(50000, "系统内部错误: " + e.getMessage());
    }

}

package com.hrms.system.log.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.util.IpUtils;
import com.hrms.system.log.annotation.OperateLog;
import com.hrms.system.log.entity.OperateLogEntity;
import com.hrms.system.log.service.OperateLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperateLogAspect {

    private final OperateLogService operateLogService;
    private final ObjectMapper objectMapper;

    /**
     * 线程变量，用于存储开始时间
     */
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    /**
     * 拦截 @OperateLog 注解的方法
     */
    @Before("@annotation(operateLog)")
    public void doBefore(OperateLog operateLog) {
        START_TIME.set(System.currentTimeMillis());
    }

    /**
     * 方法正常返回后记录日志
     */
    @AfterReturning(pointcut = "@annotation(operateLog)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, OperateLog operateLog, Object result) {
        try {
            recordLog(joinPoint, operateLog, result, null, 1);
        } finally {
            START_TIME.remove();
        }
    }

    /**
     * 方法抛出异常后记录日志
     */
    @AfterThrowing(pointcut = "@annotation(operateLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, OperateLog operateLog, Exception e) {
        try {
            recordLog(joinPoint, operateLog, null, e.getMessage(), 0);
        } finally {
            START_TIME.remove();
        }
    }

    /**
     * 记录操作日志
     */
    private void recordLog(JoinPoint joinPoint, OperateLog operateLog, Object result, String errorMsg, Integer status) {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return;
            }

            OperateLogEntity operateLogEntity = new OperateLogEntity();

            // 获取当前用户
            Long userId = SecurityContextHolder.getUserId();
            String username = SecurityContextHolder.getUsername();
            operateLogEntity.setUserId(userId);
            operateLogEntity.setUsername(username);

            // 操作类型和模块
            operateLogEntity.setOperateType(operateLog.businessType());
            operateLogEntity.setOperateModule(operateLog.title());

            // 请求信息
            operateLogEntity.setRequestMethod(request.getMethod());
            operateLogEntity.setRequestUrl(request.getRequestURI());

            // 请求参数
            if (operateLog.saveParam()) {
                operateLogEntity.setRequestParams(getRequestParams(joinPoint));
            }

            // 响应结果
            if (operateLog.saveResult() && result != null) {
                operateLogEntity.setResponseResult(getResponseResult(result));
            }

            // IP 和 User-Agent
            operateLogEntity.setIp(IpUtils.getIpAddr(request));
            operateLogEntity.setUserAgent(request.getHeader("User-Agent"));

            // 执行时长
            Long startTime = START_TIME.get();
            if (startTime != null) {
                operateLogEntity.setExecuteTime((int) (System.currentTimeMillis() - startTime));
            }

            // 状态和错误信息
            operateLogEntity.setStatus(status);
            operateLogEntity.setErrorMsg(errorMsg);

            // 创建时间
            operateLogEntity.setCreateTime(LocalDateTime.now());

            // 记录日志
            operateLogService.recordOperateLog(operateLogEntity);
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }
            return objectMapper.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            log.error("获取请求参数失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取响应结果
     */
    private String getResponseResult(Object result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("获取响应结果失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

}
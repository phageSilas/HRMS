package com.hrms.system.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.security.UserContext;
import com.hrms.system.log.annotation.OperationLog;
import com.hrms.system.log.entity.OperateLogDO;
import com.hrms.system.log.service.OperateLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面。
 *
 * <p>拦截标记了 @OperationLog 注解的方法，自动记录操作日志。</p>
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 拦截标记了 @OperationLog 注解的方法。
     *
     * @param joinPoint   切点
     * @param operationLog 注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        OperateLogDO logDO = new OperateLogDO();

        try {
            // 1. 准备日志基本信息
            prepareLogBasics(joinPoint, operationLog, logDO);

            // 2. 记录请求参数
            if (operationLog.recordParams()) {
                recordRequestParams(joinPoint, logDO);
            }

            // 3. 执行目标方法
            Object result = joinPoint.proceed();

            // 4. 记录响应结果
            if (operationLog.recordResult()) {
                recordResponseResult(result, logDO);
            }

            // 5. 设置成功状态
            logDO.setSuccess(1);

            // 6. 记录执行时长
            logDO.setDuration(System.currentTimeMillis() - startTime);

            // 7. 异步保存日志
            saveLogAsync(logDO);

            return result;

        } catch (Throwable e) {
            // 8. 记录失败状态
            logDO.setSuccess(0);
            logDO.setDuration(System.currentTimeMillis() - startTime);

            // 9. 记录异常信息
            if (operationLog.recordException()) {
                logDO.setErrorMessage(e.getMessage());
            }

            // 10. 异步保存日志
            saveLogAsync(logDO);

            throw e;
        }
    }

    /**
     * 准备日志基本信息。
     *
     * @param joinPoint   切点
     * @param operationLog 注解
     * @param logDO       日志实体
     */
    private void prepareLogBasics(ProceedingJoinPoint joinPoint, OperationLog operationLog, OperateLogDO logDO) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 设置模块和操作类型
        logDO.setModule(operationLog.module());
        logDO.setAction(operationLog.action());

        // 设置方法信息
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();
        logDO.setMethod(className + "." + methodName);

        // 设置操作人信息
        UserContext userContext = SecurityContextHolder.getContext();
        if (userContext != null) {
            logDO.setOperatorId(userContext.getUserId());
            logDO.setOperatorName(userContext.getUsername() != null ? userContext.getUsername() : "系统");
        } else {
            logDO.setOperatorId(0L);
            logDO.setOperatorName("匿名用户");
        }

        // 设置请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logDO.setRequestUrl(request.getRequestURI());
            logDO.setRequestMethod(request.getMethod());
            logDO.setIpAddress(getIpAddress(request));
            logDO.setLocation(""); // 可集成 IP 地址解析服务
        }

        // 设置操作时间
        logDO.setOperateTime(LocalDateTime.now());
    }

    /**
     * 记录请求参数。
     *
     * @param joinPoint 切点
     * @param logDO     日志实体
     */
    private void recordRequestParams(ProceedingJoinPoint joinPoint, OperateLogDO logDO) {
        try {
            Object[] args = joinPoint.getArgs();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();

            if (args != null && args.length > 0) {
                Map<String, Object> paramsMap = new HashMap<>();
                for (int i = 0; i < args.length; i++) {
                    if (paramNames != null && i < paramNames.length) {
                        paramsMap.put(paramNames[i], args[i]);
                    }
                }
                logDO.setRequestParams(objectMapper.writeValueAsString(paramsMap));
            }
        } catch (Exception e) {
            logDO.setRequestParams("参数解析失败: " + e.getMessage());
        }
    }

    /**
     * 记录响应结果。
     *
     * @param result 方法执行结果
     * @param logDO  日志实体
     */
    private void recordResponseResult(Object result, OperateLogDO logDO) {
        try {
            logDO.setResponseResult(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            logDO.setResponseResult("结果解析失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户端 IP 地址。
     *
     * @param request HTTP 请求
     * @return IP 地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个 IP 的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 异步保存日志。
     *
     * @param logDO 日志实体
     */
    private void saveLogAsync(OperateLogDO logDO) {
        // 异步记录日志，不阻塞主线程
        operateLogService.recordAsync(logDO);
    }
}
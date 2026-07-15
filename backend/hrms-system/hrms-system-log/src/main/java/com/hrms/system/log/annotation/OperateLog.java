package com.hrms.system.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateLog {

    /**
     * 操作模块
     */
    String title() default "";

    /**
     * 业务类型：INSERT/UPDATE/DELETE/EXPORT/LOGIN/LOGOUT/OTHER
     */
    String businessType() default "";

    /**
     * 是否保存请求参数
     */
    boolean saveParam() default true;

    /**
     * 是否保存返回结果
     */
    boolean saveResult() default false;

}
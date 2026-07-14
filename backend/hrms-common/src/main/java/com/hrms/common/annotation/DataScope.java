package com.hrms.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>
 * 用于标记需要数据权限过滤的 Mapper 方法
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 部门字段名
     */
    String deptColumn() default "dept_id";

    /**
     * 创建者字段名
     */
    String createByColumn() default "create_by";

}

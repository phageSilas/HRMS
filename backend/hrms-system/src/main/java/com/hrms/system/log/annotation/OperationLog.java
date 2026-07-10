package com.hrms.system.log.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解。
 *
 * <p>标记在 Controller 方法上，用于自动记录操作日志。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * &#64;OperationLog(module = "用户管理", action = "创建用户")
 * public Result&lt;UserVO&gt; createUser(@RequestBody UserRequestDTO request) {
 *     // ...
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作模块。
     * 例如：用户管理、角色管理、部门管理等。
     *
     * @return 操作模块名称
     */
    String module();

    /**
     * 操作类型。
     * 例如：创建、更新、删除、查询、导出等。
     *
     * @return 操作类型名称
     */
    String action();

    /**
     * 是否记录请求参数。
     * 默认为 true。
     *
     * @return 是否记录请求参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录响应结果。
     * 默认为 false，避免记录大量数据。
     *
     * @return 是否记录响应结果
     */
    boolean recordResult() default false;

    /**
     * 是否记录异常信息。
     * 默认为 true。
     *
     * @return 是否记录异常信息
     */
    boolean recordException() default true;
}
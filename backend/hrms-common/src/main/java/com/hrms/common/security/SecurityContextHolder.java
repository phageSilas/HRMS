package com.hrms.common.security;

import java.util.List;

/**
 * 用户上下文持有者。
 *
 * <p>基于 ThreadLocal 实现线程隔离，存储当前请求关联的用户上下文信息。</p>
 *
 * <p>使用方式：</p>
 * <ul>
 *   <li>拦截器在请求开始时调用 {@link #setContext(UserContext)} 设置用户上下文</li>
 *   <li>业务代码通过 {@link #getUserId()}、{@link #getDeptId()}、{@link #getRoleIds()} 获取当前用户信息</li>
 *   <li>拦截器在请求结束时调用 {@link #clear()} 清理 ThreadLocal，避免内存泄漏</li>
 * </ul>
 *
 * <p>注意：定时任务或异步线程中调用时，getUserId() 等方法可能返回 null。</p>
 */
public final class SecurityContextHolder {

    /**
     * ThreadLocal 存储用户上下文。
     */
    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    /**
     * 私有构造器，禁止实例化。
     */
    private SecurityContextHolder() {
    }

    /**
     * 获取当前用户 ID。
     *
     * @return 当前用户 ID，未登录时返回 null
     */
    public static Long getUserId() {
        UserContext context = getContext();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取当前部门 ID。
     *
     * @return 当前部门 ID，未登录时返回 null
     */
    public static Long getDeptId() {
        UserContext context = getContext();
        return context != null ? context.getDeptId() : null;
    }

    /**
     * 获取当前角色 ID 列表。
     *
     * @return 当前角色 ID 列表，未登录时返回空列表
     */
    public static List<Long> getRoleIds() {
        UserContext context = getContext();
        return context != null ? context.getRoleIds() : List.of();
    }

    /**
     * 设置当前用户上下文。
     *
     * <p>通常由登录拦截器在请求开始时调用。</p>
     *
     * @param userContext 用户上下文对象
     */
    public static void setContext(UserContext userContext) {
        CONTEXT.set(userContext);
    }

    /**
     * 获取当前用户上下文对象。
     *
     * @return 用户上下文对象，未设置时返回 null
     */
    public static UserContext getContext() {
        return CONTEXT.get();
    }

    /**
     * 清理当前线程的用户上下文。
     *
     * <p>必须由拦截器在请求结束时调用，避免 ThreadLocal 内存泄漏。</p>
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
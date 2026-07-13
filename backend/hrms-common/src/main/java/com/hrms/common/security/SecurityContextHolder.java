package com.hrms.common.security;

import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;

import java.util.List;

/**
 * 安全上下文持有者（ThreadLocal）
 */
public class SecurityContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 获取用户上下文
     *
     * @return 用户上下文
     */
    public static UserContext getContext() {
        UserContext context = CONTEXT_HOLDER.get();
        if (context == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        return context;
    }

    /**
     * 设置用户上下文
     *
     * @param context 用户上下文
     */
    public static void setContext(UserContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 清除用户上下文
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return getContext().getUserId();
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        return getContext().getUsername();
    }

    /**
     * 获取部门ID
     *
     * @return 部门ID
     */
    public static Long getDeptId() {
        return getContext().getDeptId();
    }

    /**
     * 获取角色ID列表
     *
     * @return 角色ID列表
     */
    public static List<Long> getRoleIds() {
        return getContext().getRoleIds();
    }

    /**
     * 获取角色编码列表
     *
     * @return 角色编码列表
     */
    public static List<String> getRoleCodes() {
        return getContext().getRoleCodes();
    }

    /**
     * 获取权限码列表
     *
     * @return 权限码列表
     */
    public static List<String> getPermissions() {
        return getContext().getPermissions();
    }

    /**
     * 判断是否有权限
     *
     * @param permission 权限码
     * @return 是否有权限
     */
    public static boolean hasPermission(String permission) {
        return getContext().getPermissions().contains(permission);
    }

}

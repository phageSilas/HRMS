package com.hrms.common.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 数据权限工具类
 * <p>
 * 提供获取用户数据权限范围的公共方法
 * </p>
 */
@Slf4j
@Component
public class DataScopeUtils {

    /**
     * 数据权限范围：1=仅本人，2=本部门，3=本部门及下属，4=全部
     */
    public static final int DATA_SCOPE_SELF = 1;
    public static final int DATA_SCOPE_DEPT = 2;
    public static final int DATA_SCOPE_DEPT_AND_SUB = 3;
    public static final int DATA_SCOPE_ALL = 4;

    private static SqlSessionFactory sqlSessionFactory;

    @Lazy
    @Autowired
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        DataScopeUtils.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 获取当前用户的数据权限范围
     * <p>
     * 从用户角色中取最大的 data_scope 值（多角色时继承最宽的权限范围）
     * </p>
     *
     * @return 数据权限范围值
     */
    public static int getCurrentUserDataScope() {
        UserContext userContext = SecurityContextHolder.getContext();
        if (userContext == null) {
            return DATA_SCOPE_SELF;
        }
        return getUserDataScope(userContext);
    }

    /**
     * 获取用户的数据权限范围
     * <p>
     * 从用户角色中取最大的 data_scope 值（多角色时继承最宽的权限范围）
     * </p>
     *
     * @param userContext 用户上下文
     * @return 数据权限范围值
     */
    @SuppressWarnings("unchecked")
    public static int getUserDataScope(UserContext userContext) {
        // 判断是否为超级管理员
        if (isAdmin(userContext)) {
            return DATA_SCOPE_ALL;
        }

        List<?> rawRoleIds = userContext.getRoleIds();
        if (rawRoleIds == null || rawRoleIds.isEmpty()) {
            return DATA_SCOPE_SELF; // 默认仅本人
        }

        try {
            // 将 roleIds 转换为 Long 类型（兼容 Integer 和 Long）
            List<Long> roleIds = rawRoleIds.stream()
                    .map(item -> {
                        if (item instanceof Number) {
                            return ((Number) item).longValue();
                        }
                        return Long.parseLong(item.toString());
                    })
                    .toList();

            // 使用 SqlSession 直接查询角色的 data_scope
            // 取最大值（权限合并原则：多角色时继承最宽权限）
            String placeholders = String.join(",", Collections.nCopies(roleIds.size(), "?"));
            String sql = "SELECT MAX(data_scope) FROM sys_role WHERE id IN (" + placeholders + ") AND status = 1 AND is_deleted = 0";

            var session = sqlSessionFactory.openSession();
            try {
                var statement = session.getConnection().prepareStatement(sql);
                for (int i = 0; i < roleIds.size(); i++) {
                    statement.setLong(i + 1, roleIds.get(i));
                }
                var rs = statement.executeQuery();
                if (rs.next()) {
                    int dataScope = rs.getInt(1);
                    return dataScope > 0 ? dataScope : DATA_SCOPE_SELF;
                }
            } finally {
                session.close();
            }
        } catch (Exception e) {
            log.error("获取用户数据权限范围失败", e);
        }

        return DATA_SCOPE_SELF; // 默认仅本人
    }

    /**
     * 判断是否为超级管理员
     *
     * @param userContext 用户上下文
     * @return 是否为超级管理员
     */
    public static boolean isAdmin(UserContext userContext) {
        List<String> roleCodes = userContext.getRoleCodes();
        if (roleCodes != null) {
            return roleCodes.contains("ADMIN") || roleCodes.contains("ROLE_ADMIN");
        }
        return false;
    }

}

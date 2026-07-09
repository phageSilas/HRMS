package com.hrms.common.security;

import java.util.List;

/**
 * 用户上下文信息。
 *
 * <p>存储当前登录用户的关键信息，包括用户 ID、部门 ID 和角色 ID 列表。</p>
 *
 * <p>此类由 {@link SecurityContextHolder} 管理，通过 ThreadLocal 存储在线程上下文中。</p>
 */
public class UserContext {

    /**
     * 用户 ID。
     */
    private final Long userId;

    /**
     * 部门 ID。
     */
    private final Long deptId;

    /**
     * 角色 ID 列表。
     */
    private final List<Long> roleIds;

    /**
     * 创建用户上下文对象。
     *
     * @param userId  用户 ID
     * @param deptId  部门 ID
     * @param roleIds 角色 ID 列表
     */
    public UserContext(Long userId, Long deptId, List<Long> roleIds) {
        this.userId = userId;
        this.deptId = deptId;
        this.roleIds = roleIds != null ? roleIds : List.of();
    }

    /**
     * 获取用户 ID。
     *
     * @return 用户 ID，未登录时可能为 null
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取部门 ID。
     *
     * @return 部门 ID，未登录时可能为 null
     */
    public Long getDeptId() {
        return deptId;
    }

    /**
     * 获取角色 ID 列表。
     *
     * @return 角色 ID 列表，永不为 null（未登录时返回空列表）
     */
    public List<Long> getRoleIds() {
        return roleIds;
    }
}

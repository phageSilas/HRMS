package com.hrms.system.auth.vo;

import java.util.List;

/**
 * 权限信息 VO。
 */
public class PermissionVO {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 角色编码列表。
     */
    private List<String> roleCodes;

    /**
     * 权限标识列表。
     */
    private List<String> permissions;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
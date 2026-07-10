package com.hrms.system.auth.vo;

import java.util.List;

/**
 * 登录响应 VO。
 */
public class LoginVO {

    /**
     * JWT Token。
     */
    private String token;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 真实姓名。
     */
    private String realName;

    /**
     * 头像地址。
     */
    private String avatarUrl;

    /**
     * 角色 ID 列表。
     */
    private List<Long> roleIds;

    /**
     * 权限标识列表。
     */
    private List<String> permissions;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
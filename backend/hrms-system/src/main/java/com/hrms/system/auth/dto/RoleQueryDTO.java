package com.hrms.system.auth.dto;

/**
 * 角色查询条件 DTO。
 */
public class RoleQueryDTO {

    /**
     * 角色名称。
     */
    private String roleName;

    /**
     * 角色编码。
     */
    private String roleCode;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
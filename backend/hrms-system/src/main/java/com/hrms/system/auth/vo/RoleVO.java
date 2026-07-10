package com.hrms.system.auth.vo;

/**
 * 角色详情 VO。
 */
public class RoleVO {

    /**
     * 角色 ID。
     */
    private Long id;

    /**
     * 角色名称。
     */
    private String roleName;

    /**
     * 角色编码。
     */
    private String roleCode;

    /**
     * 数据权限范围：1-本人 2-本部门 3-本部门及子部门 4-全部。
     */
    private Integer dataScope;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

    /**
     * 排序号。
     */
    private Integer sortNo;

    /**
     * 备注。
     */
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getDataScope() {
        return dataScope;
    }

    public void setDataScope(Integer dataScope) {
        this.dataScope = dataScope;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
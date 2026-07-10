package com.hrms.system.auth.dto;

import java.util.List;

/**
 * 角色更新请求 DTO。
 */
public class RoleUpdateDTO {

    /**
     * 角色 ID。
     */
    private Long id;

    /**
     * 角色名称。
     */
    private String roleName;

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

    /**
     * 菜单 ID 列表。
     */
    private List<Long> menuIds;

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

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }
}
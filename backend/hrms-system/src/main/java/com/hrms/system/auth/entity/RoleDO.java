package com.hrms.system.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

/**
 * 角色实体。
 *
 * <p>对应数据库表 sys_role，存储角色信息和数据权限范围。</p>
 */
@TableName("sys_role")
public class RoleDO extends BaseEntity {

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
     * 版本号。
     */
    private Integer version;

    /**
     * 备注。
     */
    private String remark;

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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
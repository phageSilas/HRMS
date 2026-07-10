package com.hrms.system.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

/**
 * 角色菜单关联实体。
 *
 * <p>对应数据库表 sys_role_menu，存储角色与菜单的多对多关系。</p>
 */
@TableName("sys_role_menu")
public class RoleMenuDO extends BaseEntity {

    /**
     * 角色 ID。
     */
    private Long roleId;

    /**
     * 菜单 ID。
     */
    private Long menuId;

    /**
     * 版本号。
     */
    private Integer version;

    /**
     * 备注。
     */
    private String remark;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
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
package com.hrms.system.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

/**
 * 菜单实体。
 *
 * <p>对应数据库表 sys_menu，存储菜单、按钮等权限资源。</p>
 */
@TableName("sys_menu")
public class MenuDO extends BaseEntity {

    /**
     * 父级菜单 ID。
     */
    private Long parentId;

    /**
     * 菜单名称。
     */
    private String menuName;

    /**
     * 菜单类型：1-目录 2-菜单 3-按钮。
     */
    private Integer menuType;

    /**
     * 路由路径。
     */
    private String path;

    /**
     * 组件路径。
     */
    private String component;

    /**
     * 权限标识。
     */
    private String permission;

    /**
     * 图标。
     */
    private String icon;

    /**
     * 排序号。
     */
    private Integer sortNo;

    /**
     * 是否可见。
     */
    private Integer visible;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

    /**
     * 版本号。
     */
    private Integer version;

    /**
     * 备注。
     */
    private String remark;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public Integer getMenuType() {
        return menuType;
    }

    public void setMenuType(Integer menuType) {
        this.menuType = menuType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
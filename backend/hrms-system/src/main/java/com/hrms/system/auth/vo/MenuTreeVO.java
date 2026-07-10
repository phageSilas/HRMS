package com.hrms.system.auth.vo;

import java.util.List;

/**
 * 菜单树形结构 VO。
 */
public class MenuTreeVO {

    /**
     * 菜单 ID。
     */
    private Long id;

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
     * 是否可见：1是 0否。
     */
    private Integer visible;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

    /**
     * 子菜单列表。
     */
    private List<MenuTreeVO> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<MenuTreeVO> getChildren() {
        return children;
    }

    public void setChildren(List<MenuTreeVO> children) {
        this.children = children;
    }
}

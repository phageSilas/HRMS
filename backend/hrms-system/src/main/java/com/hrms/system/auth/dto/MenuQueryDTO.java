package com.hrms.system.auth.dto;

/**
 * 菜单查询条件 DTO。
 */
public class MenuQueryDTO {

    /**
     * 菜单名称（模糊查询）。
     */
    private String menuName;

    /**
     * 菜单类型：1-目录 2-菜单 3-按钮。
     */
    private Integer menuType;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

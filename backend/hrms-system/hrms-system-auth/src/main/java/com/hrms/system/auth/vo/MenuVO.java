package com.hrms.system.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单 VO
 */
@Data
@Schema(description = "菜单信息")
public class MenuVO {

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单标题")
    private String title;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "组件")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "子菜单列表")
    private List<MenuVO> children;

    // 手动添加 getter 和 setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<MenuVO> getChildren() {
        return children;
    }

    public void setChildren(List<MenuVO> children) {
        this.children = children;
    }

}

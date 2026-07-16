package com.hrms.system.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class MenuEntity extends BaseEntity {

    /**
     * 父级菜单 ID，0 表示顶级菜单
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型：1-目录，2-菜单，3-按钮
     */
    private Integer menuType;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 权限标识（如：system:user:list）
     */
    private String permission;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 是否可见：1-是，0-否
     */
    private Integer visible;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}

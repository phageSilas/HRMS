package com.hrms.system.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字段权限配置实体
 * 注意：sys_field_permission 表是配置表，不包含 BaseEntity 的全部公共字段，
 * 因此使用 @TableField(exist = false) 忽略不存在的字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_field_permission")
public class FieldPermissionEntity extends BaseEntity {

    /**
     * 业务类型：employee、salary、attendance 等
     */
    private String bizType;

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 字段描述
     */
    private String fieldDesc;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 是否可见：1-是 0-否
     */
    private Integer viewable;

    /**
     * 是否可编辑：1-是 0-否
     */
    private Integer editable;

    /**
     * 是否需审批：1-是 0-否
     */
    private Integer flowRequired;

    // 重写 BaseEntity 中的字段，标记为数据库中不存在
    @TableField(exist = false)
    private Long createBy;

    @TableField(exist = false)
    private Long updateBy;

    @TableField(exist = false)
    private Integer isDeleted;

    @TableField(exist = false)
    private Integer version;

}
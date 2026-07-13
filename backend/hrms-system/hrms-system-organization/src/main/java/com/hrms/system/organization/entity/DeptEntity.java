package com.hrms.system.organization.entity;

import com.hrms.common.entity.BaseEntity;
import lombok.Data;

/**
 * 部门实体
 */
@Data
public class DeptEntity extends BaseEntity {

    /**
     * 部门名称
     */
    private String name;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 部门编码
     */
    private String code;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;

}

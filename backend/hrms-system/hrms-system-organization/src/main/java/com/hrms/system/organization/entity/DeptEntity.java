package com.hrms.system.organization.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class DeptEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 上级部门 ID，0=根部门
     */
    private Long parentId;

    /**
     * 祖级路径（逗号分隔的祖先部门ID链）
     */
    private String ancestors;

    /**
     * 部门层级（根=1，最大=5）
     */
    private Integer deptLevel;

    /**
     * 部门负责人用户 ID
     */
    private Long leaderUserId;

    /**
     * 部门负责人员工 ID
     */
    private Long leaderEmployeeId;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 在职员工数缓存（含本部门及所有下属部门）
     */
    private Integer employeeCount;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}

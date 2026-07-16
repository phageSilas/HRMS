package com.hrms.system.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class RoleEntity extends BaseEntity {

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 数据权限范围：1-仅本人 2-本部门 3-本部门及子部门 4-全部
     */
    private Integer dataScope;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 备注
     */
    private String remark;

}

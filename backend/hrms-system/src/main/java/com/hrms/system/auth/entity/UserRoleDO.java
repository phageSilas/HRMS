package com.hrms.system.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

/**
 * 用户角色关联实体。
 *
 * <p>对应数据库表 sys_user_role，存储用户与角色的多对多关系。</p>
 */
@TableName("sys_user_role")
public class UserRoleDO extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 角色 ID。
     */
    private Long roleId;

    /**
     * 版本号。
     */
    private Integer version;

    /**
     * 备注。
     */
    private String remark;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
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
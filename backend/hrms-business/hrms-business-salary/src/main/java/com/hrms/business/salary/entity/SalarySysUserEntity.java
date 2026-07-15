package com.hrms.business.salary.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统用户只读实体，对齐 sys_user 表，用于工资条二次密码校验。
 */
@Data
@TableName("sys_user")
public class SalarySysUserEntity {

    @TableId
    private Long id;

    private String username;

    private String password;

    private Long employeeId;

    private Integer status;

    private Integer isDeleted;
}

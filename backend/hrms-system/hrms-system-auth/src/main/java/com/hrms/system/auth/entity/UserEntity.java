package com.hrms.system.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class UserEntity extends BaseEntity {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（BCrypt 加密）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 部门ID（冗余字段，避免跨模块查询）
     */
    private Long deptId;

    /**
     * 关联员工 ID
     */
    private Long employeeId;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录 IP
     */
    private String lastLoginIp;

    /**
     * 首次登录强制修改密码：1-是，0-否
     */
    private Integer needChangePassword;

    /**
     * 密码最后更新时间
     */
    private LocalDateTime passwordUpdateTime;

    /**
     * 连续登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 账号锁定时间
     */
    private LocalDateTime lockTime;

    /**
     * 备注
     */
    private String remark;

}

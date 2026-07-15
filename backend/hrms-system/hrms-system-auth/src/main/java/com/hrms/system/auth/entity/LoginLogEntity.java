package com.hrms.system.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 * 注意：sys_login_log 表是日志表，不包含 BaseEntity 的全部公共字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_login_log")
public class LoginLogEntity extends BaseEntity {

    /**
     * 登录用户ID
     */
    private Long userId;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录类型：ACCOUNT-账号登录 TOKEN-令牌登录
     */
    private String loginType;

    /**
     * 登录IP
     */
    private String ip;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录状态：1-成功 0-失败
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    // 重写 BaseEntity 中的字段，标记为数据库中不存在
    @TableField(exist = false)
    private Long createBy;

    @TableField(exist = false)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private Long updateBy;

    @TableField(exist = false)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Integer isDeleted;

    @TableField(exist = false)
    private Integer version;

}

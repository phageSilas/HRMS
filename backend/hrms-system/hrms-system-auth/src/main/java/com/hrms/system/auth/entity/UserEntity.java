package com.hrms.system.auth.entity;

import com.hrms.common.entity.BaseEntity;
import lombok.Data;

/**
 * 用户实体
 */
@Data
public class UserEntity extends BaseEntity {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;

}

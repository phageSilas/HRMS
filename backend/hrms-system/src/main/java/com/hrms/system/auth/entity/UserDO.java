package com.hrms.system.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 系统用户实体。
 *
 * <p>对应数据库表 sys_user，存储用户基本信息和认证凭据。</p>
 */
@TableName("sys_user")
public class UserDO extends BaseEntity {

    /**
     * 登录账号。
     */
    private String username;

    /**
     * 登录密码（BCrypt 加密）。
     */
    private String password;

    /**
     * 用户昵称。
     */
    private String nickname;

    /**
     * 真实姓名。
     */
    private String realName;

    /**
     * 手机号。
     */
    private String phone;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 头像地址。
     */
    private String avatarUrl;

    /**
     * 关联员工 ID。
     */
    private Long employeeId;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

    /**
     * 最后登录时间。
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录 IP。
     */
    private String lastLoginIp;

    /**
     * 是否首次登录强制修改密码。
     */
    private Integer needChangePassword;

    /**
     * 版本号。
     */
    private Integer version;

    /**
     * 备注。
     */
    private String remark;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public Integer getNeedChangePassword() {
        return needChangePassword;
    }

    public void setNeedChangePassword(Integer needChangePassword) {
        this.needChangePassword = needChangePassword;
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
package com.hrms.system.log.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

/**
 * 登录日志实体类。
 *
 * <p>对应数据库表 sys_login_log，记录用户登录行为。</p>
 */
@TableName("sys_login_log")
public class LoginLogDO extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 登录结果。
     * 0 表示失败，1 表示成功。
     */
    private Integer success;

    /**
     * 登录 IP 地址。
     */
    private String ipAddress;

    /**
     * 登录地点。
     * 根据 IP 地址解析的地理位置。
     */
    private String location;

    /**
     * 登录设备信息。
     * 例如浏览器类型、操作系统等。
     */
    private String deviceInfo;

    /**
     * 登录时间。
     */
    private java.time.LocalDateTime loginTime;

    /**
     * 失败原因。
     * 登录失败时记录原因（如：用户名不存在、密码错误等）。
     */
    private String failReason;

    /**
     * 获取用户 ID。
     *
     * @return 用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户 ID。
     *
     * @param userId 用户 ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名。
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取登录结果。
     *
     * @return 登录结果（0=失败，1=成功）
     */
    public Integer getSuccess() {
        return success;
    }

    /**
     * 设置登录结果。
     *
     * @param success 登录结果（0=失败，1=成功）
     */
    public void setSuccess(Integer success) {
        this.success = success;
    }

    /**
     * 获取登录 IP 地址。
     *
     * @return 登录 IP 地址
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * 设置登录 IP 地址。
     *
     * @param ipAddress 登录 IP 地址
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * 获取登录地点。
     *
     * @return 登录地点
     */
    public String getLocation() {
        return location;
    }

    /**
     * 设置登录地点。
     *
     * @param location 登录地点
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 获取登录设备信息。
     *
     * @return 登录设备信息
     */
    public String getDeviceInfo() {
        return deviceInfo;
    }

    /**
     * 设置登录设备信息。
     *
     * @param deviceInfo 登录设备信息
     */
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    /**
     * 获取登录时间。
     *
     * @return 登录时间
     */
    public java.time.LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * 设置登录时间。
     *
     * @param loginTime 登录时间
     */
    public void setLoginTime(java.time.LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    /**
     * 获取失败原因。
     *
     * @return 失败原因
     */
    public String getFailReason() {
        return failReason;
    }

    /**
     * 设置失败原因。
     *
     * @param failReason 失败原因
     */
    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}
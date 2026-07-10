package com.hrms.system.log.vo;

import java.time.LocalDateTime;

/**
 * 登录日志 VO。
 */
public class LoginLogVO {

    /**
     * 日志 ID。
     */
    private Long id;

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
     */
    private String location;

    /**
     * 登录设备信息。
     */
    private String deviceInfo;

    /**
     * 登录时间。
     */
    private LocalDateTime loginTime;

    /**
     * 失败原因。
     */
    private String failReason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}
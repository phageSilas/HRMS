package com.hrms.system.log.vo;

import java.time.LocalDateTime;

/**
 * 操作日志 VO。
 */
public class OperateLogVO {

    /**
     * 日志 ID。
     */
    private Long id;

    /**
     * 操作模块。
     */
    private String module;

    /**
     * 操作类型。
     */
    private String action;

    /**
     * 操作人 ID。
     */
    private Long operatorId;

    /**
     * 操作人用户名。
     */
    private String operatorName;

    /**
     * 请求 URL。
     */
    private String requestUrl;

    /**
     * 请求方法。
     */
    private String requestMethod;

    /**
     * 执行时长（毫秒）。
     */
    private Long duration;

    /**
     * 操作结果。
     * 0 表示失败，1 表示成功。
     */
    private Integer success;

    /**
     * 错误消息。
     */
    private String errorMessage;

    /**
     * 操作 IP 地址。
     */
    private String ipAddress;

    /**
     * 操作地点。
     */
    private String location;

    /**
     * 操作时间。
     */
    private LocalDateTime operateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    public LocalDateTime getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(LocalDateTime operateTime) {
        this.operateTime = operateTime;
    }
}
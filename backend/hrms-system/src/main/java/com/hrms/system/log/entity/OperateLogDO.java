package com.hrms.system.log.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hrms.common.entity.BaseEntity;

/**
 * 操作日志实体类。
 *
 * <p>对应数据库表 sys_operate_log，记录用户操作行为。</p>
 */
@TableName("sys_operate_log")
public class OperateLogDO extends BaseEntity {

    /**
     * 操作模块。
     * 例如：员工管理、角色管理、部门管理等。
     */
    private String module;

    /**
     * 操作类型。
     * 例如：创建、更新、删除、查询、导出等。
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
     * 操作方法名称。
     * 例如：UserController.create。
     */
    private String method;

    /**
     * 请求 URL。
     */
    private String requestUrl;

    /**
     * 请求方法。
     * 例如：GET、POST、PUT、DELETE。
     */
    private String requestMethod;

    /**
     * 请求参数（JSON 格式）。
     */
    private String requestParams;

    /**
     * 响应结果（JSON 格式）。
     */
    private String responseResult;

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
     * 操作失败时记录异常信息。
     */
    private String errorMessage;

    /**
     * 操作 IP 地址。
     */
    private String ipAddress;

    /**
     * 操作地点。
     * 根据 IP 地址解析的地理位置。
     */
    private String location;

    /**
     * 操作时间。
     */
    private java.time.LocalDateTime operateTime;

    /**
     * 获取操作模块。
     *
     * @return 操作模块
     */
    public String getModule() {
        return module;
    }

    /**
     * 设置操作模块。
     *
     * @param module 操作模块
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * 获取操作类型。
     *
     * @return 操作类型
     */
    public String getAction() {
        return action;
    }

    /**
     * 设置操作类型。
     *
     * @param action 操作类型
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * 获取操作人 ID。
     *
     * @return 操作人 ID
     */
    public Long getOperatorId() {
        return operatorId;
    }

    /**
     * 设置操作人 ID。
     *
     * @param operatorId 操作人 ID
     */
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    /**
     * 获取操作人用户名。
     *
     * @return 操作人用户名
     */
    public String getOperatorName() {
        return operatorName;
    }

    /**
     * 设置操作人用户名。
     *
     * @param operatorName 操作人用户名
     */
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    /**
     * 获取操作方法名称。
     *
     * @return 操作方法名称
     */
    public String getMethod() {
        return method;
    }

    /**
     * 设置操作方法名称。
     *
     * @param method 操作方法名称
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * 获取请求 URL。
     *
     * @return 请求 URL
     */
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * 设置请求 URL。
     *
     * @param requestUrl 请求 URL
     */
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    /**
     * 获取请求方法。
     *
     * @return 请求方法
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /**
     * 设置请求方法。
     *
     * @param requestMethod 请求方法
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * 获取请求参数。
     *
     * @return 请求参数（JSON 格式）
     */
    public String getRequestParams() {
        return requestParams;
    }

    /**
     * 设置请求参数。
     *
     * @param requestParams 请求参数（JSON 格式）
     */
    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    /**
     * 获取响应结果。
     *
     * @return 响应结果（JSON 格式）
     */
    public String getResponseResult() {
        return responseResult;
    }

    /**
     * 设置响应结果。
     *
     * @param responseResult 响应结果（JSON 格式）
     */
    public void setResponseResult(String responseResult) {
        this.responseResult = responseResult;
    }

    /**
     * 获取执行时长。
     *
     * @return 执行时长（毫秒）
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * 设置执行时长。
     *
     * @param duration 执行时长（毫秒）
     */
    public void setDuration(Long duration) {
        this.duration = duration;
    }

    /**
     * 获取操作结果。
     *
     * @return 操作结果（0=失败，1=成功）
     */
    public Integer getSuccess() {
        return success;
    }

    /**
     * 设置操作结果。
     *
     * @param success 操作结果（0=失败，1=成功）
     */
    public void setSuccess(Integer success) {
        this.success = success;
    }

    /**
     * 获取错误消息。
     *
     * @return 错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 设置错误消息。
     *
     * @param errorMessage 错误消息
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 获取操作 IP 地址。
     *
     * @return 操作 IP 地址
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * 设置操作 IP 地址。
     *
     * @param ipAddress 操作 IP 地址
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * 获取操作地点。
     *
     * @return 操作地点
     */
    public String getLocation() {
        return location;
    }

    /**
     * 设置操作地点。
     *
     * @param location 操作地点
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 获取操作时间。
     *
     * @return 操作时间
     */
    public java.time.LocalDateTime getOperateTime() {
        return operateTime;
    }

    /**
     * 设置操作时间。
     *
     * @param operateTime 操作时间
     */
    public void setOperateTime(java.time.LocalDateTime operateTime) {
        this.operateTime = operateTime;
    }
}
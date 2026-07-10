package com.hrms.system.log.dto;

import java.time.LocalDateTime;

/**
 * 操作日志查询条件 DTO。
 */
public class OperateLogQueryDTO {

    /**
     * 操作模块。
     */
    private String module;

    /**
     * 操作类型。
     */
    private String action;

    /**
     * 操作人用户名。
     */
    private String operatorName;

    /**
     * 操作结果。
     * 0 表示失败，1 表示成功。
     */
    private Integer success;

    /**
     * 开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    private LocalDateTime endTime;

    /**
     * 页码。
     */
    private Integer pageNum = 1;

    /**
     * 每页大小。
     */
    private Integer pageSize = 20;

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

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
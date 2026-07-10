package com.hrms.system.log.dto;

import java.time.LocalDateTime;

/**
 * 登录日志查询条件 DTO。
 */
public class LoginLogQueryDTO {

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
package com.hrms.common.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 定义系统统一分页查询参数。
 */
public class BasePageQuery {

    @Min(value = 1, message = "不能小于1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "不能小于1")
    @Max(value = 200, message = "不能大于200")
    private Long pageSize = 10L;

    /**
     * 获取当前页码。
     *
     * @return 当前页码
     */
    public Long getPageNum() {
        return pageNum;
    }

    /**
     * 设置当前页码。
     *
     * @param pageNum 当前页码
     */
    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * 获取每页条数。
     *
     * @return 每页条数
     */
    public Long getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页条数。
     *
     * @param pageSize 每页条数
     */
    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}

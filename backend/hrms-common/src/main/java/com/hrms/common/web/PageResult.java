package com.hrms.common.web;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回体
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码
     */
    private int pageNum;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 总页数
     */
    private int pages;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) (total / pageSize + (total % pageSize == 0 ? 0 : 1));
    }

    /**
     * 构造分页结果
     *
     * @param records 数据列表
     * @param total   总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param <T>     数据类型
     * @return PageResult
     */
    public static <T> PageResult<T> of(List<T> records, long total, int pageNum, int pageSize) {
        return new PageResult<>(records, total, pageNum, pageSize);
    }

}

package com.hrms.common.web;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装。
 *
 * @param <T> 数据类型
 */
public class PageResult<T> {

    /** 数据列表 */
    private final List<T> records;

    /** 总数 */
    private final long total;

    /** 当前页码 */
    private final int pageNum;

    /** 每页大小 */
    private final int pageSize;

    /**
     * 创建分页结果。
     *
     * @param records 数据列表
     * @param total 总数
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     */
    public PageResult(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records != null ? records : Collections.emptyList();
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    /**
     * 获取数据列表。
     *
     * @return 数据列表
     */
    public List<T> getRecords() {
        return records;
    }

    /**
     * 获取总数。
     *
     * @return 总数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 获取当前页码。
     *
     * @return 当前页码
     */
    public int getPageNum() {
        return pageNum;
    }

    /**
     * 获取每页大小。
     *
     * @return 每页大小
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 获取总页数。
     *
     * @return 总页数
     */
    public int getPages() {
        return pageSize == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 判断是否有下一页。
     *
     * @return 有下一页返回 true
     */
    public boolean hasNext() {
        return pageNum < getPages();
    }

    /**
     * 判断是否有上一页。
     *
     * @return 有上一页返回 true
     */
    public boolean hasPrevious() {
        return pageNum > 1;
    }
}
package com.hrms.common.model;

import java.util.List;

/**
 * 定义系统统一分页返回结构。
 *
 * @param <T> 分页记录类型
 */
public class PageResult<T> {

    private final long pageNum;
    private final long pageSize;
    private final long total;
    private final List<T> records;

    /**
     * 创建分页返回对象。
     *
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @param total 总记录数
     * @param records 当前页记录
     */
    private PageResult(long pageNum, long pageSize, long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
    }

    /**
     * 构建空分页返回对象。
     *
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @param <T> 分页记录类型
     * @return 分页返回对象
     */
    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return new PageResult<>(pageNum, pageSize, 0L, List.of());
    }

    /**
     * 构建分页返回对象。
     *
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @param total 总记录数
     * @param records 当前页记录
     * @param <T> 分页记录类型
     * @return 分页返回对象
     */
    public static <T> PageResult<T> of(long pageNum, long pageSize, long total, List<T> records) {
        return new PageResult<>(pageNum, pageSize, total, records);
    }

    /**
     * 获取当前页码。
     *
     * @return 当前页码
     */
    public long getPageNum() {
        return pageNum;
    }

    /**
     * 获取每页条数。
     *
     * @return 每页条数
     */
    public long getPageSize() {
        return pageSize;
    }

    /**
     * 获取总记录数。
     *
     * @return 总记录数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 获取当前页记录。
     *
     * @return 当前页记录
     */
    public List<T> getRecords() {
        return records;
    }
}

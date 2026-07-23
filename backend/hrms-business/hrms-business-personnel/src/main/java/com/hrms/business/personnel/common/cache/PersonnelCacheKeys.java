package com.hrms.business.personnel.common.cache;

import cn.hutool.core.util.StrUtil;

/**
 * 入转调离模块 Redis Key 生成器
 *
 * @author HRMS
 */
public final class PersonnelCacheKeys {

    private PersonnelCacheKeys() {
    }

    private static final String PREFIX = "personnel:";

    // ==================== 入职 ====================

    /**
     * 入职申请详情缓存 Key
     *
     * @param id 入职申请ID
     * @return Redis Key
     */
    public static String entryDetail(Long id) {
        return PREFIX + "entry:detail:" + id;
    }

    /**
     * 入职待办统计缓存 Key
     *
     * @param queryHash 查询条件 hash 值
     * @return Redis Key
     */
    public static String entryStats(int queryHash) {
        return PREFIX + "entry:stats:" + queryHash;
    }

    /**
     * 入职统计缓存 Key 模式
     *
     * @return Redis Key 模式
     */
    public static String entryStatsPattern() {
        return PREFIX + "entry:stats:*";
    }

    /**
     * 入职分页列表缓存 Key
     *
     * @param queryKey 规范化查询条件
     * @return Redis Key
     */
    public static String entryPage(String queryKey) {
        return PREFIX + "entry:page:" + normalizeQueryKey(queryKey);
    }

    /**
     * 入职分页缓存 Key 模式
     *
     * @return Redis Key 模式
     */
    public static String entryPagePattern() {
        return PREFIX + "entry:page:*";
    }

    /**
     * 入职提交防重 token Key
     *
     * @param id 入职申请ID
     * @return Redis Key
     */
    public static String entrySubmitToken(Long id) {
        return PREFIX + "entry:submit:token:" + id;
    }

    /**
     * 确认入职分布式锁 Key
     *
     * @param id 入职申请ID
     * @return Redis Key
     */
    public static String entryConfirmLock(Long id) {
        return "lock:" + PREFIX + "entry:confirm:" + id;
    }

    // ==================== 离职 ====================

    /**
     * 离职提交防重 token Key
     *
     * @param employeeId 员工ID
     * @return Redis Key
     */
    public static String leaveSubmitToken(Long employeeId) {
        return PREFIX + "leave:submit:token:" + employeeId;
    }

    /**
     * 离职分页列表缓存 Key
     *
     * @param queryHash 查询条件 hash 值
     * @return Redis Key
     */
    public static String leavePage(String queryHash) {
        return PREFIX + "leave:page:" + queryHash;
    }

    /**
     * 离职分页缓存 Key 模式
     *
     * @return Redis Key 模式
     */
    public static String leavePagePattern() {
        return PREFIX + "leave:page:*";
    }

    // ==================== 转正 ====================

    /**
     * 转正提交防重 token Key
     *
     * @param employeeId 员工ID
     * @return Redis Key
     */
    public static String regularSubmitToken(Long employeeId) {
        return PREFIX + "regular:submit:token:" + employeeId;
    }

    /**
     * 转正分页列表缓存 Key
     *
     * @param queryHash 查询条件 hash 值
     * @return Redis Key
     */
    public static String regularPage(String queryHash) {
        return PREFIX + "regular:page:" + queryHash;
    }

    /**
     * 转正分页缓存 Key 模式
     *
     * @return Redis Key 模式
     */
    public static String regularPagePattern() {
        return PREFIX + "regular:page:*";
    }

    // ==================== 调岗 ====================

    /**
     * 调岗提交防重 token Key
     *
     * @param employeeId 员工ID
     * @return Redis Key
     */
    public static String transferSubmitToken(Long employeeId) {
        return PREFIX + "transfer:submit:token:" + employeeId;
    }

    /**
     * 调岗分页列表缓存 Key
     *
     * @param queryHash 查询条件 hash 值
     * @return Redis Key
     */
    public static String transferPage(String queryHash) {
        return PREFIX + "transfer:page:" + queryHash;
    }

    /**
     * 调岗分页缓存 Key 模式
     *
     * @return Redis Key 模式
     */
    public static String transferPagePattern() {
        return PREFIX + "transfer:page:*";
    }

    /**
     * 规范化查询条件 Key，避免空值生成重复缓存。
     *
     * @param queryKey 原始查询条件
     * @return 规范化后的查询条件
     */
    private static String normalizeQueryKey(String queryKey) {
        return StrUtil.blankToDefault(queryKey, "blank");
    }
}

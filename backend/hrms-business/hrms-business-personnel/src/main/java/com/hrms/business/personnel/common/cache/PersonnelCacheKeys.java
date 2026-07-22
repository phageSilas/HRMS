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
}
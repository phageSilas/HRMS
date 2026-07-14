package com.hrms.business.salary.cache;

/**
 * 薪资模块 Redis Key 生成器。
 */
public final class SalaryCacheKeys {

    private SalaryCacheKeys() {
    }

    /**
     * 获取薪资账套缓存 Key。
     *
     * @param templateId 账套ID
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String template(Long templateId) {
        return "salary:template:" + templateId;
    }

    /**
     * 获取账套项目缓存 Key。
     *
     * @param templateId 账套ID
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String templateItems(Long templateId) {
        return "salary:template:items:" + templateId;
    }

    /**
     * 获取批次预览缓存 Key。
     *
     * @param batchId 批次ID
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String batchPreview(Long batchId) {
        return "salary:batch:preview:" + batchId;
    }

    /**
     * 获取批次核算锁 Key。
     *
     * @param batchId 批次ID
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String calculateLock(Long batchId) {
        return "lock:salary:batch:calculate:" + batchId;
    }

    /**
     * 获取工资条二次验证 Key。
     *
     * @param employeeId 员工ID
     * @param month      薪资月份
     * @return Redis Key
     * 本方法使用的工具类: 无
     */
    public static String payslipVerify(Long employeeId, String month) {
        return "salary:payslip:verify:" + employeeId + ":" + month;
    }
}

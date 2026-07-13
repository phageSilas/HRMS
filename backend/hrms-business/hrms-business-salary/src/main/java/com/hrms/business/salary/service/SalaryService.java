package com.hrms.business.salary.service;

/**
 * 薪资管理服务接口
 */
public interface SalaryService {

    /**
     * 获取员工薪资档案
     *
     * @param employeeId 员工ID
     * @return 薪资档案
     */
    Object getSalaryProfile(Long employeeId);

    /**
     * 创建薪资批次
     *
     * @param year  年份
     * @param month 月份
     * @return 批次ID
     */
    Long createSalaryBatch(Integer year, Integer month);

    /**
     * 计算薪资
     *
     * @param batchId 批次ID
     */
    void calculateSalary(Long batchId);

}

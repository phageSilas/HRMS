package com.hrms.business.employee.service;

/**
 * 员工服务接口
 */
public interface EmployeeService {

    /**
     * 获取员工列表
     *
     * @return 员工列表
     */
    Object list();

    /**
     * 获取员工详情
     *
     * @param id 员工ID
     * @return 员工详情
     */
    Object get(Long id);

    /**
     * 生成员工工号
     *
     * @return 工号
     */
    String generateEmployeeNo();

}

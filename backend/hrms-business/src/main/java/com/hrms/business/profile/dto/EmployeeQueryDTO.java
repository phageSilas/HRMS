package com.hrms.business.profile.dto;

import com.hrms.common.model.BasePageQuery;

/**
 * 员工档案分页查询参数。
 */
public class EmployeeQueryDTO extends BasePageQuery {

    private String employeeName;
    private Long departmentId;
    private String status;

    /**
     * 获取员工姓名。
     *
     * @return 员工姓名
     */
    public String getEmployeeName() {
        return employeeName;
    }

    /**
     * 设置员工姓名。
     *
     * @param employeeName 员工姓名
     */
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    /**
     * 获取所属部门ID。
     *
     * @return 所属部门ID
     */
    public Long getDepartmentId() {
        return departmentId;
    }

    /**
     * 设置所属部门ID。
     *
     * @param departmentId 所属部门ID
     */
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    /**
     * 获取员工在职状态。
     *
     * @return 员工在职状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置员工在职状态。
     *
     * @param status 员工在职状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
}

package com.hrms.business.profile.vo;

/**
 * 员工简要信息返回对象。
 *
 * @param id 员工ID
 * @param name 员工姓名
 * @param employeeNo 员工工号
 * @param departmentId 所属部门ID
 * @param departmentName 所属部门名称
 * @param status 员工状态
 */
public record EmployeeBriefVO(
    Long id,
    String name,
    String employeeNo,
    Long departmentId,
    String departmentName,
    String status
) {
}

package com.hrms.business.personnel.service;

/**
 * 入转调离服务接口
 */
public interface PersonnelService {

    /**
     * 发起入职申请
     *
     * @param employeeId 员工ID
     * @return 审批实例ID
     */
    Long startEntryApproval(Long employeeId);

    /**
     * 发起转正申请
     *
     * @param employeeId 员工ID
     * @return 审批实例ID
     */
    Long startRegularApproval(Long employeeId);

    /**
     * 发起调岗申请
     *
     * @param employeeId 员工ID
     * @param newDeptId 新部门ID
     * @param newPostId 新职位ID
     * @return 审批实例ID
     */
    Long startTransferApproval(Long employeeId, Long newDeptId, Long newPostId);

    /**
     * 发起离职申请
     *
     * @param employeeId 员工ID
     * @return 审批实例ID
     */
    Long startResignApproval(Long employeeId);

}

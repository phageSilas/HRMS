package com.hrms.business.personnel.convert;

import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.LeaveApplicationEntity;
import com.hrms.business.personnel.common.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.common.enums.LeaveTypeEnum;
import com.hrms.business.personnel.vo.LeaveApplicationPageVO;

/**
 * 离职申请转换器
 */
public final class LeaveApplicationConvert {

    private LeaveApplicationConvert() {
    }

    /**
     * 将离职申请实体转换为分页 VO。
     *
     * @param entity 离职申请实体
     * @param employeeSnapshot 员工快照
     * @param handoverSnapshot 交接人员工快照
     * @return 离职申请分页 VO
     * 本方法使用的工具类: 无
     */
    public static LeaveApplicationPageVO toPageVO(LeaveApplicationEntity entity,
                                                  EmployeeSnapshotEntity employeeSnapshot,
                                                  EmployeeSnapshotEntity handoverSnapshot) {
        return LeaveApplicationPageVO.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployeeId())
                .employeeName(employeeSnapshot == null ? null : employeeSnapshot.getEmployeeName())
                .employeeNo(employeeSnapshot == null ? null : employeeSnapshot.getEmployeeNo())
                .departmentName(employeeSnapshot == null ? null : tempResolveDeptName(employeeSnapshot.getDeptId()))
                .leaveType(LeaveTypeEnum.getValueByCode(entity.getLeaveType()))
                .leaveTypeName(LeaveTypeEnum.getDescByCode(entity.getLeaveType()))
                .lastWorkDate(entity.getLastWorkDate() == null ? entity.getExpectedLastWorkDate() : entity.getLastWorkDate())
                .leaveDate(entity.getLastWorkDate() == null ? entity.getExpectedLastWorkDate() : entity.getLastWorkDate())
                .handoverEmployeeName(handoverSnapshot == null ? null : handoverSnapshot.getEmployeeName())
                .reason(entity.getLeaveReason())
                .approvalStatus(entity.getApprovalStatus())
                .approvalStatusDesc(ApplicationStatusEnum.getDescByCode(entity.getApprovalStatus()))
                .createTime(entity.getCreateTime())
                .build();
    }

    /**
     * 临时解析部门名称。
     *
     * @param deptId 部门ID
     * @return 部门名称
     * 本方法使用的工具类: 无
     */
    private static String tempResolveDeptName(Long deptId) {
        return null;
    }

}

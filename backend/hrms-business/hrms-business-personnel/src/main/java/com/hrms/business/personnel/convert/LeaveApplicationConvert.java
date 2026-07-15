package com.hrms.business.personnel.convert;

import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.LeaveApplicationEntity;
import com.hrms.business.personnel.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.enums.LeaveTypeEnum;
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
        LeaveApplicationPageVO vo = new LeaveApplicationPageVO();
        vo.setId(entity.getId());
        vo.setEmployeeId(entity.getEmployeeId());
        if (employeeSnapshot != null) {
            vo.setEmployeeName(employeeSnapshot.getEmployeeName());
            vo.setDepartmentName(tempResolveDeptName(employeeSnapshot.getDeptId()));
        }
        vo.setLeaveType(LeaveTypeEnum.getValueByCode(entity.getLeaveType()));
        vo.setLeaveTypeName(LeaveTypeEnum.getDescByCode(entity.getLeaveType()));
        vo.setLastWorkDate(entity.getLastWorkDate() == null ? entity.getExpectedLastWorkDate() : entity.getLastWorkDate());
        vo.setLeaveDate(entity.getLastWorkDate() == null ? entity.getExpectedLastWorkDate() : entity.getLastWorkDate());
        vo.setHandoverEmployeeName(handoverSnapshot == null ? null : handoverSnapshot.getEmployeeName());
        vo.setReason(entity.getLeaveReason());
        vo.setApprovalStatus(entity.getApprovalStatus());
        vo.setApprovalStatusDesc(ApplicationStatusEnum.getDescByCode(entity.getApprovalStatus()));
        vo.setCreateTime(entity.getCreateTime());
        return vo;
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

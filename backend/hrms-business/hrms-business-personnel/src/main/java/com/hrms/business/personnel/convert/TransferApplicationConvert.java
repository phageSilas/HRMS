package com.hrms.business.personnel.convert;

import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.TransferApplicationEntity;
import com.hrms.business.personnel.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.vo.TransferApplicationPageVO;

/**
 * 调岗申请转换器
 */
public final class TransferApplicationConvert {

    private TransferApplicationConvert() {
    }

    /**
     * 将调岗申请实体转换为分页 VO。
     *
     * @param entity 调岗申请实体
     * @param employeeSnapshot 员工快照
     * @return 调岗申请分页 VO
     * 本方法使用的工具类: 无
     */
    public static TransferApplicationPageVO toPageVO(TransferApplicationEntity entity,
                                                     EmployeeSnapshotEntity employeeSnapshot) {
        return TransferApplicationPageVO.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployeeId())
                .employeeName(employeeSnapshot == null ? null : employeeSnapshot.getEmployeeName())
                .employeeNo(employeeSnapshot == null ? null : employeeSnapshot.getEmployeeNo())
                .fromDeptName(tempResolveDeptName(entity.getFromDeptId()))
                .fromPostName(tempResolvePostName(entity.getFromPostId()))
                .toDeptName(tempResolveDeptName(entity.getToDeptId()))
                .toPostName(tempResolvePostName(entity.getToPostId()))
                .effectiveDate(entity.getEffectiveDate())
                .reason(entity.getReason())
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

    /**
     * 临时解析岗位名称。
     *
     * @param postId 岗位ID
     * @return 岗位名称
     * 本方法使用的工具类: 无
     */
    private static String tempResolvePostName(Long postId) {
        return null;
    }

}

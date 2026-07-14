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
        TransferApplicationPageVO vo = new TransferApplicationPageVO();
        vo.setId(entity.getId());
        vo.setEmployeeId(entity.getEmployeeId());
        if (employeeSnapshot != null) {
            vo.setEmployeeName(employeeSnapshot.getEmployeeName());
            vo.setEmployeeNo(employeeSnapshot.getEmployeeNo());
        }
        vo.setFromDeptName(tempResolveDeptName(entity.getFromDeptId()));
        vo.setFromPostName(tempResolvePostName(entity.getFromPostId()));
        vo.setToDeptName(tempResolveDeptName(entity.getToDeptId()));
        vo.setToPostName(tempResolvePostName(entity.getToPostId()));
        vo.setEffectiveDate(entity.getEffectiveDate());
        vo.setReason(entity.getReason());
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

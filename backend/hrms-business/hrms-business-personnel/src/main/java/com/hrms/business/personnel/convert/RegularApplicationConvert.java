package com.hrms.business.personnel.convert;

import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.RegularApplicationEntity;
import com.hrms.business.personnel.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.vo.RegularApplicationPageVO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 转正申请转换器
 */
public final class RegularApplicationConvert {

    private static final String EVALUATION_PENDING = "pending";

    private static final String EVALUATION_EVALUATED = "evaluated";

    private RegularApplicationConvert() {
    }

    /**
     * 将试用期员工快照转换为待转正分页 VO。
     *
     * @param employeeSnapshot 员工快照
     * @return 转正分页 VO
     * 本方法使用的工具类: ChronoUnit(JDK)
     */
    public static RegularApplicationPageVO toPendingVO(EmployeeSnapshotEntity employeeSnapshot) {
        RegularApplicationPageVO vo = new RegularApplicationPageVO();
        vo.setEmployeeId(employeeSnapshot.getId());
        vo.setEmployeeName(employeeSnapshot.getEmployeeName());
        vo.setEmployeeNo(employeeSnapshot.getEmployeeNo());
        vo.setDeptId(employeeSnapshot.getDeptId());
        vo.setDepartmentName(tempResolveDeptName(employeeSnapshot.getDeptId()));
        vo.setPostId(employeeSnapshot.getPostId());
        vo.setPositionName(tempResolvePostName(employeeSnapshot.getPostId()));
        vo.setHireDate(employeeSnapshot.getHireDate());
        LocalDate probationEndDate = calculateProbationEndDate(employeeSnapshot);
        vo.setProbationEndDate(probationEndDate);
        vo.setRemainingDays(probationEndDate == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), probationEndDate));
        vo.setEvaluationStatus(EVALUATION_PENDING);
        vo.setApprovalStatus(ApplicationStatusEnum.DRAFT.getCode());
        vo.setApprovalStatusDesc(ApplicationStatusEnum.DRAFT.getDesc());
        vo.setCreateTime(employeeSnapshot.getCreateTime());
        return vo;
    }

    /**
     * 将转正申请实体转换为已评估分页 VO。
     *
     * @param entity 转正申请实体
     * @param employeeSnapshot 员工快照
     * @return 转正分页 VO
     * 本方法使用的工具类: ChronoUnit(JDK)
     */
    public static RegularApplicationPageVO toEvaluatedVO(RegularApplicationEntity entity,
                                                         EmployeeSnapshotEntity employeeSnapshot) {
        RegularApplicationPageVO vo = new RegularApplicationPageVO();
        vo.setId(entity.getId());
        vo.setEmployeeId(entity.getEmployeeId());
        if (employeeSnapshot != null) {
            vo.setEmployeeName(employeeSnapshot.getEmployeeName());
            vo.setEmployeeNo(employeeSnapshot.getEmployeeNo());
            vo.setDeptId(employeeSnapshot.getDeptId());
            vo.setDepartmentName(tempResolveDeptName(employeeSnapshot.getDeptId()));
            vo.setPostId(employeeSnapshot.getPostId());
            vo.setPositionName(tempResolvePostName(employeeSnapshot.getPostId()));
            vo.setHireDate(employeeSnapshot.getHireDate());
        }
        vo.setProbationEndDate(entity.getProbationEndDate());
        vo.setRemainingDays(entity.getProbationEndDate() == null
                ? null
                : ChronoUnit.DAYS.between(LocalDate.now(), entity.getProbationEndDate()));
        vo.setEvaluationStatus(EVALUATION_EVALUATED);
        vo.setApprovalStatus(entity.getApprovalStatus());
        vo.setApprovalStatusDesc(ApplicationStatusEnum.getDescByCode(entity.getApprovalStatus()));
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    /**
     * 计算试用期结束日期。
     *
     * @param employeeSnapshot 员工快照
     * @return 试用期结束日期
     * 本方法使用的工具类: 无
     */
    private static LocalDate calculateProbationEndDate(EmployeeSnapshotEntity employeeSnapshot) {
        if (employeeSnapshot.getHireDate() == null) {
            return null;
        }
        int probationMonth = employeeSnapshot.getProbationMonth() == null ? 0 : employeeSnapshot.getProbationMonth();
        return employeeSnapshot.getHireDate().plusMonths(probationMonth);
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

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

    private static final String PENDING_APPROVAL_DESC = "待审批";

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
        return toPendingVO(employeeSnapshot, null);
    }

    /**
     * 将试用期员工快照转换为待转正分页 VO，并在存在进行中申请时回填真实审批状态。
     *
     * @param employeeSnapshot 员工快照
     * @param regularApplication 进行中的转正申请，可为空
     * @return 转正分页 VO
     * 本方法使用的工具类: ChronoUnit(JDK)
     */
    public static RegularApplicationPageVO toPendingVO(EmployeeSnapshotEntity employeeSnapshot,
                                                       RegularApplicationEntity regularApplication) {
        LocalDate probationEndDate = calculateProbationEndDate(employeeSnapshot);
        Integer approvalStatus = regularApplication == null
                ? ApplicationStatusEnum.DRAFT.getCode()
                : regularApplication.getApprovalStatus();
        return RegularApplicationPageVO.builder()
                .id(regularApplication == null ? null : regularApplication.getId())
                .employeeId(employeeSnapshot.getId())
                .employeeName(employeeSnapshot.getEmployeeName())
                .employeeNo(employeeSnapshot.getEmployeeNo())
                .deptId(employeeSnapshot.getDeptId())
                .departmentName(tempResolveDeptName(employeeSnapshot.getDeptId()))
                .postId(employeeSnapshot.getPostId())
                .positionName(tempResolvePostName(employeeSnapshot.getPostId()))
                .hireDate(employeeSnapshot.getHireDate())
                .probationEndDate(probationEndDate)
                .remainingDays(probationEndDate == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), probationEndDate))
                .evaluationStatus(EVALUATION_PENDING)
                .approvalStatus(approvalStatus)
                .approvalStatusDesc(regularApplication == null
                        ? PENDING_APPROVAL_DESC
                        : ApplicationStatusEnum.getDescByCode(approvalStatus))
                .createTime(regularApplication == null ? employeeSnapshot.getCreateTime() : regularApplication.getCreateTime())
                .build();
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
        return RegularApplicationPageVO.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployeeId())
                .employeeName(employeeSnapshot == null ? null : employeeSnapshot.getEmployeeName())
                .employeeNo(employeeSnapshot == null ? null : employeeSnapshot.getEmployeeNo())
                .deptId(employeeSnapshot == null ? null : employeeSnapshot.getDeptId())
                .departmentName(employeeSnapshot == null ? null : tempResolveDeptName(employeeSnapshot.getDeptId()))
                .postId(employeeSnapshot == null ? null : employeeSnapshot.getPostId())
                .positionName(employeeSnapshot == null ? null : tempResolvePostName(employeeSnapshot.getPostId()))
                .hireDate(employeeSnapshot == null ? null : employeeSnapshot.getHireDate())
                .probationEndDate(entity.getProbationEndDate())
                .remainingDays(entity.getProbationEndDate() == null
                        ? null
                        : ChronoUnit.DAYS.between(LocalDate.now(), entity.getProbationEndDate()))
                .evaluationStatus(EVALUATION_EVALUATED)
                .approvalStatus(entity.getApprovalStatus())
                .approvalStatusDesc(ApplicationStatusEnum.getDescByCode(entity.getApprovalStatus()))
                .createTime(entity.getCreateTime())
                .build();
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

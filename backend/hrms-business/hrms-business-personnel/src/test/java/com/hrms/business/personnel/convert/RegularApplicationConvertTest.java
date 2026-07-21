package com.hrms.business.personnel.convert;

import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.RegularApplicationEntity;
import com.hrms.business.personnel.common.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.vo.RegularApplicationPageVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 转正申请转换器测试
 */
class RegularApplicationConvertTest {

    /**
     * 验证待转正员工存在审批中申请时，待转正列表也返回审批中状态。
     *
     * 本方法使用的工具类: LocalDate(JDK),LocalDateTime(JDK)
     */
    @Test
    void shouldUseProcessingApplicationStatusForPendingVo() {
        EmployeeSnapshotEntity employeeSnapshot = new EmployeeSnapshotEntity();
        employeeSnapshot.setId(1001L);
        employeeSnapshot.setEmployeeName("孙可心");
        employeeSnapshot.setEmployeeNo("EMP24003");
        employeeSnapshot.setDeptId(2L);
        employeeSnapshot.setPostId(102L);
        employeeSnapshot.setHireDate(LocalDate.of(2024, 4, 4));
        employeeSnapshot.setProbationMonth(3);
        employeeSnapshot.setCreateTime(LocalDateTime.of(2026, 7, 18, 16, 52));

        RegularApplicationEntity processingApplication = new RegularApplicationEntity();
        processingApplication.setId(9001L);
        processingApplication.setEmployeeId(1001L);
        processingApplication.setApprovalStatus(ApplicationStatusEnum.APPROVING.getCode());
        processingApplication.setCreateTime(LocalDateTime.of(2026, 7, 18, 18, 0));

        RegularApplicationPageVO vo = RegularApplicationConvert.toPendingVO(employeeSnapshot, processingApplication);

        assertEquals(9001L, vo.getId());
        assertEquals(ApplicationStatusEnum.APPROVING.getCode(), vo.getApprovalStatus());
        assertEquals(ApplicationStatusEnum.APPROVING.getDesc(), vo.getApprovalStatusDesc());
        assertEquals(LocalDateTime.of(2026, 7, 18, 18, 0), vo.getCreateTime());
    }

    /**
     * 验证默认待转正文案显示为待审批，而不是草稿。
     *
     * 本方法使用的工具类: LocalDate(JDK),LocalDateTime(JDK)
     */
    @Test
    void shouldUsePendingApprovalDescWhenNoRegularApplicationExists() {
        EmployeeSnapshotEntity employeeSnapshot = new EmployeeSnapshotEntity();
        employeeSnapshot.setId(1002L);
        employeeSnapshot.setEmployeeName("郑雨桐");
        employeeSnapshot.setEmployeeNo("EMP24006");
        employeeSnapshot.setHireDate(LocalDate.of(2024, 7, 7));
        employeeSnapshot.setProbationMonth(3);
        employeeSnapshot.setCreateTime(LocalDateTime.of(2026, 7, 18, 16, 52));

        RegularApplicationPageVO vo = RegularApplicationConvert.toPendingVO(employeeSnapshot);

        assertEquals(ApplicationStatusEnum.DRAFT.getCode(), vo.getApprovalStatus());
        assertEquals("待审批", vo.getApprovalStatusDesc());
    }
}

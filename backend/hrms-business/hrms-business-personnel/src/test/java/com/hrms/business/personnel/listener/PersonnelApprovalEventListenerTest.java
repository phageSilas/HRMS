package com.hrms.business.personnel.listener;

import com.hrms.business.approval.enums.ApprovalStatusEnum;
import com.hrms.business.approval.enums.ApprovalTypeEnum;
import com.hrms.business.approval.service.event.ApprovalCompletedEvent;
import com.hrms.business.employee.dto.EmployeeApprovalSyncUpdateDTO;
import com.hrms.business.employee.enums.EmploymentStatusEnum;
import com.hrms.business.employee.service.EmployeeService;
import com.hrms.business.personnel.common.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.common.enums.RegularEvaluateResultEnum;
import com.hrms.business.personnel.entity.LeaveApplicationEntity;
import com.hrms.business.personnel.entity.RegularApplicationEntity;
import com.hrms.business.personnel.entity.TransferApplicationEntity;
import com.hrms.business.personnel.mapper.LeaveApplicationMapper;
import com.hrms.business.personnel.mapper.RegularApplicationMapper;
import com.hrms.business.personnel.mapper.TransferApplicationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 人员审批完成事件监听器测试
 */
@ExtendWith(MockitoExtension.class)
class PersonnelApprovalEventListenerTest {

    @Mock
    private RegularApplicationMapper regularApplicationMapper;

    @Mock
    private TransferApplicationMapper transferApplicationMapper;

    @Mock
    private LeaveApplicationMapper leaveApplicationMapper;

    @Mock
    private EmployeeService employeeService;

    private PersonnelApprovalEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new PersonnelApprovalEventListener(
                regularApplicationMapper,
                transferApplicationMapper,
                leaveApplicationMapper,
                employeeService
        );
    }

    /**
     * 转正审批通过后应同步申请状态、转正日期和员工正式状态。
     */
    @Test
    void shouldSyncRegularApplicationAndEmployeeWhenApproved() {
        RegularApplicationEntity entity = new RegularApplicationEntity();
        entity.setId(32001L);
        entity.setEmployeeId(25001L);
        entity.setEvaluateResult(RegularEvaluateResultEnum.PASS.getCode());
        when(regularApplicationMapper.selectById(32001L)).thenReturn(entity);

        listener.handleApprovalCompleted(new ApprovalCompletedEvent(
                932001L,
                ApprovalTypeEnum.REGULAR.getCode(),
                32001L,
                ApprovalStatusEnum.APPROVED.getCode()
        ));

        ArgumentCaptor<RegularApplicationEntity> applicationCaptor = ArgumentCaptor.forClass(RegularApplicationEntity.class);
        verify(regularApplicationMapper).updateById(applicationCaptor.capture());
        assertEquals(ApplicationStatusEnum.APPROVED.getCode(), applicationCaptor.getValue().getApprovalStatus());
        assertNotNull(applicationCaptor.getValue().getRegularDate());

        ArgumentCaptor<EmployeeApprovalSyncUpdateDTO> updateCaptor = ArgumentCaptor.forClass(EmployeeApprovalSyncUpdateDTO.class);
        verify(employeeService).syncEmployeeForApproval(org.mockito.ArgumentMatchers.eq(25001L), updateCaptor.capture());
        assertEquals(EmploymentStatusEnum.FORMAL.getCode(), updateCaptor.getValue().getEmploymentStatus());
    }

    /**
     * 转正非通过结果即使审批通过，也不应强制同步员工正式状态。
     */
    @Test
    void shouldOnlyUpdateRegularApplicationWhenEvaluateResultIsNotPass() {
        RegularApplicationEntity entity = new RegularApplicationEntity();
        entity.setId(32002L);
        entity.setEmployeeId(25002L);
        entity.setEvaluateResult(RegularEvaluateResultEnum.EXTEND.getCode());
        when(regularApplicationMapper.selectById(32002L)).thenReturn(entity);

        listener.handleApprovalCompleted(new ApprovalCompletedEvent(
                932002L,
                ApprovalTypeEnum.REGULAR.getCode(),
                32002L,
                ApprovalStatusEnum.APPROVED.getCode()
        ));

        verify(regularApplicationMapper).updateById(entity);
        assertEquals(ApplicationStatusEnum.APPROVED.getCode(), entity.getApprovalStatus());
        verify(employeeService, never()).syncEmployeeForApproval(any(), any());
    }

    /**
     * 调岗审批通过后应同步员工组织信息。
     */
    @Test
    void shouldSyncTransferApplicationAndEmployeeWhenApproved() {
        TransferApplicationEntity entity = new TransferApplicationEntity();
        entity.setId(33001L);
        entity.setEmployeeId(25003L);
        entity.setToDeptId(2310L);
        entity.setToPostId(3008L);
        entity.setToJobLevel("P6");
        entity.setToLeaderId(22006L);
        when(transferApplicationMapper.selectById(33001L)).thenReturn(entity);

        listener.handleApprovalCompleted(new ApprovalCompletedEvent(
                933001L,
                ApprovalTypeEnum.TRANSFER.getCode(),
                33001L,
                ApprovalStatusEnum.APPROVED.getCode()
        ));

        verify(transferApplicationMapper).updateById(entity);
        assertEquals(ApplicationStatusEnum.APPROVED.getCode(), entity.getApprovalStatus());

        ArgumentCaptor<EmployeeApprovalSyncUpdateDTO> updateCaptor = ArgumentCaptor.forClass(EmployeeApprovalSyncUpdateDTO.class);
        verify(employeeService).syncEmployeeForApproval(org.mockito.ArgumentMatchers.eq(25003L), updateCaptor.capture());
        assertEquals(2310L, updateCaptor.getValue().getDeptId());
        assertEquals(3008L, updateCaptor.getValue().getPostId());
        assertEquals("P6", updateCaptor.getValue().getJobLevel());
        assertEquals(22006L, updateCaptor.getValue().getLeaderId());
    }

    /**
     * 离职审批通过后应直接同步员工为已离职状态。
     */
    @Test
    void shouldSyncLeaveApplicationAndEmployeeWhenApproved() {
        LeaveApplicationEntity entity = new LeaveApplicationEntity();
        entity.setId(34001L);
        entity.setEmployeeId(25005L);
        when(leaveApplicationMapper.selectById(34001L)).thenReturn(entity);

        listener.handleApprovalCompleted(new ApprovalCompletedEvent(
                934001L,
                ApprovalTypeEnum.LEAVE.getCode(),
                34001L,
                ApprovalStatusEnum.APPROVED.getCode()
        ));

        verify(leaveApplicationMapper).updateById(entity);
        assertEquals(ApplicationStatusEnum.APPROVED.getCode(), entity.getApprovalStatus());

        ArgumentCaptor<EmployeeApprovalSyncUpdateDTO> updateCaptor = ArgumentCaptor.forClass(EmployeeApprovalSyncUpdateDTO.class);
        verify(employeeService).syncEmployeeForApproval(org.mockito.ArgumentMatchers.eq(25005L), updateCaptor.capture());
        assertEquals(EmploymentStatusEnum.LEFT.getCode(), updateCaptor.getValue().getEmploymentStatus());
    }

    /**
     * 审批驳回后只更新申请单状态，不应回写员工档案。
     */
    @Test
    void shouldOnlyUpdateApplicationStatusWhenRejected() {
        TransferApplicationEntity entity = new TransferApplicationEntity();
        entity.setId(33002L);
        entity.setEmployeeId(25004L);
        when(transferApplicationMapper.selectById(33002L)).thenReturn(entity);

        listener.handleApprovalCompleted(new ApprovalCompletedEvent(
                933002L,
                ApprovalTypeEnum.TRANSFER.getCode(),
                33002L,
                ApprovalStatusEnum.REJECTED.getCode()
        ));

        verify(transferApplicationMapper).updateById(entity);
        assertEquals(ApplicationStatusEnum.REJECTED.getCode(), entity.getApprovalStatus());
        verify(employeeService, never()).syncEmployeeForApproval(any(), any());
    }
}

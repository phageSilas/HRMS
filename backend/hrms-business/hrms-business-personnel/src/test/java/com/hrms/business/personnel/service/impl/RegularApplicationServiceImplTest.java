package com.hrms.business.personnel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.approval.service.ApprovalTaskService;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.service.EmployeeService;
import com.hrms.business.personnel.common.enums.ApplicationStatusEnum;
import com.hrms.business.personnel.dto.RegularApplicationQueryDTO;
import com.hrms.business.personnel.entity.EmployeeSnapshotEntity;
import com.hrms.business.personnel.entity.RegularApplicationEntity;
import com.hrms.business.personnel.mapper.EmployeeSnapshotMapper;
import com.hrms.business.personnel.mapper.RegularApplicationMapper;
import com.hrms.business.personnel.vo.RegularApplicationPageVO;
import com.hrms.common.web.PageResult;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.service.PostService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 转正申请服务状态筛选测试
 */
@ExtendWith(MockitoExtension.class)
class RegularApplicationServiceImplTest {

    @Mock
    private RegularApplicationMapper regularApplicationMapper;

    @Mock
    private EmployeeSnapshotMapper employeeSnapshotMapper;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private ApprovalEngine approvalEngine;

    @Mock
    private ApprovalTaskService approvalTaskService;

    @Mock
    private DeptService deptService;

    @Mock
    private PostService postService;

    @Mock
    private ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    private RegularApplicationServiceImpl regularApplicationService;



    @BeforeEach
    void setUp() {
        initTableInfo(EmployeeSnapshotEntity.class);
        initTableInfo(RegularApplicationEntity.class);
        regularApplicationService = new RegularApplicationServiceImpl(
                regularApplicationMapper,
                employeeSnapshotMapper,
                employeeService,
                approvalEngine,
                approvalTaskService,
                deptService,
                postService,
                redisTemplateProvider
        );
    }

    /**
     * 验证待转正列表会排除最新申请状态已进入已评估范围的试用期员工。
     *
     * 本方法使用的工具类: PageResult(hrms-common),LocalDate(JDK),LocalDateTime(JDK)
     */
    @Test
    void shouldExcludeEmployeesWhoseLatestApplicationIsEvaluatedFromPendingTab() {
        EmployeeSnapshotEntity approvedEmployee = buildEmployeeSnapshot(1L, "已通过员工", "EMP001", 1);
        EmployeeSnapshotEntity rejectedEmployee = buildEmployeeSnapshot(2L, "已驳回员工", "EMP002", 2);
        EmployeeSnapshotEntity approvingEmployee = buildEmployeeSnapshot(3L, "审批中员工", "EMP003", 3);
        EmployeeSnapshotEntity draftEmployee = buildEmployeeSnapshot(4L, "待审批员工", "EMP004", 4);

        when(employeeSnapshotMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                approvedEmployee,
                rejectedEmployee,
                approvingEmployee,
                draftEmployee
        ));
        when(regularApplicationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(
                        buildRegularApplication(1L, 1L, ApplicationStatusEnum.APPROVED.getCode(), 1),
                        buildRegularApplication(2L, 2L, ApplicationStatusEnum.REJECTED.getCode(), 2),
                        buildRegularApplication(3L, 3L, ApplicationStatusEnum.APPROVING.getCode(), 3)
                ))
                .thenReturn(List.of(
                        buildRegularApplication(3L, 3L, ApplicationStatusEnum.APPROVING.getCode(), 3)
                ));

        PageResult<RegularApplicationPageVO> result = regularApplicationService.pageRegularApplications(
                RegularApplicationQueryDTO.builder()
                        .tab("pending")
                        .pageNum(1)
                        .pageSize(20)
                        .build()
        );

        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals(List.of(3L, 4L), result.getRecords().stream().map(RegularApplicationPageVO::getEmployeeId).toList());
        assertEquals(List.of(
                ApplicationStatusEnum.APPROVING.getCode(),
                ApplicationStatusEnum.DRAFT.getCode()
        ), result.getRecords().stream().map(RegularApplicationPageVO::getApprovalStatus).toList());
    }

    /**
     * 验证已评估列表查询会按已通过、已驳回、已入职状态进行过滤。
     *
     * 本方法使用的工具类: Page(mybatis-plus),LocalDate(JDK),LocalDateTime(JDK)
     */
    @Test
    void shouldQueryEvaluatedTabWithEvaluatedApprovalStatusesOnly() {
        RegularApplicationEntity approved = buildRegularApplication(11L, 101L, ApplicationStatusEnum.APPROVED.getCode(), 11);
        RegularApplicationEntity rejected = buildRegularApplication(12L, 102L, ApplicationStatusEnum.REJECTED.getCode(), 12);
        RegularApplicationEntity entered = buildRegularApplication(13L, 103L, ApplicationStatusEnum.ENTERED.getCode(), 13);

        when(regularApplicationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<RegularApplicationEntity>(1, 20, 3).setRecords(List.of(approved, rejected, entered)));
        when(employeeService.getEmployeeBrief(101L)).thenReturn(buildEmployee(101L, "已通过", "EMP101"));
        when(employeeService.getEmployeeBrief(102L)).thenReturn(buildEmployee(102L, "已驳回", "EMP102"));
        when(employeeService.getEmployeeBrief(103L)).thenReturn(buildEmployee(103L, "已入职", "EMP103"));

        PageResult<RegularApplicationPageVO> result = regularApplicationService.pageRegularApplications(
                RegularApplicationQueryDTO.builder()
                        .tab("evaluated")
                        .pageNum(1)
                        .pageSize(20)
                        .build()
        );

        assertEquals(List.of(
                ApplicationStatusEnum.APPROVED.getCode(),
                ApplicationStatusEnum.REJECTED.getCode(),
                ApplicationStatusEnum.ENTERED.getCode()
        ), result.getRecords().stream().map(RegularApplicationPageVO::getApprovalStatus).toList());
    }

    /**
     * 初始化 MyBatis-Plus 实体缓存，供 LambdaQueryWrapper 单测使用。
     *
     * @param entityClass 实体类型
     * 本方法使用的工具类: TableInfoHelper(mybatis-plus),MapperBuilderAssistant(mybatis),Configuration(mybatis)
     */
    private void initTableInfo(Class<?> entityClass) {
        if (TableInfoHelper.getTableInfo(entityClass) != null) {
            return;
        }
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), entityClass);
    }

    /**
     * 构造员工快照测试数据。
     *
     * @param employeeId 员工ID
     * @param employeeName 员工姓名
     * @param employeeNo 员工工号
     * @param createDay 创建日期
     * @return 员工快照
     * 本方法使用的工具类: LocalDate(JDK),LocalDateTime(JDK)
     */
    private EmployeeSnapshotEntity buildEmployeeSnapshot(Long employeeId, String employeeName, String employeeNo, int createDay) {
        EmployeeSnapshotEntity snapshot = new EmployeeSnapshotEntity();
        snapshot.setId(employeeId);
        snapshot.setEmployeeName(employeeName);
        snapshot.setEmployeeNo(employeeNo);
        snapshot.setDeptId(10L);
        snapshot.setPostId(20L);
        snapshot.setEmploymentStatus(1);
        snapshot.setHireDate(LocalDate.of(2026, 4, createDay));
        snapshot.setProbationMonth(3);
        snapshot.setCreateTime(LocalDateTime.of(2026, 7, createDay, 10, 0));
        return snapshot;
    }

    /**
     * 构造转正申请测试数据。
     *
     * @param id 申请ID
     * @param employeeId 员工ID
     * @param approvalStatus 审批状态
     * @param createDay 创建日期
     * @return 转正申请
     * 本方法使用的工具类: LocalDate(JDK),LocalDateTime(JDK)
     */
    private RegularApplicationEntity buildRegularApplication(Long id, Long employeeId, Integer approvalStatus, int createDay) {
        RegularApplicationEntity entity = new RegularApplicationEntity();
        entity.setId(id);
        entity.setEmployeeId(employeeId);
        entity.setApprovalStatus(approvalStatus);
        entity.setProbationEndDate(LocalDate.of(2026, 7, createDay));
        entity.setCreateTime(LocalDateTime.of(2026, 7, createDay, 12, 0));
        return entity;
    }

    /**
     * 构造员工简要信息测试数据。
     *
     * @param employeeId 员工ID
     * @param employeeName 员工姓名
     * @param employeeNo 员工工号
     * @return 员工简要信息
     * 本方法使用的工具类: LocalDate(JDK)
     */
    private EmployeeEntity buildEmployee(Long employeeId, String employeeName, String employeeNo) {
        EmployeeEntity employee = new EmployeeEntity();
        employee.setId(employeeId);
        employee.setEmployeeName(employeeName);
        employee.setEmployeeNo(employeeNo);
        employee.setDeptId(10L);
        employee.setPostId(20L);
        employee.setHireDate(LocalDate.of(2026, 4, 1));
        employee.setProbationMonth(3);
        return employee;
    }
}

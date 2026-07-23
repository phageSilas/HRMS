package com.hrms.business.salary.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.hrms.business.attendance.service.AttendanceService;
import com.hrms.business.attendance.vo.AttendancePayrollSourceVO;
import com.hrms.business.approval.service.ApprovalEngine;
import com.hrms.business.salary.dto.SalaryBatchCreateRequestDTO;
import com.hrms.business.salary.entity.SalaryBatchEntity;
import com.hrms.business.salary.entity.SalaryBatchItemEntity;
import com.hrms.business.salary.entity.SalaryEmployeeSnapshotEntity;
import com.hrms.business.salary.mapper.EmployeeSalaryProfileMapper;
import com.hrms.business.salary.mapper.SalaryBatchAdjustmentMapper;
import com.hrms.business.salary.mapper.SalaryBatchItemMapper;
import com.hrms.business.salary.mapper.SalaryBatchMapper;
import com.hrms.business.salary.mapper.SalaryEmployeeSnapshotMapper;
import com.hrms.business.salary.mq.event.SalaryBatchCalculateMessage;
import com.hrms.business.salary.mq.event.SalaryBatchCalculateTriggerTypeEnum;
import com.hrms.business.salary.mq.producer.SalaryBatchCalculateProducer;
import com.hrms.business.salary.vo.SalaryBatchPreviewVO;
import com.hrms.business.salary.vo.SalaryBatchTrendVO;
import com.hrms.business.salary.vo.SalaryBatchVO;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.security.UserContext;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.file.config.FileConfig;
import com.hrms.system.file.service.FileService;
import com.hrms.system.organization.service.DeptService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hrms.business.salary.cache.SalaryCacheKeys.calculateLock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 薪资核算服务异步重算测试。
 */
@ExtendWith(MockitoExtension.class)
class SalaryCalculateServiceImplTest {

    @Mock
    private SalaryBatchMapper salaryBatchMapper;

    @Mock
    private SalaryBatchAdjustmentMapper salaryBatchAdjustmentMapper;

    @Mock
    private SalaryBatchItemMapper salaryBatchItemMapper;

    @Mock
    private SalaryEmployeeSnapshotMapper employeeSnapshotMapper;

    @Mock
    private EmployeeSalaryProfileMapper employeeSalaryProfileMapper;

    @Mock
    private ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectProvider<com.hrms.business.attendance.service.AttendanceService> attendanceServiceProvider;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private SalaryBatchCalculateProducer salaryBatchCalculateProducer;

    @Mock
    private ApprovalEngine approvalEngine;

    @Mock
    private RoleService roleService;

    @Mock
    private DeptService deptService;

    @Mock
    private FileService fileService;

    @Mock
    private FileConfig fileConfig;

    private SalaryCalculateServiceImpl salaryCalculateService;

    /**
     * 初始化薪资核算服务测试上下文。
     *
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),List(JDK)
     */
    @BeforeEach
    void setUp() {
        initTableInfo(SalaryBatchEntity.class);
        initTableInfo(SalaryBatchItemEntity.class);
        initTableInfo(SalaryEmployeeSnapshotEntity.class);
        salaryCalculateService = new SalaryCalculateServiceImpl(
                salaryBatchMapper,
                salaryBatchAdjustmentMapper,
                salaryBatchItemMapper,
                employeeSnapshotMapper,
                employeeSalaryProfileMapper,
                redisTemplateProvider,
                attendanceServiceProvider,
                salaryBatchCalculateProducer,
                approvalEngine,
                roleService,
                deptService,
                fileService,
                fileConfig
        );
        UserContext context = new UserContext();
        context.setUserId(1L);
        SecurityContextHolder.setContext(context);

        RoleEntity financeRole = new RoleEntity();
        financeRole.setRoleCode("FINANCE");
        lenient().when(roleService.getRolesByUserId(1L)).thenReturn(List.of(financeRole));
        lenient().when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        lenient().when(attendanceServiceProvider.getIfAvailable()).thenReturn(attendanceService);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.setIfAbsent(any(), eq("1"), eq(10L), eq(TimeUnit.MINUTES))).thenReturn(Boolean.TRUE);
        lenient().when(employeeSnapshotMapper.selectList(any())).thenReturn(List.of(buildEmployee(2001L)));
        lenient().when(attendanceService.getPayrollSource(any(), anyList())).thenReturn(List.of(buildAttendanceWithActivity(2001L)));
    }

    /**
     * 清理线程级安全上下文，避免污染其他测试。
     *
     * 本方法使用的工具类: SecurityContextHolder(hrms-common)
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    /**
     * 验证重新计算会先把批次状态更新为 CALCULATING 再异步发送消息。
     *
     * 本方法使用的工具类: ArgumentCaptor(mockito),BigDecimal(JDK)
     */
    @Test
    void shouldMarkBatchAsCalculatingBeforeSendingRecalculateMessage() {
        SalaryBatchEntity batch = buildPendingReviewBatch(1001L);
        when(salaryBatchMapper.selectById(1001L)).thenReturn(batch);

        SalaryBatchVO result = salaryCalculateService.recalculateBatch(1001L);

        ArgumentCaptor<SalaryBatchEntity> batchCaptor = ArgumentCaptor.forClass(SalaryBatchEntity.class);
        verify(salaryBatchMapper).updateById(batchCaptor.capture());
        verify(salaryBatchCalculateProducer).send(any(SalaryBatchCalculateMessage.class));
        assertEquals("CALCULATING", batchCaptor.getValue().getBatchStatus());
        assertEquals("CALCULATING", result.getBatchStatus());
    }

    /**
     * 验证重新计算消息发送失败时会回滚批次状态并释放 Redis 锁。
     *
     * 本方法使用的工具类: ArgumentCaptor(mockito),TimeUnit(JDK)
     */
    @Test
    void shouldRollbackBatchStatusAndReleaseLockWhenRecalculateMessageSendFails() {
        SalaryBatchEntity batch = buildPendingReviewBatch(1002L);
        List<String> updatedStatuses = new ArrayList<>();
        when(salaryBatchMapper.selectById(1002L)).thenReturn(batch);
        when(salaryBatchMapper.updateById(any(SalaryBatchEntity.class))).thenAnswer((Answer<Integer>) invocation -> {
            SalaryBatchEntity entity = invocation.getArgument(0);
            updatedStatuses.add(entity.getBatchStatus());
            return 1;
        });
        doThrow(new IllegalStateException("mq send failed")).when(salaryBatchCalculateProducer)
                .send(any(SalaryBatchCalculateMessage.class));

        assertThrows(IllegalStateException.class, () -> salaryCalculateService.recalculateBatch(1002L));

        verify(salaryBatchMapper, times(2)).updateById(any(SalaryBatchEntity.class));
        assertEquals(List.of("CALCULATING", "PENDING_REVIEW"), updatedStatuses);
        verify(redisTemplate).delete(calculateLock(1002L));
    }

    /**
     * 验证重算消息消费异常时会回滚批次状态并释放 Redis 锁。
     *
     * 本方法使用的工具类: ArgumentCaptor(mockito),TimeUnit(JDK)
     */
    @Test
    void shouldRollbackBatchStatusAndReleaseLockWhenRecalculateConsumptionFails() {
        SalaryBatchEntity batch = buildCalculatingBatch(1003L);
        List<String> updatedStatuses = new ArrayList<>();
        when(salaryBatchMapper.selectById(1003L)).thenReturn(batch);
        when(salaryBatchMapper.updateById(any(SalaryBatchEntity.class))).thenAnswer((Answer<Integer>) invocation -> {
            SalaryBatchEntity entity = invocation.getArgument(0);
            updatedStatuses.add(entity.getBatchStatus());
            return 1;
        });
        doThrow(new IllegalStateException("load employee failed")).when(employeeSnapshotMapper).selectList(any());

        SalaryBatchCalculateMessage message = SalaryBatchCalculateMessage.builder()
                .messageId("msg-1003")
                .batchId(1003L)
                .salaryMonth("2026-07")
                .triggerType(SalaryBatchCalculateTriggerTypeEnum.RECALCULATE.name())
                .rollbackStatus("PENDING_REVIEW")
                .applyAdjustments(true)
                .build();

        assertThrows(IllegalStateException.class, () -> salaryCalculateService.handleBatchCalculateMessage(message));

        verify(salaryBatchMapper, times(1)).updateById(any(SalaryBatchEntity.class));
        assertEquals(List.of("PENDING_REVIEW"), updatedStatuses);
        verify(redisTemplate).delete(calculateLock(1003L));
    }

    /**
     * 未来月份不允许创建薪资核算批次。
     */
    @Test
    void shouldRejectCreatingBatchForFutureMonth() {
        SalaryBatchCreateRequestDTO requestDTO = SalaryBatchCreateRequestDTO.builder()
                .salaryMonth("2030-05")
                .scopeType("ALL")
                .build();

        var exception = assertThrows(
                com.hrms.common.exception.GlobalException.class,
                () -> salaryCalculateService.createBatch(requestDTO)
        );

        assertEquals("未来月份不允许创建薪资核算批次", exception.getMessage());
        verify(salaryBatchMapper, never()).insert(any(SalaryBatchEntity.class));
    }

    /**
     * 当前月若完全没有考勤数据，不允许创建薪资核算批次。
     */
    @Test
    void shouldRejectCreatingBatchWhenAttendanceDataMissing() {
        SalaryBatchCreateRequestDTO requestDTO = SalaryBatchCreateRequestDTO.builder()
                .salaryMonth("2026-07")
                .scopeType("ALL")
                .build();
        SalaryEmployeeSnapshotEntity employee = new SalaryEmployeeSnapshotEntity();
        employee.setId(2001L);
        employee.setEmployeeNo("EMP2001");
        employee.setEmployeeName("张三");
        when(employeeSnapshotMapper.selectList(any())).thenReturn(List.of(employee));
        when(attendanceService.getPayrollSource(eq("2026-07"), anyList())).thenReturn(List.of(
                AttendancePayrollSourceVO.builder()
                        .employeeId(2001L)
                        .employeeNo("EMP2001")
                        .employeeName("张三")
                        .shouldAttendDays(23)
                        .actualAttendDays(0)
                        .lateCount(0)
                        .earlyLeaveCount(0)
                        .leaveDays(BigDecimal.ZERO)
                        .absenceDays(new BigDecimal("23"))
                        .overtimeHours(BigDecimal.ZERO)
                        .build()
        ));

        var exception = assertThrows(
                com.hrms.common.exception.GlobalException.class,
                () -> salaryCalculateService.createBatch(requestDTO)
        );

        assertEquals("当前月份暂无考勤数据，无法创建薪资核算批次", exception.getMessage());
        verify(salaryBatchMapper, never()).insert(any(SalaryBatchEntity.class));
    }

    /**
     * 已存在的未来月份批次不允许继续触发核算。
     */
    @Test
    void shouldRejectCalculatingFutureBatch() {
        SalaryBatchEntity batch = buildPendingReviewBatch(1004L);
        batch.setSalaryMonth("2030-05");
        when(salaryBatchMapper.selectById(1004L)).thenReturn(batch);

        var exception = assertThrows(
                com.hrms.common.exception.GlobalException.class,
                () -> salaryCalculateService.calculateBatch(1004L)
        );

        assertEquals("未来月份不允许执行薪资核算", exception.getMessage());
        verify(salaryBatchCalculateProducer, never()).send(any(SalaryBatchCalculateMessage.class));
    }

    /**
     * 构造待复核薪资批次测试数据。
     *
     * @param batchId 批次ID
     * @return 薪资批次实体
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    /**
     * 未传部门筛选时应保持原有按批次汇总口径。
     */
    @Test
    void shouldKeepExistingTrendAggregationWhenDeptIdsMissing() {
        SalaryBatchEntity febBatch = buildTrendBatch(2001L, "2026-02", "ALL", new BigDecimal("1200"), new BigDecimal("980"), 3);
        SalaryBatchEntity marBatch = buildTrendBatch(2002L, "2026-03", "ALL", new BigDecimal("1600"), new BigDecimal("1300"), 4);
        when(salaryBatchMapper.selectList(any())).thenReturn(List.of(febBatch, marBatch));

        List<SalaryBatchTrendVO> result = salaryCalculateService.listBatchTrend("2026-03", 2, "ALL", null, null);

        assertEquals(2, result.size());
        assertEquals("2026-02", result.get(0).getMonth());
        assertBigDecimalEquals("1200", result.get(0).getGrossSalary());
        assertBigDecimalEquals("980", result.get(0).getNetSalary());
        assertEquals(3, result.get(0).getEmployeeCount());
        assertEquals("2026-03", result.get(1).getMonth());
        assertBigDecimalEquals("1600", result.get(1).getGrossSalary());
        assertBigDecimalEquals("1300", result.get(1).getNetSalary());
        assertEquals(4, result.get(1).getEmployeeCount());
    }

    /**
     * 传多个部门时应按部门命中的明细重新聚合趋势。
     */
    @Test
    void shouldAggregateTrendBySelectedDepartments() {
        SalaryBatchEntity febBatch = buildTrendBatch(2101L, "2026-02", "ALL", BigDecimal.ZERO, BigDecimal.ZERO, 0);
        SalaryBatchEntity marBatch = buildTrendBatch(2102L, "2026-03", "ALL", BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(salaryBatchMapper.selectList(any())).thenReturn(List.of(febBatch, marBatch));
        when(salaryBatchItemMapper.selectList(any())).thenReturn(List.of(
                buildTrendItem(2101L, 3001L, new BigDecimal("100"), new BigDecimal("90")),
                buildTrendItem(2101L, 3002L, new BigDecimal("200"), new BigDecimal("180")),
                buildTrendItem(2102L, 3003L, new BigDecimal("300"), new BigDecimal("260")),
                buildTrendItem(2102L, 3004L, new BigDecimal("400"), new BigDecimal("350"))
        ));
        when(employeeSnapshotMapper.selectList(any())).thenReturn(List.of(
                buildEmployee(3001L, 1L),
                buildEmployee(3002L, 2L),
                buildEmployee(3003L, 1L),
                buildEmployee(3004L, 3L)
        ));

        List<SalaryBatchTrendVO> result = salaryCalculateService.listBatchTrend("2026-03", 2, "ALL", null, List.of(1L, 3L));

        assertEquals(2, result.size());
        assertBigDecimalEquals("100", result.get(0).getGrossSalary());
        assertBigDecimalEquals("90", result.get(0).getNetSalary());
        assertEquals(1, result.get(0).getEmployeeCount());
        assertBigDecimalEquals("700", result.get(1).getGrossSalary());
        assertBigDecimalEquals("610", result.get(1).getNetSalary());
        assertEquals(2, result.get(1).getEmployeeCount());
    }

    /**
     * 某个月没有命中部门明细时应返回零值趋势点。
     */
    @Test
    void shouldReturnZeroTrendPointWhenDepartmentHasNoItemsForMonth() {
        SalaryBatchEntity febBatch = buildTrendBatch(2201L, "2026-02", "ALL", BigDecimal.ZERO, BigDecimal.ZERO, 0);
        SalaryBatchEntity marBatch = buildTrendBatch(2202L, "2026-03", "ALL", BigDecimal.ZERO, BigDecimal.ZERO, 0);
        when(salaryBatchMapper.selectList(any())).thenReturn(List.of(febBatch, marBatch));
        when(salaryBatchItemMapper.selectList(any())).thenReturn(List.of(
                buildTrendItem(2201L, 3101L, new BigDecimal("180"), new BigDecimal("160")),
                buildTrendItem(2202L, 3102L, new BigDecimal("260"), new BigDecimal("230"))
        ));
        when(employeeSnapshotMapper.selectList(any())).thenReturn(List.of(
                buildEmployee(3101L, 2L),
                buildEmployee(3102L, 1L)
        ));

        List<SalaryBatchTrendVO> result = salaryCalculateService.listBatchTrend("2026-03", 2, "ALL", null, List.of(2L));

        assertEquals(2, result.size());
        assertBigDecimalEquals("180", result.get(0).getGrossSalary());
        assertBigDecimalEquals("160", result.get(0).getNetSalary());
        assertEquals(1, result.get(0).getEmployeeCount());
        assertBigDecimalEquals("0", result.get(1).getGrossSalary());
        assertBigDecimalEquals("0", result.get(1).getNetSalary());
        assertEquals(0, result.get(1).getEmployeeCount());
    }

    /**
     * 预览明细应返回部门ID，供前端按部门过滤图表。
     */
    @Test
    void shouldIncludeDeptIdInPreviewItems() {
        SalaryBatchEntity batch = buildPendingReviewBatch(2301L);
        SalaryBatchItemEntity item = buildTrendItem(2301L, 3201L, new BigDecimal("500"), new BigDecimal("420"));
        when(salaryBatchMapper.selectById(2301L)).thenReturn(batch);
        when(salaryBatchItemMapper.selectList(any())).thenReturn(List.of(item));
        when(employeeSnapshotMapper.selectList(any())).thenReturn(List.of(buildEmployee(3201L, 88L)));

        SalaryBatchPreviewVO result = salaryCalculateService.previewBatch(2301L);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(88L, result.getItems().get(0).getDeptId());
    }

    private SalaryBatchEntity buildPendingReviewBatch(Long batchId) {
        SalaryBatchEntity batch = new SalaryBatchEntity();
        batch.setId(batchId);
        batch.setBatchNo("SAL-202607-test");
        batch.setSalaryMonth("2026-07");
        batch.setScopeType("ALL");
        batch.setBatchStatus("PENDING_REVIEW");
        batch.setTotalGrossSalary(BigDecimal.ZERO);
        batch.setTotalNetSalary(BigDecimal.ZERO);
        batch.setYellowWarningCount(0);
        batch.setRedWarningCount(0);
        batch.setBlockCount(0);
        return batch;
    }

    /**
     * 构造计算中的薪资批次测试数据。
     *
     * @param batchId 批次ID
     * @return 薪资批次实体
     * 本方法使用的工具类: BigDecimal(JDK)
     */
    private SalaryBatchEntity buildCalculatingBatch(Long batchId) {
        SalaryBatchEntity batch = buildPendingReviewBatch(batchId);
        batch.setBatchStatus("CALCULATING");
        return batch;
    }

    /**
     * 构造测试员工快照。
     *
     * @param employeeId 员工ID
     * @return 员工快照
     */
    /**
     * 构造趋势图测试批次数据。
     *
     * @param batchId 批次ID
     * @param salaryMonth 薪资月份
     * @param scopeType 范围类型
     * @param grossSalary 应发总额
     * @param netSalary 实发总额
     * @param employeeCount 员工人数
     * @return 薪资批次实体
     */
    private SalaryBatchEntity buildTrendBatch(Long batchId, String salaryMonth, String scopeType,
                                              BigDecimal grossSalary, BigDecimal netSalary, Integer employeeCount) {
        SalaryBatchEntity batch = buildPendingReviewBatch(batchId);
        batch.setSalaryMonth(salaryMonth);
        batch.setScopeType(scopeType);
        batch.setTotalGrossSalary(grossSalary);
        batch.setTotalNetSalary(netSalary);
        batch.setTotalCount(employeeCount);
        return batch;
    }

    /**
     * 构造趋势图测试明细数据。
     *
     * @param batchId 批次ID
     * @param employeeId 员工ID
     * @param grossSalary 应发总额
     * @param netSalary 实发总额
     * @return 薪资明细实体
     */
    private SalaryBatchItemEntity buildTrendItem(Long batchId, Long employeeId, BigDecimal grossSalary, BigDecimal netSalary) {
        SalaryBatchItemEntity item = new SalaryBatchItemEntity();
        item.setBatchId(batchId);
        item.setEmployeeId(employeeId);
        item.setGrossSalary(grossSalary);
        item.setNetSalary(netSalary);
        item.setBaseSalary(grossSalary);
        item.setDeductionTotal(grossSalary.subtract(netSalary));
        return item;
    }

    /**
     * 构造带部门信息的测试员工快照。
     *
     * @param employeeId 员工ID
     * @param deptId 部门ID
     * @return 员工快照
     */
    private SalaryEmployeeSnapshotEntity buildEmployee(Long employeeId, Long deptId) {
        SalaryEmployeeSnapshotEntity employee = new SalaryEmployeeSnapshotEntity();
        employee.setId(employeeId);
        employee.setEmployeeNo("EMP" + employeeId);
        employee.setEmployeeName("娴嬭瘯鍛樺伐");
        employee.setDeptId(deptId);
        employee.setEmploymentStatus(1);
        employee.setIsDeleted(0);
        return employee;
    }

    private SalaryEmployeeSnapshotEntity buildEmployee(Long employeeId) {
        SalaryEmployeeSnapshotEntity employee = new SalaryEmployeeSnapshotEntity();
        employee.setId(employeeId);
        employee.setEmployeeNo("EMP" + employeeId);
        employee.setEmployeeName("测试员工");
        employee.setEmploymentStatus(1);
        employee.setIsDeleted(0);
        return employee;
    }

    /**
     * 构造带真实考勤活动的月度考勤汇总。
     *
     * @param employeeId 员工ID
     * @return 月度考勤汇总
     */
    private AttendancePayrollSourceVO buildAttendanceWithActivity(Long employeeId) {
        return AttendancePayrollSourceVO.builder()
                .employeeId(employeeId)
                .employeeNo("EMP" + employeeId)
                .employeeName("测试员工")
                .shouldAttendDays(23)
                .actualAttendDays(1)
                .lateCount(0)
                .earlyLeaveCount(0)
                .leaveDays(BigDecimal.ZERO)
                .absenceDays(new BigDecimal("22"))
                .overtimeHours(BigDecimal.ZERO)
                .build();
    }

    /**
     * 按金额数值比较 BigDecimal，忽略小数位差异。
     *
     * @param expected 期望值
     * @param actual 实际值
     */
    private void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    /**
     * 初始化 MyBatis-Plus 实体缓存，供 LambdaWrapper 单测使用。
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
}

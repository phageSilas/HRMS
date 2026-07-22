package com.hrms.business.salary.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.hrms.business.approval.service.ApprovalEngine;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doThrow;
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
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.setIfAbsent(any(), eq("1"), eq(10L), eq(TimeUnit.MINUTES))).thenReturn(Boolean.TRUE);
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

        verify(salaryBatchMapper, times(2)).updateById(any(SalaryBatchEntity.class));
        assertEquals(List.of("CALCULATING", "PENDING_REVIEW"), updatedStatuses);
        verify(redisTemplate).delete(calculateLock(1003L));
    }

    /**
     * 构造待复核薪资批次测试数据。
     *
     * @param batchId 批次ID
     * @return 薪资批次实体
     * 本方法使用的工具类: BigDecimal(JDK)
     */
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

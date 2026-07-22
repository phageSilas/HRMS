package com.hrms.business.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.hrms.business.approval.config.ApprovalNodeDef;
import com.hrms.business.approval.dto.OperateResultVO;
import com.hrms.business.approval.entity.ApprovalInstanceEntity;
import com.hrms.business.approval.entity.ApprovalTaskEntity;
import com.hrms.business.approval.enums.ApprovalStatusEnum;
import com.hrms.business.approval.enums.ApproveResultEnum;
import com.hrms.business.approval.enums.TaskStatusEnum;
import com.hrms.business.approval.mapper.ApprovalInstanceMapper;
import com.hrms.business.approval.mapper.ApprovalTaskMapper;
import com.hrms.business.approval.service.ApprovalTemplateLoader;
import com.hrms.business.approval.service.ApproverResolver;
import com.hrms.business.approval.service.event.ApprovalCompletedEvent;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 审批引擎单节点流程测试
 */
@ExtendWith(MockitoExtension.class)
class ApprovalEngineImplTest {

    @Mock
    private ApprovalInstanceMapper instanceMapper;

    @Mock
    private ApprovalTaskMapper taskMapper;

    @Mock
    private ApprovalTemplateLoader templateLoader;

    @Mock
    private ApproverResolver approverResolver;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ApprovalEngineImpl approvalEngine;

    @BeforeEach
    void setUp() {
        initTableInfo(ApprovalTaskEntity.class);
        approvalEngine = new ApprovalEngineImpl(
                instanceMapper,
                taskMapper,
                templateLoader,
                approverResolver,
                eventPublisher
        );
    }

    /**
     * 单节点模板下，部门负责人审批通过后应直接完成审批实例。
     */
    @Test
    void shouldCompleteApprovalDirectlyAfterDeptHeadApprovesSingleNodeFlow() {
        ApprovalNodeDef deptHeadNode = new ApprovalNodeDef("DEPT_HEAD", "部门负责人审批", "DEPT_HEAD", 1, false);
        when(templateLoader.loadTemplate("REGULAR")).thenReturn(List.of(deptHeadNode));
        when(approverResolver.resolveApprover("DEPT_HEAD", 101L, 32001L)).thenReturn(90001L);

        AtomicLong instanceIdGenerator = new AtomicLong(1L);
        AtomicLong taskIdGenerator = new AtomicLong(1L);
        AtomicReference<ApprovalInstanceEntity> storedInstance = new AtomicReference<>();
        AtomicReference<ApprovalTaskEntity> storedTask = new AtomicReference<>();

        when(instanceMapper.insert(any(ApprovalInstanceEntity.class))).thenAnswer(invocation -> {
            ApprovalInstanceEntity instance = invocation.getArgument(0);
            instance.setId(instanceIdGenerator.getAndIncrement());
            storedInstance.set(cloneInstance(instance));
            return 1;
        });
        when(instanceMapper.updateById(any(ApprovalInstanceEntity.class))).thenAnswer(invocation -> {
            ApprovalInstanceEntity instance = invocation.getArgument(0);
            storedInstance.set(cloneInstance(instance));
            return 1;
        });
        when(instanceMapper.selectById(anyLong())).thenAnswer(invocation -> storedInstance.get());

        when(taskMapper.insert(any(ApprovalTaskEntity.class))).thenAnswer(invocation -> {
            ApprovalTaskEntity task = invocation.getArgument(0);
            task.setId(taskIdGenerator.getAndIncrement());
            storedTask.set(cloneTask(task));
            return 1;
        });
        when(taskMapper.selectById(anyLong())).thenAnswer(invocation -> storedTask.get());
        when(taskMapper.update(eq(null), any(Wrapper.class))).thenAnswer(invocation -> {
            ApprovalTaskEntity task = storedTask.get();
            task.setTaskStatus(TaskStatusEnum.PROCESSED.getCode());
            task.setApproveResult(ApproveResultEnum.APPROVE.getCode());
            task.setApproveTime(LocalDateTime.now());
            return 1;
        });

        Long instanceId = approvalEngine.startApproval("REGULAR", 32001L, "{\"employeeId\":25001}",
                11001L, 101L, 25001L);

        ApprovalTaskEntity createdTask = storedTask.get();
        assertEquals(instanceId, createdTask.getInstanceId());
        assertEquals("部门负责人审批", storedInstance.get().getCurrentNodeName());

        OperateResultVO result = approvalEngine.processAction(createdTask.getId(), "approve", "同意", null);

        assertTrue(Boolean.TRUE.equals(result.getSuccess()));
        assertEquals("PROCESSED", result.getTaskStatus());
        assertEquals(ApprovalStatusEnum.APPROVED.name(), result.getInstanceStatus());
        assertNull(result.getNextNodeName());
        assertEquals(ApprovalStatusEnum.APPROVED.getCode(), storedInstance.get().getApprovalStatus());
        verify(taskMapper, times(1)).insert(any(ApprovalTaskEntity.class));

        ArgumentCaptor<ApprovalCompletedEvent> eventCaptor = ArgumentCaptor.forClass(ApprovalCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(instanceId, eventCaptor.getValue().getInstanceId());
        assertEquals("REGULAR", eventCaptor.getValue().getApprovalType());
        assertEquals(32001L, eventCaptor.getValue().getBizId());
        assertEquals(ApprovalStatusEnum.APPROVED.getCode(), eventCaptor.getValue().getInstanceStatus());
    }

    /**
     * 初始化 MyBatis-Plus 实体缓存，供 LambdaUpdateWrapper 单测使用。
     *
     * @param entityClass 实体类型
     */
    private void initTableInfo(Class<?> entityClass) {
        if (TableInfoHelper.getTableInfo(entityClass) != null) {
            return;
        }
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), entityClass);
    }

    /**
     * 复制审批实例，避免测试中的对象引用互相污染。
     *
     * @param source 审批实例
     * @return 审批实例副本
     */
    private ApprovalInstanceEntity cloneInstance(ApprovalInstanceEntity source) {
        ApprovalInstanceEntity target = new ApprovalInstanceEntity();
        target.setId(source.getId());
        target.setApprovalNo(source.getApprovalNo());
        target.setApprovalType(source.getApprovalType());
        target.setBizId(source.getBizId());
        target.setTitle(source.getTitle());
        target.setApplicantUserId(source.getApplicantUserId());
        target.setApplicantEmployeeId(source.getApplicantEmployeeId());
        target.setCurrentNodeName(source.getCurrentNodeName());
        target.setApprovalStatus(source.getApprovalStatus());
        target.setFormJson(source.getFormJson());
        target.setApplyTime(source.getApplyTime());
        target.setFinishTime(source.getFinishTime());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        target.setIsDeleted(source.getIsDeleted());
        target.setVersion(source.getVersion());
        return target;
    }

    /**
     * 复制审批任务，避免测试中的对象引用互相污染。
     *
     * @param source 审批任务
     * @return 审批任务副本
     */
    private ApprovalTaskEntity cloneTask(ApprovalTaskEntity source) {
        ApprovalTaskEntity target = new ApprovalTaskEntity();
        target.setId(source.getId());
        target.setInstanceId(source.getInstanceId());
        target.setNodeCode(source.getNodeCode());
        target.setNodeName(source.getNodeName());
        target.setApproverUserId(source.getApproverUserId());
        target.setOriginalApproverId(source.getOriginalApproverId());
        target.setDelegateFlag(source.getDelegateFlag());
        target.setTaskStatus(source.getTaskStatus());
        target.setApproveResult(source.getApproveResult());
        target.setApproveComment(source.getApproveComment());
        target.setReceiveTime(source.getReceiveTime());
        target.setApproveTime(source.getApproveTime());
        target.setDeadlineTime(source.getDeadlineTime());
        target.setSortNo(source.getSortNo());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }
}

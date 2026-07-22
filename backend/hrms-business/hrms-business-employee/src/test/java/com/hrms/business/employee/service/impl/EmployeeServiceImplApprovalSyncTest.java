package com.hrms.business.employee.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.hrms.business.employee.dto.EmployeeApprovalSyncUpdateDTO;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.event.EmployeeChangeEvent;
import com.hrms.business.employee.mapper.EmployeeMapper;
import com.hrms.common.exception.GlobalException;
import com.hrms.system.auth.service.FieldPermissionService;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.auth.service.UserService;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.service.PostService;
import com.hrms.system.organization.vo.DeptDetailVO;
import com.hrms.system.organization.vo.PostVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 员工审批联动同步测试
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplApprovalSyncTest {

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private FieldPermissionService fieldPermissionService;

    @Mock
    private DeptService deptService;

    @Mock
    private PostService postService;

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @Mock
    private PlatformTransactionManager transactionManager;

    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        initTableInfo(EmployeeEntity.class);
        employeeService = new EmployeeServiceImpl(
                employeeMapper,
                fieldPermissionService,
                deptService,
                postService,
                userService,
                roleService,
                eventPublisher,
                stringRedisTemplate,
                transactionManager
        );
    }

    /**
     * 部门变更时应同步用户部门并发布员工变更事件。
     */
    @Test
    void shouldSyncUserDeptAndPublishEventWhenDeptChanged() {
        EmployeeEntity entity = buildEmployee(25001L, 2101L, 3001L, "P4", 22001L);
        entity.setUserId(11001L);
        when(employeeMapper.selectById(25001L)).thenReturn(entity);
        when(employeeMapper.update(any(), any())).thenReturn(1);
        when(deptService.getDeptById(2301L)).thenReturn(new DeptDetailVO());
        when(postService.getPostById(3002L)).thenReturn(new PostVO());

        EmployeeEntity updated = employeeService.syncEmployeeForApproval(25001L,
                EmployeeApprovalSyncUpdateDTO.builder()
                        .deptId(2301L)
                        .postId(3002L)
                        .jobLevel("P5")
                        .leaderId(22002L)
                        .build());

        assertEquals(2301L, updated.getDeptId());
        assertEquals(3002L, updated.getPostId());
        assertEquals("P5", updated.getJobLevel());
        assertEquals(22002L, updated.getLeaderId());
        verify(userService).updateUserDept(11001L, 2301L);

        ArgumentCaptor<EmployeeChangeEvent> eventCaptor = ArgumentCaptor.forClass(EmployeeChangeEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(EmployeeChangeEvent.ChangeType.UPDATE, eventCaptor.getValue().getChangeType());
        assertEquals(2101L, eventCaptor.getValue().getOldDeptId());
        assertEquals(2301L, eventCaptor.getValue().getNewDeptId());
    }

    /**
     * 仅更新状态时不应触发部门同步逻辑。
     */
    @Test
    void shouldNotSyncDeptOrPublishEventWhenOnlyEmploymentStatusChanged() {
        EmployeeEntity entity = buildEmployee(25002L, 2102L, 3003L, "P3", 22003L);
        when(employeeMapper.selectById(25002L)).thenReturn(entity);
        when(employeeMapper.update(any(), any())).thenReturn(1);

        EmployeeEntity updated = employeeService.syncEmployeeForApproval(25002L,
                EmployeeApprovalSyncUpdateDTO.builder()
                        .employmentStatus(4)
                        .build());

        assertEquals(4, updated.getEmploymentStatus());
        verify(userService, never()).updateUserDept(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    /**
     * 员工不存在时应抛出异常。
     */
    @Test
    void shouldThrowWhenEmployeeNotFound() {
        when(employeeMapper.selectById(99999L)).thenReturn(null);

        assertThrows(GlobalException.class, () -> employeeService.syncEmployeeForApproval(
                99999L,
                EmployeeApprovalSyncUpdateDTO.builder().employmentStatus(2).build()
        ));
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
     * 构造员工测试数据。
     *
     * @param employeeId 员工ID
     * @param deptId 部门ID
     * @param postId 职位ID
     * @param jobLevel 职级
     * @param leaderId 汇报人ID
     * @return 员工实体
     */
    private EmployeeEntity buildEmployee(Long employeeId, Long deptId, Long postId, String jobLevel, Long leaderId) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(employeeId);
        entity.setDeptId(deptId);
        entity.setPostId(postId);
        entity.setJobLevel(jobLevel);
        entity.setLeaderId(leaderId);
        entity.setEmploymentStatus(1);
        return entity;
    }
}

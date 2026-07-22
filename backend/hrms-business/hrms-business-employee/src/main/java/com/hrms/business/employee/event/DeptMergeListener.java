package com.hrms.business.employee.event;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.mapper.EmployeeMapper;
import com.hrms.system.organization.event.DeptMergeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门合并事件监听器
 * <p>
 * 监听部门合并事件，处理员工迁移逻辑
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeptMergeListener {

    private final EmployeeMapper employeeMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 监听部门合并事件，迁移员工
     */
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void onDeptMerge(DeptMergeEvent event) {
        Long sourceDeptId = event.getSourceDeptId();
        Long targetDeptId = event.getTargetDeptId();

        log.info("收到部门合并事件：sourceDeptId={}, targetDeptId={}", sourceDeptId, targetDeptId);

        // 批量更新员工部门ID
        int updatedCount = employeeMapper.update(null,
                Wrappers.<EmployeeEntity>lambdaUpdate()
                        .set(EmployeeEntity::getDeptId, targetDeptId)
                        .eq(EmployeeEntity::getDeptId, sourceDeptId)
                        .eq(EmployeeEntity::getIsDeleted, 0));

        log.info("部门合并完成：迁移了 {} 名员工从部门 {} 到部门 {}", updatedCount, sourceDeptId, targetDeptId);

        // 发布员工变更事件以触发部门人数更新
        if (updatedCount > 0) {
            eventPublisher.publishEvent(new EmployeeChangeEvent(this,
                    EmployeeChangeEvent.ChangeType.UPDATE, null, sourceDeptId, targetDeptId));
        }
    }

}
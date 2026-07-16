package com.hrms.business.employee.event;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.enums.EmploymentStatusEnum;
import com.hrms.business.employee.mapper.EmployeeMapper;
import com.hrms.system.organization.entity.DeptEntity;
import com.hrms.system.organization.mapper.DeptMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工变更事件监听器
 * <p>
 * 监听员工变更事件，实时更新部门人数缓存
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeChangeListener {

    private final EmployeeMapper employeeMapper;
    private final DeptMapper deptMapper;

    /**
     * 监听员工变更事件
     */
    @EventListener
    public void onEmployeeChange(EmployeeChangeEvent event) {
        log.info("收到员工变更事件：type={}, employeeId={}", event.getChangeType(), event.getEmployeeId());

        switch (event.getChangeType()) {
            case CREATE:
                handleCreate(event.getNewDeptId());
                break;
            case DELETE:
                handleDelete(event.getOldDeptId());
                break;
            case UPDATE:
                handleUpdate(event.getOldDeptId(), event.getNewDeptId());
                break;
            default:
                log.warn("未知的变更类型：{}", event.getChangeType());
        }
    }

    /**
     * 处理员工创建
     */
    private void handleCreate(Long deptId) {
        if (deptId == null) {
            return;
        }
        refreshDeptEmployeeCount(deptId);
    }

    /**
     * 处理员工删除
     */
    private void handleDelete(Long deptId) {
        if (deptId == null) {
            return;
        }
        refreshDeptEmployeeCount(deptId);
    }

    /**
     * 处理员工更新（调部门）
     */
    private void handleUpdate(Long oldDeptId, Long newDeptId) {
        if (oldDeptId != null) {
            refreshDeptEmployeeCount(oldDeptId);
        }
        if (newDeptId != null && !newDeptId.equals(oldDeptId)) {
            refreshDeptEmployeeCount(newDeptId);
        }
    }

    /**
     * 刷新指定部门的人数
     */
    private void refreshDeptEmployeeCount(Long deptId) {
        try {
            // 查询该部门及所有下属部门
            List<DeptEntity> allDepts = deptMapper.selectList(
                    Wrappers.<DeptEntity>lambdaQuery()
                            .eq(DeptEntity::getStatus, 1)
                            .eq(DeptEntity::getIsDeleted, 0)
            );

            // 计算该部门的总人数（本部门 + 所有下属部门）
            int totalCount = calculateTotalCount(deptId, allDepts);

            // 更新数据库
            deptMapper.update(null,
                    Wrappers.<DeptEntity>lambdaUpdate()
                            .set(DeptEntity::getEmployeeCount, totalCount)
                            .eq(DeptEntity::getId, deptId)
            );

            log.info("部门 {} 人数已更新为 {}", deptId, totalCount);
        } catch (Exception e) {
            log.error("更新部门 {} 人数失败", deptId, e);
        }
    }

    /**
     * 递归计算部门总人数（本部门 + 所有下属部门）
     */
    private int calculateTotalCount(Long deptId, List<DeptEntity> allDepts) {
        // 本部门直接员工数
        long directCount = employeeMapper.selectCount(
                Wrappers.<EmployeeEntity>lambdaQuery()
                        .eq(EmployeeEntity::getDeptId, deptId)
                        .in(EmployeeEntity::getEmploymentStatus,
                                EmploymentStatusEnum.PROBATION.getCode(),
                                EmploymentStatusEnum.FORMAL.getCode())
                        .eq(EmployeeEntity::getIsDeleted, 0)
        );

        int count = (int) directCount;

        // 递归累加所有直接子部门的员工数
        List<DeptEntity> children = allDepts.stream()
                .filter(dept -> deptId.equals(dept.getParentId()))
                .collect(Collectors.toList());

        for (DeptEntity child : children) {
            count += calculateTotalCount(child.getId(), allDepts);
        }

        return count;
    }

}

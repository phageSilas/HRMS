package com.hrms.business.employee.schedule;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.business.employee.entity.EmployeeEntity;
import com.hrms.business.employee.enums.EmploymentStatusEnum;
import com.hrms.business.employee.mapper.EmployeeMapper;
import com.hrms.system.organization.entity.DeptEntity;
import com.hrms.system.organization.mapper.DeptMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门人数定时更新任务
 * <p>
 * 每天凌晨 2 点更新 sys_dept.employee_count 字段
 * 统计口径：包含本部门及所有下属部门中状态为试用期/正式的员工总数
 * </p>
 * <p>
 * 同时实现 ApplicationRunner，在项目启动时刷新一次部门人数
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeptEmployeeCountSchedule implements ApplicationRunner {

    private final EmployeeMapper employeeMapper;
    private final DeptMapper deptMapper;

    /**
     * 每天凌晨 2 点执行
     * 生产环境建议：每天凌晨 2 点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshDeptEmployeeCount() {
        log.info("开始更新部门人数缓存...");

        // 1. 查询所有有效部门
        List<DeptEntity> allDepts = deptMapper.selectList(
                Wrappers.<DeptEntity>lambdaQuery()
                        .eq(DeptEntity::getStatus, 1)
                        .eq(DeptEntity::getIsDeleted, 0)
        );

        if (allDepts.isEmpty()) {
            log.info("没有有效部门，跳过更新");
            return;
        }

        // 2. 统计每个部门的直接员工数
        // 查询所有在职员工（试用期 + 正式）
        List<EmployeeEntity> allEmployees = employeeMapper.selectList(
                Wrappers.<EmployeeEntity>lambdaQuery()
                        .in(EmployeeEntity::getEmploymentStatus,
                                EmploymentStatusEnum.PROBATION.getCode(),
                                EmploymentStatusEnum.FORMAL.getCode())
                        .eq(EmployeeEntity::getIsDeleted, 0)
        );

        // 按 deptId 分组统计
        Map<Long, Long> directCountMap = allEmployees.stream()
                .collect(Collectors.groupingBy(
                        EmployeeEntity::getDeptId,
                        Collectors.counting()
                ));

        // 3. 计算每个部门的总人数（本部门 + 所有下属部门）
        for (DeptEntity dept : allDepts) {
            int totalCount = calculateTotalCount(dept.getId(), allDepts, directCountMap);

            // 更新数据库
            deptMapper.update(null,
                    Wrappers.<DeptEntity>lambdaUpdate()
                            .set(DeptEntity::getEmployeeCount, totalCount)
                            .eq(DeptEntity::getId, dept.getId())
            );
        }

        log.info("部门人数缓存更新完成，共更新 {} 个部门", allDepts.size());
    }

    /**
     * 项目启动时执行
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("项目启动，开始刷新部门人数...");
        refreshDeptEmployeeCount();
    }

    /**
     * 递归计算部门总人数（本部门 + 所有下属部门）
     */
    private int calculateTotalCount(Long deptId, List<DeptEntity> allDepts, Map<Long, Long> directCountMap) {
        // 本部门直接员工数
        int count = directCountMap.getOrDefault(deptId, 0L).intValue();

        // 递归累加所有直接子部门的员工数
        List<DeptEntity> children = allDepts.stream()
                .filter(dept -> deptId.equals(dept.getParentId()))
                .collect(Collectors.toList());

        for (DeptEntity child : children) {
            count += calculateTotalCount(child.getId(), allDepts, directCountMap);
        }

        return count;
    }

}

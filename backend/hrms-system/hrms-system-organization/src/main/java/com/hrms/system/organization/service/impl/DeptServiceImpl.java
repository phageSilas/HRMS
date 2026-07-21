package com.hrms.system.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.DataScopeUtils;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.system.organization.dto.DeptCreateDTO;
import com.hrms.system.organization.dto.DeptUpdateDTO;
import com.hrms.system.organization.entity.DeptEntity;
import com.hrms.system.organization.mapper.DeptMapper;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.vo.DeptDetailVO;
import com.hrms.system.organization.vo.DeptListVO;
import com.hrms.system.organization.vo.DeptTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;

    private static final int MAX_DEPT_LEVEL = 5;

    @Override
    public List<DeptTreeVO> getDeptTree() {
        // 获取所有有效部门
        List<DeptEntity> allDepts = deptMapper.selectList(
                Wrappers.<DeptEntity>lambdaQuery()
                        .eq(DeptEntity::getStatus, 1)
                        .eq(DeptEntity::getIsDeleted, 0)
                        .orderByAsc(DeptEntity::getSortNo)
        );

        if (CollectionUtils.isEmpty(allDepts)) {
            return Collections.emptyList();
        }

        // 根据数据权限过滤部门
        int dataScope = DataScopeUtils.getCurrentUserDataScope();
        Long userDeptId = SecurityContextHolder.getDeptId();
        List<DeptEntity> filteredDepts = filterDeptsByDataScope(allDepts, dataScope, userDeptId);

        if (CollectionUtils.isEmpty(filteredDepts)) {
            return Collections.emptyList();
        }

        // 按 parentId 分组
        Map<Long, List<DeptEntity>> parentMap = filteredDepts.stream()
                .collect(Collectors.groupingBy(DeptEntity::getParentId));

        // 找出所有在过滤结果中存在的部门ID
        Set<Long> visibleDeptIds = filteredDepts.stream()
                .map(DeptEntity::getId)
                .collect(Collectors.toSet());

        // 构建根节点：
        // 1. parentId=0 的部门（完整树的根）
        // 2. parentId 不在可见部门集合中的部门（子树的根，即父节点被过滤掉的情况）
        List<DeptEntity> rootDepts = filteredDepts.stream()
                .filter(dept -> dept.getParentId() == 0L || !visibleDeptIds.contains(dept.getParentId()))
                .collect(Collectors.toList());

        return rootDepts.stream()
                .map(dept -> buildTreeNode(dept, parentMap))
                .collect(Collectors.toList());
    }

    /**
     * 根据数据权限过滤部门列表
     *
     * @param allDepts   所有部门列表
     * @param dataScope  数据权限范围
     * @param userDeptId 用户所属部门ID
     * @return 过滤后的部门列表
     */
    private List<DeptEntity> filterDeptsByDataScope(List<DeptEntity> allDepts, int dataScope, Long userDeptId) {
        if (dataScope == DataScopeUtils.DATA_SCOPE_ALL) {
            // 全部权限：返回所有部门
            return allDepts;
        }

        if (userDeptId == null) {
            // 没有部门信息，返回空列表
            return Collections.emptyList();
        }

        if (dataScope == DataScopeUtils.DATA_SCOPE_SELF || dataScope == DataScopeUtils.DATA_SCOPE_DEPT) {
            // 仅本人或本部门：只返回用户所属部门
            return allDepts.stream()
                    .filter(dept -> dept.getId().equals(userDeptId))
                    .collect(Collectors.toList());
        }

        if (dataScope == DataScopeUtils.DATA_SCOPE_DEPT_AND_SUB) {
            // 本部门及下属：返回用户所属部门及其所有子部门
            List<Long> visibleDeptIds = getSubDeptIds(userDeptId, allDepts);
            return allDepts.stream()
                    .filter(dept -> visibleDeptIds.contains(dept.getId()))
                    .collect(Collectors.toList());
        }

        // 默认返回空列表
        return Collections.emptyList();
    }

    /**
     * 递归获取子部门ID列表（含自身）
     *
     * @param deptId   部门ID
     * @param allDepts 所有部门列表
     * @return 子部门ID列表
     */
    private List<Long> getSubDeptIds(Long deptId, List<DeptEntity> allDepts) {
        List<Long> result = new ArrayList<>();
        result.add(deptId);

        // 找出直接子部门
        List<DeptEntity> children = allDepts.stream()
                .filter(dept -> deptId.equals(dept.getParentId()))
                .collect(Collectors.toList());

        // 递归获取子部门的子部门
        for (DeptEntity child : children) {
            result.addAll(getSubDeptIds(child.getId(), allDepts));
        }

        return result;
    }

    /**
     * 递归构建树节点
     */
    private DeptTreeVO buildTreeNode(DeptEntity dept, Map<Long, List<DeptEntity>> parentMap) {
        DeptTreeVO vo = new DeptTreeVO();
        vo.setId(dept.getId());
        vo.setDeptName(dept.getDeptName());
        vo.setDeptCode(dept.getDeptCode());
        vo.setParentId(dept.getParentId());
        vo.setDeptLevel(dept.getDeptLevel());
        vo.setLeaderUserId(dept.getLeaderUserId());
        vo.setLeaderEmployeeId(dept.getLeaderEmployeeId());
        vo.setEmployeeCount(dept.getEmployeeCount());
        vo.setSortNo(dept.getSortNo());
        vo.setStatus(dept.getStatus());

        // 递归设置子部门
        List<DeptEntity> children = parentMap.getOrDefault(dept.getId(), Collections.emptyList());
        if (!CollectionUtils.isEmpty(children)) {
            vo.setChildren(children.stream()
                    .map(child -> buildTreeNode(child, parentMap))
                    .collect(Collectors.toList()));
        } else {
            vo.setChildren(Collections.emptyList());
        }

        return vo;
    }

    @Override
    public List<DeptListVO> getDeptList() {
        // 获取所有有效部门
        List<DeptEntity> allDepts = deptMapper.selectList(
                Wrappers.<DeptEntity>lambdaQuery()
                        .eq(DeptEntity::getStatus, 1)
                        .eq(DeptEntity::getIsDeleted, 0)
                        .orderByAsc(DeptEntity::getSortNo)
        );

        if (CollectionUtils.isEmpty(allDepts)) {
            return Collections.emptyList();
        }

        // 根据数据权限过滤部门
        int dataScope = DataScopeUtils.getCurrentUserDataScope();
        Long userDeptId = SecurityContextHolder.getDeptId();
        List<DeptEntity> filteredDepts = filterDeptsByDataScope(allDepts, dataScope, userDeptId);

        return filteredDepts.stream().map(dept -> {
            DeptListVO vo = new DeptListVO();
            vo.setId(dept.getId());
            vo.setDeptName(dept.getDeptName());
            vo.setParentId(dept.getParentId());
            vo.setDeptLevel(dept.getDeptLevel());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public DeptDetailVO getDeptById(Long id) {
        DeptEntity dept = deptMapper.selectById(id);
        if (dept == null || dept.getIsDeleted() == 1) {
            throw new GlobalException(ErrorCode.NOT_FOUND);
        }

        // 数据权限校验
        int dataScope = DataScopeUtils.getCurrentUserDataScope();
        Long userDeptId = SecurityContextHolder.getDeptId();
        checkDeptAccess(dept, dataScope, userDeptId);

        DeptDetailVO vo = new DeptDetailVO();
        vo.setId(dept.getId());
        vo.setDeptName(dept.getDeptName());
        vo.setDeptCode(dept.getDeptCode());
        vo.setParentId(dept.getParentId());
        vo.setAncestors(dept.getAncestors());
        vo.setDeptLevel(dept.getDeptLevel());
        vo.setLeaderUserId(dept.getLeaderUserId());
        vo.setLeaderEmployeeId(dept.getLeaderEmployeeId());
        vo.setEmployeeCount(dept.getEmployeeCount());
        vo.setSortNo(dept.getSortNo());
        vo.setStatus(dept.getStatus());
        vo.setRemark(dept.getRemark());
        vo.setCreateTime(dept.getCreateTime());

        // 查询上级部门名称
        if (dept.getParentId() != null && dept.getParentId() > 0) {
            DeptEntity parentDept = deptMapper.selectById(dept.getParentId());
            if (parentDept != null) {
                vo.setParentName(parentDept.getDeptName());
            }
        }

        return vo;
    }

    /**
     * 校验部门访问权限
     *
     * @param dept       部门实体
     * @param dataScope  数据权限范围
     * @param userDeptId 用户所属部门ID
     */
    private void checkDeptAccess(DeptEntity dept, int dataScope, Long userDeptId) {
        if (dataScope == DataScopeUtils.DATA_SCOPE_ALL) {
            // 全部权限：可以访问所有部门
            return;
        }

        if (userDeptId == null) {
            throw new GlobalException(ErrorCode.DATA_SCOPE_DENIED);
        }

        if (dataScope == DataScopeUtils.DATA_SCOPE_SELF || dataScope == DataScopeUtils.DATA_SCOPE_DEPT) {
            // 仅本人或本部门：只能访问自己所属部门
            if (!dept.getId().equals(userDeptId)) {
                throw new GlobalException(ErrorCode.DATA_SCOPE_DENIED);
            }
            return;
        }

        if (dataScope == DataScopeUtils.DATA_SCOPE_DEPT_AND_SUB) {
            // 本部门及下属：只能访问自己所属部门及其子部门
            List<DeptEntity> allDepts = deptMapper.selectList(
                    Wrappers.<DeptEntity>lambdaQuery()
                            .eq(DeptEntity::getStatus, 1)
                            .eq(DeptEntity::getIsDeleted, 0)
            );
            List<Long> visibleDeptIds = getSubDeptIds(userDeptId, allDepts);
            if (!visibleDeptIds.contains(dept.getId())) {
                throw new GlobalException(ErrorCode.DATA_SCOPE_DENIED);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDept(DeptCreateDTO createDTO) {
        // 校验部门编码唯一性
        Long count = deptMapper.selectCount(
                Wrappers.<DeptEntity>lambdaQuery()
                        .eq(DeptEntity::getDeptCode, createDTO.getDeptCode())
        );
        if (count > 0) {
            throw new GlobalException(ErrorCode.DEPT_CODE_EXISTS);
        }

        DeptEntity dept = new DeptEntity();
        dept.setDeptName(createDTO.getDeptName());
        dept.setDeptCode(createDTO.getDeptCode());
        dept.setLeaderEmployeeId(createDTO.getLeaderEmployeeId());
        dept.setSortNo(createDTO.getSortNo());
        dept.setRemark(createDTO.getRemark());
        dept.setStatus(1);
        dept.setEmployeeCount(0);

        // 处理 parentId
        Long parentId = createDTO.getParentId();
        if (parentId == null || parentId == 0) {
            // 根部门
            dept.setParentId(0L);
            dept.setDeptLevel(1);
            dept.setAncestors("");
        } else {
            // 子部门
            DeptEntity parentDept = deptMapper.selectById(parentId);
            if (parentDept == null || parentDept.getIsDeleted() == 1) {
                throw new GlobalException(ErrorCode.DEPT_PARENT_NOT_FOUND);
            }

            // 校验层级限制
            int newLevel = parentDept.getDeptLevel() + 1;
            if (newLevel > MAX_DEPT_LEVEL) {
                throw new GlobalException(ErrorCode.DEPT_LEVEL_EXCEED);
            }

            dept.setParentId(parentId);
            dept.setDeptLevel(newLevel);

            // 计算祖级路径
            String ancestors = parentDept.getAncestors();
            if (StringUtils.hasText(ancestors)) {
                dept.setAncestors(ancestors + "," + parentId);
            } else {
                dept.setAncestors(String.valueOf(parentId));
            }
        }

        deptMapper.insert(dept);
        return dept.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(Long id, DeptUpdateDTO updateDTO) {
        DeptEntity dept = deptMapper.selectById(id);
        if (dept == null || dept.getIsDeleted() == 1) {
            throw new GlobalException(ErrorCode.NOT_FOUND);
        }

        // 数据权限校验
        int dataScope = DataScopeUtils.getCurrentUserDataScope();
        Long userDeptId = SecurityContextHolder.getDeptId();
        checkDeptAccess(dept, dataScope, userDeptId);

        dept.setDeptName(updateDTO.getDeptName());
        dept.setLeaderEmployeeId(updateDTO.getLeaderEmployeeId());
        dept.setSortNo(updateDTO.getSortNo());
        dept.setRemark(updateDTO.getRemark());

        deptMapper.updateById(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id) {
        DeptEntity dept = deptMapper.selectById(id);
        if (dept == null || dept.getIsDeleted() == 1) {
            throw new GlobalException(ErrorCode.NOT_FOUND);
        }

        // 数据权限校验
        int dataScope = DataScopeUtils.getCurrentUserDataScope();
        Long userDeptId = SecurityContextHolder.getDeptId();
        checkDeptAccess(dept, dataScope, userDeptId);

        // 检查是否有子部门
        Long childCount = deptMapper.selectCount(
                Wrappers.<DeptEntity>lambdaQuery()
                        .eq(DeptEntity::getParentId, id)
                        .eq(DeptEntity::getIsDeleted, 0)
        );
        if (childCount > 0) {
            throw new GlobalException(ErrorCode.DEPT_HAS_CHILDREN);
        }

        // 注意：部门下是否有在职员工的校验由调用方（前端/客户端）负责
        // 原因：
        // 1. organization模块不能反向依赖employee模块（避免循环依赖）
        // 2. 前端应在删除部门前先调用 /api/v1/employees/check-dept?deptId={id} 接口校验
        // 3. 如果该校验接口返回 true（有在职员工），前端应阻止删除操作并提示用户
        // 4. 后端此处仅校验子部门，不重复校验员工（避免跨模块耦合）

        deptMapper.deleteById(id);
    }

    @Override
    public List<Long> getSubDeptIds(Long parentId) {
        List<Long> result = new ArrayList<>();
        result.add(parentId);

        List<DeptEntity> children = deptMapper.selectList(
                Wrappers.<DeptEntity>lambdaQuery()
                        .eq(DeptEntity::getParentId, parentId)
                        .eq(DeptEntity::getStatus, 1)
                        .eq(DeptEntity::getIsDeleted, 0)
        );

        for (DeptEntity child : children) {
            result.addAll(getSubDeptIds(child.getId()));
        }

        return result;
    }

}

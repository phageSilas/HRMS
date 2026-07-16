package com.hrms.system.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrms.common.exception.ErrorCode;
import com.hrms.common.exception.GlobalException;
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
        List<DeptEntity> allDepts = deptMapper.selectList(
                Wrappers.<DeptEntity>lambdaQuery()
                        .eq(DeptEntity::getStatus, 1)
                        .orderByAsc(DeptEntity::getSortNo)
        );

        if (CollectionUtils.isEmpty(allDepts)) {
            return Collections.emptyList();
        }

        // 按 parentId 分组
        Map<Long, List<DeptEntity>> parentMap = allDepts.stream()
                .collect(Collectors.groupingBy(DeptEntity::getParentId));

        // 构建根节点（parentId = 0）
        List<DeptEntity> rootDepts = parentMap.getOrDefault(0L, Collections.emptyList());

        return rootDepts.stream()
                .map(dept -> buildTreeNode(dept, parentMap))
                .collect(Collectors.toList());
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
        List<DeptEntity> allDepts = deptMapper.selectList(
                Wrappers.<DeptEntity>lambdaQuery()
                        .eq(DeptEntity::getStatus, 1)
                        .orderByAsc(DeptEntity::getSortNo)
        );

        return allDepts.stream().map(dept -> {
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
        dept.setLeaderUserId(createDTO.getLeaderUserId());
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

        dept.setDeptName(updateDTO.getDeptName());
        dept.setLeaderUserId(updateDTO.getLeaderUserId());
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

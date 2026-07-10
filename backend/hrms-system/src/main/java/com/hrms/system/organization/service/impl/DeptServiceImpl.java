package com.hrms.system.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ErrorCode;
import com.hrms.system.organization.dto.DeptCreateDTO;
import com.hrms.system.organization.entity.DeptDO;
import com.hrms.system.organization.mapper.DeptMapper;
import com.hrms.system.organization.service.DeptService;
import com.hrms.system.organization.vo.DeptTreeVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门服务实现类。
 */
@Service
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;

    public DeptServiceImpl(DeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    @Override
    public Long create(DeptCreateDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getDeptName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "部门名称不能为空");
        }

        // 校验部门编码是否已存在
        if (StringUtils.hasText(dto.getDeptCode())) {
            LambdaQueryWrapper<DeptDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeptDO::getDeptCode, dto.getDeptCode());
            if (deptMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "部门编码已存在");
            }
        }

        DeptDO dept = new DeptDO();
        dept.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        dept.setDeptName(dto.getDeptName());
        dept.setDeptCode(dto.getDeptCode());
        dept.setLeaderUserId(dto.getLeaderUserId());
        dept.setLeaderEmployeeId(dto.getLeaderEmployeeId());
        dept.setSortNo(dto.getSortNo() != null ? dto.getSortNo() : 0);
        dept.setDescription(dto.getDescription());
        dept.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        dept.setEmployeeCount(0);
        dept.setRemark(dto.getRemark());

        // 计算层级和祖级路径
        if (dept.getParentId() == 0L) {
            dept.setDeptLevel(1);
            dept.setAncestors("0");
        } else {
            DeptDO parent = deptMapper.selectById(dept.getParentId());
            if (parent == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "上级部门不存在");
            }
            dept.setDeptLevel(parent.getDeptLevel() + 1);
            dept.setAncestors(parent.getAncestors() + "," + parent.getId());
        }

        deptMapper.insert(dept);
        return dept.getId();
    }

    @Override
    public void update(Long id, DeptCreateDTO dto) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "部门 ID 不能为空");
        }

        DeptDO dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "部门不存在");
        }

        if (StringUtils.hasText(dto.getDeptName())) {
            dept.setDeptName(dto.getDeptName());
        }
        if (dto.getLeaderUserId() != null) {
            dept.setLeaderUserId(dto.getLeaderUserId());
        }
        if (dto.getLeaderEmployeeId() != null) {
            dept.setLeaderEmployeeId(dto.getLeaderEmployeeId());
        }
        if (dto.getSortNo() != null) {
            dept.setSortNo(dto.getSortNo());
        }
        if (dto.getDescription() != null) {
            dept.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            dept.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            dept.setRemark(dto.getRemark());
        }

        deptMapper.updateById(dept);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "部门 ID 不能为空");
        }

        // 检查是否有子部门
        LambdaQueryWrapper<DeptDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeptDO::getParentId, id);
        if (deptMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该部门下存在子部门，请先删除子部门");
        }

        deptMapper.deleteById(id);
    }

    @Override
    public List<DeptTreeVO> tree() {
        // 查询所有部门
        List<DeptDO> allDepts = deptMapper.selectList(null);

        // 转换为 VO
        List<DeptTreeVO> voList = allDepts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildTree(voList, 0L);
    }

    @Override
    public DeptTreeVO getById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "部门 ID 不能为空");
        }

        DeptDO dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "部门不存在");
        }

        return convertToVO(dept);
    }

    /**
     * 转换为 VO。
     */
    private DeptTreeVO convertToVO(DeptDO dept) {
        DeptTreeVO vo = new DeptTreeVO();
        vo.setId(dept.getId());
        vo.setParentId(dept.getParentId());
        vo.setDeptName(dept.getDeptName());
        vo.setDeptCode(dept.getDeptCode());
        vo.setLeaderUserId(dept.getLeaderUserId());
        vo.setDeptLevel(dept.getDeptLevel());
        vo.setSortNo(dept.getSortNo());
        vo.setStatus(dept.getStatus());
        return vo;
    }

    /**
     * 构建树形结构。
     */
    private List<DeptTreeVO> buildTree(List<DeptTreeVO> allDepts, Long parentId) {
        List<DeptTreeVO> result = new ArrayList<>();

        for (DeptTreeVO dept : allDepts) {
            if (dept.getParentId().equals(parentId)) {
                dept.setChildren(buildTree(allDepts, dept.getId()));
                result.add(dept);
            }
        }

        return result;
    }
}
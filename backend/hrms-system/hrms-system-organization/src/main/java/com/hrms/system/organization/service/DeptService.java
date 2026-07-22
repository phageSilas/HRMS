package com.hrms.system.organization.service;

import com.hrms.system.organization.dto.DeptCreateDTO;
import com.hrms.system.organization.dto.DeptMergeDTO;
import com.hrms.system.organization.dto.DeptUpdateDTO;
import com.hrms.system.organization.entity.DeptEntity;
import com.hrms.system.organization.vo.DeptDetailVO;
import com.hrms.system.organization.vo.DeptListVO;
import com.hrms.system.organization.vo.DeptTreeVO;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DeptService {

    /**
     * 获取部门树
     *
     * @return 部门树列表
     */
    List<DeptTreeVO> getDeptTree();

    /**
     * 获取部门平铺列表
     *
     * @return 部门平铺列表
     */
    List<DeptListVO> getDeptList();

    /**
     * 根据 ID 查询部门详情
     *
     * @param id 部门 ID
     * @return 部门详情
     */
    DeptDetailVO getDeptById(Long id);

    /**
     * 创建部门
     *
     * @param createDTO 创建部门 DTO
     * @return 创建的部门 ID
     */
    Long createDept(DeptCreateDTO createDTO);

    /**
     * 更新部门
     *
     * @param id        部门 ID
     * @param updateDTO 更新部门 DTO
     */
    void updateDept(Long id, DeptUpdateDTO updateDTO);

    /**
     * 删除部门
     *
     * @param id 部门 ID
     */
    void deleteDept(Long id);

    /**
     * 递归获取子部门 ID 列表（含自身）
     *
     * @param parentId 父部门 ID
     * @return 子部门 ID 列表
     */
    List<Long> getSubDeptIds(Long parentId);

    /**
     * 合并部门
     * <p>
     * 将源部门的员工迁移到目标部门，然后删除源部门。
     * 只有叶子部门（无子部门）才能被合并。
     * </p>
     *
     * @param sourceDeptId 源部门 ID（将被删除）
     * @param mergeDTO     合并参数（包含目标部门 ID）
     */
    void mergeDept(Long sourceDeptId, DeptMergeDTO mergeDTO);

}

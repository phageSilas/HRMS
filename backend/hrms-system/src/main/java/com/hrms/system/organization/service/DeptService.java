package com.hrms.system.organization.service;

import com.hrms.system.organization.dto.DeptCreateDTO;
import com.hrms.system.organization.vo.DeptTreeVO;

import java.util.List;

/**
 * 部门服务接口。
 */
public interface DeptService {

    /**
     * 创建部门。
     *
     * @param dto 部门创建请求
     * @return 部门 ID
     */
    Long create(DeptCreateDTO dto);

    /**
     * 更新部门。
     *
     * @param id  部门 ID
     * @param dto 部门更新请求
     */
    void update(Long id, DeptCreateDTO dto);

    /**
     * 删除部门。
     *
     * @param id 部门 ID
     */
    void delete(Long id);

    /**
     * 查询部门树。
     *
     * @return 部门树
     */
    List<DeptTreeVO> tree();

    /**
     * 查询部门详情。
     *
     * @param id 部门 ID
     * @return 部门详情
     */
    DeptTreeVO getById(Long id);
}
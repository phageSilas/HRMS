package com.hrms.system.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.system.auth.dto.RoleMenuAssignDTO;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 创建角色
     *
     * @param role 角色实体
     * @return 角色 ID
     */
    Long create(RoleEntity role);

    /**
     * 更新角色
     *
     * @param role 角色实体
     */
    void update(RoleEntity role);

    /**
     * 删除角色
     *
     * @param id 角色 ID
     */
    void delete(Long id);

    /**
     * 根据 ID 查询角色
     *
     * @param id 角色 ID
     * @return 角色实体
     */
    RoleEntity getById(Long id);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<RoleEntity> list();

    /**
     * 分页查询角色
     *
     * @param page     分页参数
     * @param roleName 角色名称（可选）
     * @param status   状态（可选）
     * @return 分页结果
     */
    Page<RoleEntity> page(Page<RoleEntity> page, String roleName, Integer status);

    /**
     * 根据用户 ID 查询角色列表
     *
     * @param userId 用户 ID
     * @return 角色列表
     */
    List<RoleEntity> getRolesByUserId(Long userId);

    /**
     * 获取用户的数据权限范围（取最大值）
     *
     * @param userId 用户 ID
     * @return 数据权限范围（1-仅本人 2-本部门 3-本部门及子部门 4-全部）
     */
    Integer getDataScope(Long userId);

    /**
     * 查询所有角色VO列表（包含菜单ID）
     *
     * @param keyword 角色名称关键字（可选）
     * @param status  状态（可选）
     * @return 角色VO列表
     */
    List<RoleVO> listRoleVOs(String keyword, Integer status);

    /**
     * 分配角色菜单权限
     *
     * @param roleId          角色ID
     * @param roleMenuAssignDTO 分配参数
     */
    void assignMenus(Long roleId, RoleMenuAssignDTO roleMenuAssignDTO);

}

package com.hrms.system.auth.service;

import com.hrms.system.auth.dto.RoleCreateDTO;
import com.hrms.system.auth.dto.RoleQueryDTO;
import com.hrms.system.auth.dto.RoleUpdateDTO;
import com.hrms.system.auth.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口。
 */
public interface RoleService {

    /**
     * 创建角色。
     *
     * @param dto 角色创建请求
     * @return 角色 ID
     */
    Long create(RoleCreateDTO dto);

    /**
     * 更新角色。
     *
     * @param dto 角色更新请求
     */
    void update(RoleUpdateDTO dto);

    /**
     * 删除角色。
     *
     * @param id 角色 ID
     */
    void delete(Long id);

    /**
     * 查询角色详情。
     *
     * @param id 角色 ID
     * @return 角色详情
     */
    RoleVO getById(Long id);

    /**
     * 查询角色列表。
     *
     * @param dto 查询条件
     * @return 角色列表
     */
    List<RoleVO> list(RoleQueryDTO dto);

    /**
     * 为角色分配菜单。
     *
     * @param roleId  角色 ID
     * @param menuIds 菜单 ID 列表
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 为角色分配用户。
     *
     * @param roleId  角色 ID
     * @param userIds 用户 ID 列表
     */
    void assignUsers(Long roleId, List<Long> userIds);
}
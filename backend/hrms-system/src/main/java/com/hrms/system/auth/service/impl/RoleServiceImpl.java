package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ErrorCode;
import com.hrms.system.auth.dto.RoleCreateDTO;
import com.hrms.system.auth.dto.RoleQueryDTO;
import com.hrms.system.auth.dto.RoleUpdateDTO;
import com.hrms.system.auth.entity.MenuDO;
import com.hrms.system.auth.entity.RoleDO;
import com.hrms.system.auth.entity.RoleMenuDO;
import com.hrms.system.auth.entity.UserRoleDO;
import com.hrms.system.auth.mapper.MenuMapper;
import com.hrms.system.auth.mapper.RoleMapper;
import com.hrms.system.auth.mapper.RoleMenuMapper;
import com.hrms.system.auth.mapper.UserRoleMapper;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.auth.vo.RoleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类。
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;
    private final MenuMapper menuMapper;

    public RoleServiceImpl(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper,
                           UserRoleMapper userRoleMapper, MenuMapper menuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    @Transactional
    public Long create(RoleCreateDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getRoleCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色编码不能为空");
        }

        // 校验角色编码是否已存在
        LambdaQueryWrapper<RoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleDO::getRoleCode, dto.getRoleCode());
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色编码已存在");
        }

        RoleDO role = new RoleDO();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDataScope(dto.getDataScope() != null ? dto.getDataScope() : 1);
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        role.setSortNo(dto.getSortNo() != null ? dto.getSortNo() : 0);
        role.setRemark(dto.getRemark());

        roleMapper.insert(role);

        // 分配菜单
        if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) {
            assignMenus(role.getId(), dto.getMenuIds());
        }

        return role.getId();
    }

    @Override
    @Transactional
    public void update(RoleUpdateDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色 ID 不能为空");
        }

        RoleDO role = roleMapper.selectById(dto.getId());
        if (role == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色不存在");
        }

        if (StringUtils.hasText(dto.getRoleName())) {
            role.setRoleName(dto.getRoleName());
        }
        if (dto.getDataScope() != null) {
            role.setDataScope(dto.getDataScope());
        }
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }
        if (dto.getSortNo() != null) {
            role.setSortNo(dto.getSortNo());
        }
        if (dto.getRemark() != null) {
            role.setRemark(dto.getRemark());
        }

        roleMapper.updateById(role);

        // 更新菜单分配
        if (dto.getMenuIds() != null) {
            assignMenus(dto.getId(), dto.getMenuIds());
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色 ID 不能为空");
        }

        RoleDO role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色不存在");
        }

        // 删除角色菜单关联
        LambdaQueryWrapper<RoleMenuDO> rmWrapper = new LambdaQueryWrapper<>();
        rmWrapper.eq(RoleMenuDO::getRoleId, id);
        roleMenuMapper.delete(rmWrapper);

        // 删除用户角色关联
        LambdaQueryWrapper<UserRoleDO> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(UserRoleDO::getRoleId, id);
        userRoleMapper.delete(urWrapper);

        // 删除角色
        roleMapper.deleteById(id);
    }

    @Override
    public RoleVO getById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色 ID 不能为空");
        }

        RoleDO role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色不存在");
        }

        return convertToVO(role);
    }

    @Override
    public List<RoleVO> list(RoleQueryDTO dto) {
        LambdaQueryWrapper<RoleDO> wrapper = new LambdaQueryWrapper<>();
        if (dto != null) {
            if (StringUtils.hasText(dto.getRoleName())) {
                wrapper.like(RoleDO::getRoleName, dto.getRoleName());
            }
            if (StringUtils.hasText(dto.getRoleCode())) {
                wrapper.like(RoleDO::getRoleCode, dto.getRoleCode());
            }
            if (dto.getStatus() != null) {
                wrapper.eq(RoleDO::getStatus, dto.getStatus());
            }
        }
        wrapper.orderByAsc(RoleDO::getSortNo);

        List<RoleDO> roles = roleMapper.selectList(wrapper);
        return roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色 ID 不能为空");
        }

        // 删除旧的菜单关联
        LambdaQueryWrapper<RoleMenuDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenuDO::getRoleId, roleId);
        roleMenuMapper.delete(wrapper);

        // 创建新的菜单关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenuDO rm = new RoleMenuDO();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }

    @Override
    @Transactional
    public void assignUsers(Long roleId, List<Long> userIds) {
        if (roleId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色 ID 不能为空");
        }

        // 删除旧的用户关联
        LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRoleDO::getRoleId, roleId);
        userRoleMapper.delete(wrapper);

        // 创建新的用户关联
        if (userIds != null && !userIds.isEmpty()) {
            for (Long userId : userIds) {
                UserRoleDO ur = new UserRoleDO();
                ur.setRoleId(roleId);
                ur.setUserId(userId);
                userRoleMapper.insert(ur);
            }
        }
    }

    private RoleVO convertToVO(RoleDO role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDataScope(role.getDataScope());
        vo.setStatus(role.getStatus());
        vo.setSortNo(role.getSortNo());
        vo.setRemark(role.getRemark());
        return vo;
    }
}
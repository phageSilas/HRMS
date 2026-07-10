package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ErrorCode;
import com.hrms.system.auth.entity.MenuDO;
import com.hrms.system.auth.entity.RoleDO;
import com.hrms.system.auth.entity.UserDO;
import com.hrms.system.auth.entity.UserRoleDO;
import com.hrms.system.auth.mapper.MenuMapper;
import com.hrms.system.auth.mapper.RoleMapper;
import com.hrms.system.auth.mapper.UserMapper;
import com.hrms.system.auth.mapper.UserRoleMapper;
import com.hrms.system.auth.service.PermissionService;
import com.hrms.system.auth.vo.DataScopeVO;
import com.hrms.system.auth.vo.FieldPermissionVO;
import com.hrms.system.auth.vo.PermissionVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务实现类。
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;

    public PermissionServiceImpl(UserMapper userMapper, UserRoleMapper userRoleMapper,
                                  RoleMapper roleMapper, MenuMapper menuMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public PermissionVO getUserPermissions(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户 ID 不能为空");
        }

        // 查询用户
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户不存在");
        }

        PermissionVO vo = new PermissionVO();
        vo.setUserId(userId);
        vo.setUsername(user.getUsername());

        // 查询用户角色
        List<String> roleCodes = getRoleCodes(userId);
        vo.setRoleCodes(roleCodes);

        // 查询用户权限
        List<String> permissions = menuMapper.selectPermissionsByUserId(userId);
        vo.setPermissions(permissions != null ? permissions : new ArrayList<>());

        return vo;
    }

    @Override
    public DataScopeVO getDataScope(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户 ID 不能为空");
        }

        // 查询用户的角色
        LambdaQueryWrapper<UserRoleDO> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(UserRoleDO::getUserId, userId);
        List<UserRoleDO> userRoles = userRoleMapper.selectList(urWrapper);

        if (userRoles == null || userRoles.isEmpty()) {
            DataScopeVO vo = new DataScopeVO();
            vo.setScopeType(1); // 默认仅本人
            return vo;
        }

        // 获取角色的数据权限范围（取最大范围）
        Integer maxScope = 1;
        for (UserRoleDO ur : userRoles) {
            RoleDO role = roleMapper.selectById(ur.getRoleId());
            if (role != null && role.getDataScope() != null && role.getDataScope() > maxScope) {
                maxScope = role.getDataScope();
            }
        }

        DataScopeVO vo = new DataScopeVO();
        vo.setScopeType(maxScope);

        // TODO: 根据 scopeType 查询部门 ID 列表（需要关联部门表）
        // 这里暂时返回空列表，后续实现部门模块后再补充
        vo.setDepartmentIds(new ArrayList<>());

        return vo;
    }

    @Override
    public FieldPermissionVO getFieldPermissions(Long userId, String module) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户 ID 不能为空");
        }

        // TODO: 实现字段级权限控制
        // 这里暂时返回全部字段可见可编辑，后续根据需求实现

        FieldPermissionVO vo = new FieldPermissionVO();
        vo.setViewableFields(new ArrayList<>());
        vo.setEditableFields(new ArrayList<>());
        vo.setFlowRequiredFields(new ArrayList<>());

        return vo;
    }

    /**
     * 获取用户角色编码列表。
     */
    private List<String> getRoleCodes(Long userId) {
        LambdaQueryWrapper<UserRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRoleDO::getUserId, userId);
        List<UserRoleDO> userRoles = userRoleMapper.selectList(wrapper);

        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        return userRoles.stream()
                .map(ur -> {
                    RoleDO role = roleMapper.selectById(ur.getRoleId());
                    return role != null ? role.getRoleCode() : null;
                })
                .filter(code -> code != null)
                .collect(Collectors.toList());
    }
}
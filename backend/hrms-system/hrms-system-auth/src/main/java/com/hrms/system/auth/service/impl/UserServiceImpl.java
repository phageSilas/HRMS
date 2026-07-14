package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.web.PageResult;
import com.hrms.system.auth.dto.UserQueryDTO;
import com.hrms.system.auth.entity.RoleEntity;
import com.hrms.system.auth.entity.UserEntity;
import com.hrms.system.auth.entity.UserRoleEntity;
import com.hrms.system.auth.mapper.UserMapper;
import com.hrms.system.auth.mapper.UserRoleMapper;
import com.hrms.system.auth.service.RoleService;
import com.hrms.system.auth.service.UserService;
import com.hrms.system.auth.vo.UserListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleService roleService;

    @Override
    public PageResult<UserListVO> listUsers(UserQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getIsDeleted, 0);

        // 关键词搜索（匹配用户名/真实姓名/手机号）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword();
            wrapper.and(w -> w.like(UserEntity::getUsername, keyword)
                    .or()
                    .like(UserEntity::getRealName, keyword)
                    .or()
                    .like(UserEntity::getPhone, keyword));
        }

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(UserEntity::getStatus, queryDTO.getStatus());
        }

        // 部门筛选（如果用户表有deptId字段）
        // TODO: 用户表目前没有deptId字段，需要在UserEntity中添加

        // 按创建时间降序
        wrapper.orderByDesc(UserEntity::getCreateTime);

        // 执行分页查询
        Page<UserEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<UserEntity> userPage = userMapper.selectPage(page, wrapper);

        // 查询所有用户的角色信息
        List<Long> userIds = userPage.getRecords().stream()
                .map(UserEntity::getId)
                .collect(Collectors.toList());

        // 批量查询用户角色关联
        Map<Long, List<Long>> userRoleMap = userIds.isEmpty() ? Map.of() :
                userRoleMapper.selectList(
                                new LambdaQueryWrapper<UserRoleEntity>()
                                        .in(UserRoleEntity::getUserId, userIds))
                        .stream()
                        .collect(Collectors.groupingBy(
                                UserRoleEntity::getUserId,
                                Collectors.mapping(UserRoleEntity::getRoleId, Collectors.toList())
                        ));

        // 查询所有角色信息
        List<RoleEntity> allRoles = roleService.list();
        Map<Long, String> roleNameMap = allRoles.stream()
                .collect(Collectors.toMap(RoleEntity::getId, RoleEntity::getRoleName));

        // 转换为VO
        List<UserListVO> records = userPage.getRecords().stream().map(user -> {
            UserListVO vo = new UserListVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setStatus(user.getStatus());
            vo.setEmployeeId(user.getEmployeeId());
            // TODO: employeeNo 和 deptName 需要从员工模块查询
            vo.setLastLoginTime(user.getLastLoginTime());
            vo.setCreateTime(user.getCreateTime());

            // 设置角色名称列表
            List<Long> roleIds = userRoleMap.getOrDefault(user.getId(), List.of());
            List<String> roleNames = roleIds.stream()
                    .map(roleNameMap::get)
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            vo.setRoleNames(roleNames);

            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(records, userPage.getTotal(), (int) userPage.getCurrent(), (int) userPage.getSize());
    }

}

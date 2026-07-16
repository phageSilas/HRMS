package com.hrms.system.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.system.auth.entity.FieldPermissionEntity;
import com.hrms.system.auth.mapper.FieldPermissionMapper;
import com.hrms.system.auth.service.FieldPermissionService;
import com.hrms.system.auth.vo.FieldPermissionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字段权限服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FieldPermissionServiceImpl implements FieldPermissionService {

    private final FieldPermissionMapper fieldPermissionMapper;

    @Override
    public FieldPermissionVO getFieldPermissions(String bizType, List<Long> roleIds) {
        FieldPermissionVO vo = new FieldPermissionVO();
        vo.setBizType(bizType);

        // 如果没有角色ID，返回默认权限（全部可见可编辑）
        if (CollectionUtils.isEmpty(roleIds)) {
            vo.setViewableFields(List.of("*"));
            vo.setEditableFields(List.of("*"));
            vo.setFlowRequiredFields(List.of());
            return vo;
        }

        // 查询字段权限配置
        LambdaQueryWrapper<FieldPermissionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldPermissionEntity::getBizType, bizType)
                .in(FieldPermissionEntity::getRoleId, roleIds);

        List<FieldPermissionEntity> permissions = fieldPermissionMapper.selectList(wrapper);

        // 如果没有配置，返回默认权限（全部可见可编辑）
        if (CollectionUtils.isEmpty(permissions)) {
            vo.setViewableFields(List.of("*"));
            vo.setEditableFields(List.of("*"));
            vo.setFlowRequiredFields(List.of());
            return vo;
        }

        // 按权限类型分组
        List<String> viewableFields = permissions.stream()
                .filter(p -> p.getViewable() != null && p.getViewable() == 1)
                .map(FieldPermissionEntity::getFieldName)
                .distinct()
                .collect(Collectors.toList());

        List<String> editableFields = permissions.stream()
                .filter(p -> p.getEditable() != null && p.getEditable() == 1)
                .map(FieldPermissionEntity::getFieldName)
                .distinct()
                .collect(Collectors.toList());

        List<String> flowRequiredFields = permissions.stream()
                .filter(p -> p.getFlowRequired() != null && p.getFlowRequired() == 1)
                .map(FieldPermissionEntity::getFieldName)
                .distinct()
                .collect(Collectors.toList());

        vo.setViewableFields(viewableFields);
        vo.setEditableFields(editableFields);
        vo.setFlowRequiredFields(flowRequiredFields);

        return vo;
    }

}
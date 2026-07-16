package com.hrms.system.auth.service;

import com.hrms.system.auth.vo.FieldPermissionVO;

import java.util.List;

/**
 * 字段权限服务接口
 */
public interface FieldPermissionService {

    /**
     * 根据业务类型和用户角色获取字段权限
     *
     * @param bizType 业务类型
     * @param roleIds 角色ID列表
     * @return 字段权限VO
     */
    FieldPermissionVO getFieldPermissions(String bizType, List<Long> roleIds);

}
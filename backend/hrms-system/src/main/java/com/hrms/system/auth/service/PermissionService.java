package com.hrms.system.auth.service;

import com.hrms.system.auth.vo.DataScopeVO;
import com.hrms.system.auth.vo.FieldPermissionVO;
import com.hrms.system.auth.vo.PermissionVO;

/**
 * 权限服务接口。
 */
public interface PermissionService {

    /**
     * 获取用户权限列表。
     *
     * @param userId 用户 ID
     * @return 权限信息
     */
    PermissionVO getUserPermissions(Long userId);

    /**
     * 获取数据权限范围。
     *
     * @param userId 用户 ID
     * @return 数据权限范围
     */
    DataScopeVO getDataScope(Long userId);

    /**
     * 获取字段权限。
     *
     * @param userId 用户 ID
     * @param module 模块名称（如 employee、salary）
     * @return 字段权限
     */
    FieldPermissionVO getFieldPermissions(Long userId, String module);
}
package com.hrms.business.personnel.context;

import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.security.UserContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 入转调离模块当前登录用户上下文适配器
 */
@Component
public class PersonnelUserContext {

    /**
     * 获取当前登录用户上下文。
     *
     * @return 当前登录用户上下文
     * 本方法使用的工具类: SecurityContextHolder(hrms-common)
     */
    public UserContext getRequiredContext() {
        return SecurityContextHolder.getContext();
    }

    /**
     * 获取当前登录用户ID。
     *
     * @return 当前登录用户ID
     * 本方法使用的工具类: SecurityContextHolder(hrms-common)
     */
    public Long getCurrentUserId() {
        return getRequiredContext().getUserId();
    }

    /**
     * 获取当前登录用户名。
     *
     * @return 当前登录用户名
     * 本方法使用的工具类: SecurityContextHolder(hrms-common)
     */
    public String getCurrentUsername() {
        return getRequiredContext().getUsername();
    }

    /**
     * 获取当前登录用户部门ID。
     *
     * @return 当前登录用户部门ID
     * 本方法使用的工具类: SecurityContextHolder(hrms-common)
     */
    public Long getCurrentDeptId() {
        return getRequiredContext().getDeptId();
    }

    /**
     * 获取当前登录用户角色ID列表。
     *
     * @return 当前登录用户角色ID列表
     * 本方法使用的工具类: Collections(JDK)
     */
    public List<Long> getCurrentRoleIds() {
        List<Long> roleIds = getRequiredContext().getRoleIds();
        if (roleIds == null) {
            return Collections.emptyList();
        }
        return roleIds;
    }

    /**
     * 获取当前登录用户角色编码列表。
     *
     * @return 当前登录用户角色编码列表
     * 本方法使用的工具类: Collections(JDK)
     */
    public List<String> getCurrentRoleCodes() {
        List<String> roleCodes = getRequiredContext().getRoleCodes();
        if (roleCodes == null) {
            return Collections.emptyList();
        }
        return roleCodes;
    }

    /**
     * 获取当前登录用户权限码列表。
     *
     * @return 当前登录用户权限码列表
     * 本方法使用的工具类: Collections(JDK)
     */
    public List<String> getCurrentPermissions() {
        List<String> permissions = getRequiredContext().getPermissions();
        if (permissions == null) {
            return Collections.emptyList();
        }
        return permissions;
    }

}

package com.hrms.business.personnel.context;

import com.hrms.common.exception.GlobalException;
import com.hrms.common.security.SecurityContextHolder;
import com.hrms.common.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 入转调离模块用户上下文测试
 */
class PersonnelUserContextTest {

    private final PersonnelUserContext personnelUserContext = new PersonnelUserContext();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    /**
     * 验证能从统一安全上下文读取当前登录用户。
     *
     * 本方法使用的工具类: SecurityContextHolder(hrms-common),UserContext(hrms-common)
     */
    @Test
    void shouldReadCurrentUserFromCommonSecurityContext() {
        UserContext context = new UserContext();
        context.setUserId(1001L);
        context.setUsername("hr_admin");
        context.setDeptId(20L);
        context.setRoleIds(List.of(1L, 2L));
        SecurityContextHolder.setContext(context);

        assertEquals(1001L, personnelUserContext.getCurrentUserId());
        assertEquals("hr_admin", personnelUserContext.getCurrentUsername());
        assertEquals(20L, personnelUserContext.getCurrentDeptId());
        assertEquals(List.of(1L, 2L), personnelUserContext.getCurrentRoleIds());
    }

    /**
     * 验证未登录时继续沿用统一未认证异常。
     *
     * 本方法使用的工具类: SecurityContextHolder(hrms-common)
     */
    @Test
    void shouldThrowUnauthorizedWhenCommonSecurityContextMissing() {
        SecurityContextHolder.clear();

        assertThrows(GlobalException.class, personnelUserContext::getRequiredContext);
    }

}

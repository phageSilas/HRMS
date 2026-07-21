package com.hrms.business.attendance.common.constant;

import java.util.Set;

public class AttendanceServiceConstant {

    // 最大查询31天
    public static final long GROUP_RECORD_MAX_DAYS = 31L;

    // 首页排名默认显示数量
    public static final int DASHBOARD_RANKING_LIMIT = 10;

    // 首页全量数据角色
    public static final Set<String> DASHBOARD_FULL_SCOPE_ROLE_CODES = Set.of("HR", "HR_TEST", "ADMIN", "ROLE_ADMIN");

    // 请假管理全量数据角色
    public static final Set<String> LEAVE_MANAGE_FULL_SCOPE_ROLE_CODES = Set.of("HR", "HR_TEST", "ADMIN", "ROLE_ADMIN");

    // 请假管理权限角色
    public static final String LEAVE_MANAGE_MANAGER_ROLE_CODE = "MANAGER";

    // 集团权限范围部门
    public static final String GROUP_SCOPE_DEPT = "DEPT";

    // 集团权限范围职位
    public static final String GROUP_SCOPE_POST = "POST";

    // 集团权限范围员工
    public static final String GROUP_SCOPE_EMPLOYEE = "EMPLOYEE";
}

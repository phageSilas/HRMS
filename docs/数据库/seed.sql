-- ============================================================
-- 初始化数据脚本（精简版 - 修复主键冲突）
-- ============================================================

-- ----------------------------------------
-- 1. 系统用户 (sys_user)
-- 密码: 123456 (BCrypt加密, cost=10)
-- ----------------------------------------
INSERT IGNORE INTO `sys_user` (`id`, `username`, `password`, `nickname`, `real_name`, `phone`, `email`, `status`, `need_change_password`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
    (1, 'admin', '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', '管理员', '系统管理员', '13800000000', 'admin@company.com', 1, 0, NOW(), NOW(), 0, 0);

-- ----------------------------------------
-- 2. 角色 (sys_role)
-- ----------------------------------------
INSERT IGNORE INTO `sys_role` (`id`, `role_name`, `role_code`, `data_scope`, `status`, `sort_no`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
                                                                                                                                                             (1, '超级管理员', 'ADMIN', 4, 1, 1, NOW(), NOW(), 0, 0),
                                                                                                                                                             (2, '人力资源', 'HR', 3, 1, 2, NOW(), NOW(), 0, 0),
                                                                                                                                                             (3, '部门经理', 'MANAGER', 2, 1, 3, NOW(), NOW(), 0, 0),
                                                                                                                                                             (4, '财务人员', 'FINANCE', 3, 1, 4, NOW(), NOW(), 0, 0),
                                                                                                                                                             (5, '普通员工', 'EMPLOYEE', 1, 1, 5, NOW(), NOW(), 0, 0);

-- ----------------------------------------
-- 3. 用户角色关联 (sys_user_role)
-- ----------------------------------------
INSERT IGNORE INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
    (1, 1, 1, NOW(), NOW(), 0, 0);

-- ----------------------------------------
-- 4. 菜单 (sys_menu) - 精简版三级菜单
-- ----------------------------------------
INSERT IGNORE INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_no`, `visible`, `status`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
-- 一级目录
(1, 0, '系统管理', 1, '/system', 'Layout', NULL, 'system', 1, 1, 1, NOW(), NOW(), 0, 0),
(2, 0, '个人中心', 1, '/profile', 'Layout', NULL, 'profile', 2, 1, 1, NOW(), NOW(), 0, 0),
-- 二级菜单
(101, 1, '用户管理', 2, 'user', 'system/user/index', 'system:user:list', 'user', 1, 1, 1, NOW(), NOW(), 0, 0),
(102, 1, '角色管理', 2, 'role', 'system/role/index', 'system:role:list', 'role', 2, 1, 1, NOW(), NOW(), 0, 0),
(201, 2, '个人信息', 2, 'info', 'profile/info/index', 'profile:info:view', 'info', 1, 1, 1, NOW(), NOW(), 0, 0),
(202, 2, '修改密码', 2, 'password', 'profile/password/index', 'profile:password:update', 'password', 2, 1, 1, NOW(), NOW(), 0, 0),
-- 三级按钮 - 用户管理
(10101, 101, '用户查询', 3, NULL, NULL, 'system:user:query', NULL, 1, 1, 1, NOW(), NOW(), 0, 0),
(10102, 101, '用户新增', 3, NULL, NULL, 'system:user:add', NULL, 2, 1, 1, NOW(), NOW(), 0, 0),
(10103, 101, '用户修改', 3, NULL, NULL, 'system:user:edit', NULL, 3, 1, 1, NOW(), NOW(), 0, 0),
(10104, 101, '用户删除', 3, NULL, NULL, 'system:user:delete', NULL, 4, 1, 1, NOW(), NOW(), 0, 0),
-- 三级按钮 - 角色管理
(10201, 102, '角色查询', 3, NULL, NULL, 'system:role:query', NULL, 1, 1, 1, NOW(), NOW(), 0, 0),
(10202, 102, '角色新增', 3, NULL, NULL, 'system:role:add', NULL, 2, 1, 1, NOW(), NOW(), 0, 0),
(10203, 102, '角色修改', 3, NULL, NULL, 'system:role:edit', NULL, 3, 1, 1, NOW(), NOW(), 0, 0),
(10204, 102, '角色删除', 3, NULL, NULL, 'system:role:delete', NULL, 4, 1, 1, NOW(), NOW(), 0, 0);

-- ----------------------------------------
-- 5. 角色菜单关联 (sys_role_menu)
-- ----------------------------------------
INSERT IGNORE INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
-- ADMIN: 全部菜单
(1, 1, 1, NOW(), NOW(), 0, 0),
(2, 1, 2, NOW(), NOW(), 0, 0),
(3, 1, 101, NOW(), NOW(), 0, 0),
(4, 1, 102, NOW(), NOW(), 0, 0),
(5, 1, 201, NOW(), NOW(), 0, 0),
(6, 1, 202, NOW(), NOW(), 0, 0),
(7, 1, 10101, NOW(), NOW(), 0, 0),
(8, 1, 10102, NOW(), NOW(), 0, 0),
(9, 1, 10103, NOW(), NOW(), 0, 0),
(10, 1, 10104, NOW(), NOW(), 0, 0),
(11, 1, 10201, NOW(), NOW(), 0, 0),
(12, 1, 10202, NOW(), NOW(), 0, 0),
(13, 1, 10203, NOW(), NOW(), 0, 0),
(14, 1, 10204, NOW(), NOW(), 0, 0),
-- EMPLOYEE: 仅个人中心
(15, 5, 2, NOW(), NOW(), 0, 0),
(16, 5, 201, NOW(), NOW(), 0, 0),
(17, 5, 202, NOW(), NOW(), 0, 0);

-- ----------------------------------------
-- 6. 字典类型 (sys_dict_type)
-- ----------------------------------------
INSERT IGNORE INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
                                                                                                                              (1, '请假类型', 'leave_type', 1, NOW(), NOW(), 0),
                                                                                                                              (2, '证件类型', 'id_type', 1, NOW(), NOW(), 0),
                                                                                                                              (3, '学历', 'education', 1, NOW(), NOW(), 0);

-- ----------------------------------------
-- 7. 字典数据 (sys_dict_data)
-- ----------------------------------------
INSERT IGNORE INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `sort`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
-- 请假类型
(1, 'leave_type', '年假', 'annual', 1, 1, NOW(), NOW(), 0),
(2, 'leave_type', '事假', 'personal', 2, 1, NOW(), NOW(), 0),
(3, 'leave_type', '病假', 'sick', 3, 1, NOW(), NOW(), 0),
-- 证件类型
(4, 'id_type', '身份证', 'id_card', 1, 1, NOW(), NOW(), 0),
(5, 'id_type', '护照', 'passport', 2, 1, NOW(), NOW(), 0),
-- 学历
(6, 'education', '本科', 'bachelor', 1, 1, NOW(), NOW(), 0),
(7, 'education', '大专', 'college', 2, 1, NOW(), NOW(), 0),
(8, 'education', '硕士', 'master', 3, 1, NOW(), NOW(), 0);

-- ----------------------------------------
-- 8. 部门 (sys_dept)
-- ----------------------------------------
INSERT IGNORE INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `dept_code`, `dept_level`, `sort_no`, `status`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
    (1, 0, 'XX科技有限公司', 'XTGS', 1, 1, 1, NOW(), NOW(), 0, 0);

-- ----------------------------------------
-- 9. 职位 (sys_post)
-- ----------------------------------------
INSERT IGNORE INTO `sys_post` (`id`, `post_name`, `post_code`, `sequence_code`, `dept_id`, `default_probation_month`, `status`, `sort_no`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
                                                                                                                                                                                                      (1, '部门经理', 'DM', 'M', 1, 3, 1, 1, NOW(), NOW(), 0, 0),
                                                                                                                                                                                                      (2, '开发工程师', 'DEV', 'P', 1, 3, 1, 2, NOW(), NOW(), 0, 0),
                                                                                                                                                                                                      (3, '人力资源专员', 'HR', 'S', 1, 3, 1, 3, NOW(), NOW(), 0, 0);
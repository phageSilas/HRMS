-- ========================================
-- HRMS 初始化数据 - 角色与管理员用户
-- 创建时间: 2026-07-09
-- 注意: 密码为 AES-256 加密后的 'admin123'
-- ========================================

-- ========================================
-- 角色数据
-- data_scope: 1-本人 2-本部门 3-本部门及子部门 4-全部
-- ========================================
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `data_scope`, `status`, `sort_no`, `remark`) VALUES
(1, '系统管理员', 'ADMIN', 4, 1, 1, '系统最高权限管理员'),
(2, 'HR专员', 'HR', 4, 1, 2, '人力资源专员'),
(3, '部门主管', 'MANAGER', 3, 1, 3, '部门主管'),
(4, '普通员工', 'EMPLOYEE', 1, 1, 4, '普通员工'),
(5, '财务专员', 'FINANCE', 1, 1, 5, '财务专员');

-- ========================================
-- 管理员用户
-- 密码: admin123 (实际项目中应使用加密后的密码)
-- ========================================
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `real_name`, `phone`, `email`, `status`, `need_change_password`, `remark`) VALUES
(1, 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '超级管理员', '系统管理员', '13800138000', 'admin@hrms.com', 1, 1, '系统默认管理员账号');

-- ========================================
-- 用户角色关联
-- ========================================
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
(1, 1, 1);

-- ========================================
-- 默认部门
-- ========================================
INSERT INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `dept_code`, `dept_level`, `sort_no`, `description`, `status`, `remark`) VALUES
(1, 0, '总公司', '00', 1, 1, '总公司', 1, '根节点部门');

-- ========================================
-- 默认职位
-- ========================================
INSERT INTO `sys_post` (`id`, `post_name`, `post_code`, `sequence_code`, `job_level_min`, `job_level_max`, `default_probation_month`, `description`, `status`, `sort_no`) VALUES
(1, '软件工程师', 'SE001', 'P', 'P1', 'P10', 3, '软件开发工程师', 1, 1),
(2, '项目经理', 'PM001', 'M', 'M1', 'M5', 3, '项目管理', 1, 2);

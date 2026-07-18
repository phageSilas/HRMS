-- ============================================================
-- LF 本地测试数据生成脚本
-- 说明：插入以 lf 为前缀的角色/用户用于本地测试
-- 角色菜单权限参考现有系统角色复制
-- 密码统一使用 $2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe
--   （对应明文密码：123456，BCrypt cost=12）
-- ============================================================

-- ==================== 1. 插入角色 ====================
INSERT INTO `sys_role` (`role_name`, `role_code`, `data_scope`, `status`, `sort_no`, `remark`)
VALUES
('lf系统管理员', 'LF_ADMIN',    4, 1, 1, '本地测试-系统管理员，拥有全部权限'),
('lfHR专员',     'LF_HR',       3, 1, 2, '本地测试-HR专员，负责人力资源管理'),
('lf部门主管',   'LF_MANAGER',  3, 1, 3, '本地测试-部门主管，负责部门管理和审批'),
('lf财务专员',   'LF_FINANCE',  2, 1, 4, '本地测试-财务专员，负责薪资财务相关'),
('lf普通员工',   'LF_EMPLOYEE', 1, 1, 5, '本地测试-普通员工，仅查看本人数据');

-- 记录新角色ID（假设自增起始，后续使用变量）
SET @lf_admin_role_id    = LAST_INSERT_ID();
SET @lf_hr_role_id       = @lf_admin_role_id + 1;
SET @lf_manager_role_id  = @lf_admin_role_id + 2;
SET @lf_finance_role_id  = @lf_admin_role_id + 3;
SET @lf_employee_role_id = @lf_admin_role_id + 4;

-- ==================== 2. 插入员工（hr_employee） ====================
-- admin、HR 各1条，其他角色因有多用户需要多条员工记录
INSERT INTO `hr_employee` (`employee_no`, `user_id`, `dept_id`, `post_id`, `leader_id`, `employee_name`, `gender`, `phone`, `email`, `employment_status`, `hire_date`, `probation_month`, `probation_salary_ratio`)
VALUES
('LF2026001', NULL, 1, 1, NULL, 'lf管理员', 1, '13800001001', 'lfadmin@company.com',  1, '2024-01-01', 3, '100.00'),
('LF2026002', NULL, 3, 5, NULL, 'lfHR专员',  1, '13800001002', 'lfhr@company.com',    1, '2024-02-01', 3, '100.00'),
('LF2026003', NULL, 2, 4, NULL, 'lf张经理',  1, '13800001003', 'lfmgr1@company.com',  1, '2024-03-01', 3, '100.00'),
('LF2026004', NULL, 5, 9, NULL, 'lf王经理',  0, '13800001004', 'lfmgr2@company.com',  1, '2024-03-15', 3, '100.00'),
('LF2026005', NULL, 4, 7, NULL, 'lf赵财务',  0, '13800001005', 'lffin1@company.com',  1, '2024-04-01', 3, '100.00'),
('LF2026006', NULL, 4, 7, NULL, 'lf钱财务',  0, '13800001006', 'lffin2@company.com',  1, '2024-04-15', 3, '100.00'),
('LF2026007', NULL, 2, 1, NULL, 'lf孙开发',  1, '13800001007', 'lfemp1@company.com',  1, '2024-05-01', 3, '100.00'),
('LF2026008', NULL, 3, 5, NULL, 'lf周助理',  0, '13800001008', 'lfemp2@company.com',  1, '2024-05-15', 3, '100.00'),
('LF2026009', NULL, 5, 9, NULL, 'lf吴行政',  0, '13800001009', 'lfemp3@company.com',  1, '2024-06-01', 3, '100.00');

-- ==================== 3. 插入系统用户（sys_user） ====================
-- admin（lfadmin）和 HR（lfhr）各1条，唯一
-- 其他角色各有2-3条数据
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `real_name`, `phone`, `email`, `dept_id`, `employee_id`, `status`, `need_change_password`)
VALUES
-- admin 角色（唯一）
('lfadmin', '$2b$12$T5qSEbFF5OFSEj1GEH5UDedLtO0ljBiIDrRtJmLcpIuzL0/.RfCWC', 'lf管理员', 'lf管理员',   '13800001001', 'lfadmin@company.com',  1, (SELECT id FROM hr_employee WHERE employee_no = 'LF2026001'), 1, 0),
-- HR 角色（唯一）
('lfhr',    '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', 'lfHR专员', 'lfHR专员',   '13800001002', 'lfhr@company.com',     3, (SELECT id FROM hr_employee WHERE employee_no = 'LF2026002'), 1, 0),
-- 部门主管（2条）
('lfmgr1',  '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', 'lf张经理', 'lf张经理',   '13800001003', 'lfmgr1@company.com',   2, (SELECT id FROM hr_employee WHERE employee_no = 'LF2026003'), 1, 0),
('lfmgr2',  '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', 'lf王经理', 'lf王经理',   '13800001004', 'lfmgr2@company.com',   5, (SELECT id FROM hr_employee WHERE employee_no = 'LF2026004'), 1, 0),
-- 财务专员（2条）
('lffin1',  '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', 'lf赵财务', 'lf赵财务',   '13800001005', 'lffin1@company.com',   4, (SELECT id FROM hr_employee WHERE employee_no = 'LF2026005'), 1, 0),
('lffin2',  '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', 'lf钱财务', 'lf钱财务',   '13800001006', 'lffin2@company.com',   4, (SELECT id FROM hr_employee WHERE employee_no = 'LF2026006'), 1, 0),
-- 普通员工（3条）
('lfemp1',  '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', 'lf孙开发', 'lf孙开发',   '13800001007', 'lfemp1@company.com',   2, (SELECT id FROM hr_employee WHERE employee_no = 'LF2026007'), 1, 0),
('lfemp2',  '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', 'lf周助理', 'lf周助理',   '13800001008', 'lfemp2@company.com',   3, (SELECT id FROM hr_employee WHERE employee_no = 'LF2026008'), 1, 0),
('lfemp3',  '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', 'lf吴行政', 'lf吴行政',   '13800001009', 'lfemp3@company.com',   5, (SELECT id FROM hr_employee WHERE employee_no = 'LF2026009'), 1, 0);

-- 回补员工表的 user_id
UPDATE hr_employee e
JOIN sys_user u ON u.employee_id = e.id
SET e.user_id = u.id
WHERE e.employee_no LIKE 'LF2026%';

-- ==================== 4. 插入角色-菜单权限（sys_role_menu） ====================
-- 复制现有角色的菜单权限到对应的 lf 角色

-- 4.1 lf系统管理员 ← 复制自 ADMIN（role_id=1）：拥有全部菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT @lf_admin_role_id, `menu_id`
FROM `sys_role_menu`
WHERE `role_id` = 1;

-- 4.2 lfHR专员 ← 复制自 HR（role_id=2）：组织架构/员工/入转调离/考勤/薪资/审批/个人
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT @lf_hr_role_id, `menu_id`
FROM `sys_role_menu`
WHERE `role_id` = 2;

-- 4.3 lf部门主管 ← 复制自 MANAGER（role_id=3）：员工/入转调离/考勤/审批/个人
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT @lf_manager_role_id, `menu_id`
FROM `sys_role_menu`
WHERE `role_id` = 3;

-- 4.4 lf财务专员 ← 复制自 FINANCE（role_id=4）：薪资/审批/个人
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT @lf_finance_role_id, `menu_id`
FROM `sys_role_menu`
WHERE `role_id` = 4;

-- 4.5 lf普通员工 ← 复制自 EMPLOYEE（role_id=5）：考勤/个人中心/AI
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT @lf_employee_role_id, `menu_id`
FROM `sys_role_menu`
WHERE `role_id` = 5;

-- ==================== 5. 插入用户-角色关联（sys_user_role） ====================
-- admin → LF_ADMIN
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, @lf_admin_role_id
FROM `sys_user` u
WHERE u.username = 'lfadmin';

-- HR → LF_HR
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, @lf_hr_role_id
FROM `sys_user` u
WHERE u.username = 'lfhr';

-- 部门主管 → LF_MANAGER
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, @lf_manager_role_id
FROM `sys_user` u
WHERE u.username IN ('lfmgr1', 'lfmgr2');

-- 财务专员 → LF_FINANCE
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, @lf_finance_role_id
FROM `sys_user` u
WHERE u.username IN ('lffin1', 'lffin2');

-- 普通员工 → LF_EMPLOYEE
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, @lf_employee_role_id
FROM `sys_user` u
WHERE u.username IN ('lfemp1', 'lfemp2', 'lfemp3');

-- ==================== 6. 验证数据 ====================

-- 查看所有 lf 角色
SELECT '=== LF 角色 ===' AS '';
SELECT `id`, `role_name`, `role_code`, `data_scope`, `status`
FROM `sys_role`
WHERE `role_code` LIKE 'LF_%'
ORDER BY `id`;

-- 查看所有 lf 用户
SELECT '=== LF 用户 ===' AS '';
SELECT u.`id`, u.`username`, u.`nickname`, u.`phone`, u.`dept_id`, u.`employee_id`, u.`status`
FROM `sys_user` u
WHERE u.`username` LIKE 'lf%'
ORDER BY u.`id`;

-- 查看用户-角色关联
SELECT '=== LF 用户-角色关联 ===' AS '';
SELECT u.`username`, r.`role_name`, r.`role_code`
FROM `sys_user_role` ur
JOIN `sys_user` u ON u.`id` = ur.`user_id`
JOIN `sys_role` r ON r.`id` = ur.`role_id`
WHERE u.`username` LIKE 'lf%'
ORDER BY u.`username`;

-- 查看角色-菜单数量
SELECT '=== LF 角色-菜单数量 ===' AS '';
SELECT r.`role_name`, r.`role_code`, COUNT(rm.`menu_id`) AS `menu_count`
FROM `sys_role` r
LEFT JOIN `sys_role_menu` rm ON rm.`role_id` = r.`id`
WHERE r.`role_code` LIKE 'LF_%'
GROUP BY r.`id`, r.`role_name`, r.`role_code`
ORDER BY r.`role_code`;

-- ==================== 7. 清理脚本（如需回滚） ====================
-- 删除用户-角色关联
-- DELETE FROM `sys_user_role` WHERE `user_id` IN (SELECT `id` FROM `sys_user` WHERE `username` LIKE 'lf%');
-- 删除用户
-- DELETE FROM `sys_user` WHERE `username` LIKE 'lf%';
-- 删除角色-菜单关联
-- DELETE FROM `sys_role_menu` WHERE `role_id` IN (SELECT `id` FROM `sys_role` WHERE `role_code` LIKE 'LF_%');
-- 删除员工
-- DELETE FROM `hr_employee` WHERE `employee_no` LIKE 'LF2026%';
-- 删除角色
-- DELETE FROM `sys_role` WHERE `role_code` LIKE 'LF_%';
-- 重置 AUTO_INCREMENT（根据实际情况调整）
-- ALTER TABLE `sys_role` AUTO_INCREMENT = 13;
-- ALTER TABLE `sys_user` AUTO_INCREMENT = 7;
-- ALTER TABLE `hr_employee` AUTO_INCREMENT = 7;

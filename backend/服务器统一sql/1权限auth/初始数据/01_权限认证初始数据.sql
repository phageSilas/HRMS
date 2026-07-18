-- 自动生成的初始化数据，请勿手工零散修改
USE `hrms`;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `sys_role_menu`;
DELETE FROM `sys_user_role`;
DELETE FROM `sys_field_permission`;
DELETE FROM `sys_menu`;
DELETE FROM `sys_role`;
DELETE FROM `sys_user`;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `data_scope`, `status`, `sort_no`, `create_by`, `update_by`, `is_deleted`, `version`, `remark`) VALUES
(1, '系统管理员', 'ADMIN', 4, 1, 1, 1, 1, 0, 0, '拥有系统全部权限，可管理所有数据'),
(2, 'HR专员', 'HR', 3, 1, 2, 1, 1, 0, 0, '负责人力资源管理，可查看本部门及下属部门数据'),
(3, '部门主管', 'MANAGER', 3, 1, 3, 1, 1, 0, 0, '负责部门管理和审批，可查看本部门及下属部门数据'),
(4, '财务专员', 'FINANCE', 2, 1, 4, 1, 1, 0, 0, '负责薪资核算和财务相关业务，可查看本部门数据'),
(5, '普通员工', 'EMPLOYEE', 1, 1, 5, 1, 1, 0, 0, '普通员工角色，仅可查看本人数据')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `data_scope` = VALUES(`data_scope`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `update_by` = VALUES(`update_by`), `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_no`, `visible`, `status`, `create_by`, `update_by`, `is_deleted`, `version`, `remark`) VALUES
(1, 0, '首页', 1, '/home', '@/pages/Home', 'home', 'home', 1, 1, 1, 1, 1, 0, 0, '首页工作台'),
(2, 0, '权限体系', 1, '/system', 'Layout', 'system', 'setting', 2, 1, 1, 1, 1, 0, 0, '系统管理目录'),
(3, 0, '组织架构', 1, '/organization', 'Layout', 'organization', 'apartment', 3, 1, 1, 1, 1, 0, 0, '组织架构目录'),
(4, 0, '员工档案', 1, '/employee', 'Layout', 'employee', 'team', 4, 1, 1, 1, 1, 0, 0, '员工档案目录'),
(5, 0, '入转调离', 1, '/process', 'Layout', 'process', 'swap', 5, 1, 1, 1, 1, 0, 0, '入转调离目录'),
(6, 0, '考勤管理', 1, '/attendance', 'Layout', 'attendance', 'clock-circle', 6, 1, 1, 1, 1, 0, 0, '考勤管理目录'),
(7, 0, '薪资管理', 1, '/salary', 'Layout', 'salary', 'pay-circle', 7, 1, 1, 1, 1, 0, 0, '薪资管理目录'),
(8, 0, '审批中心', 1, '/approval', 'Layout', 'approval', 'check-circle', 8, 1, 1, 1, 1, 0, 0, '审批中心目录'),
(9, 0, '个人中心', 1, '/profile', 'Layout', 'profile', 'user', 9, 1, 1, 1, 1, 0, 0, '个人中心目录'),
(10, 0, 'AI智能助手', 1, '/ai', '@/pages/ai', 'ai', 'robot', 10, 1, 1, 1, 1, 0, 0, 'AI智能助手'),
(101, 2, '用户管理', 2, '/system/user', '@/pages/system/user', 'system:user', 'user', 1, 1, 1, 1, 1, 0, 0, '用户管理页面'),
(102, 2, '角色管理', 2, '/system/role', '@/pages/system/role', 'system:role', 'team', 2, 1, 1, 1, 1, 0, 0, '角色管理页面'),
(103, 2, '菜单管理', 2, '/system/menu', '@/pages/system/menu', 'system:menu', 'menu', 3, 1, 1, 1, 1, 0, 0, '菜单管理页面'),
(301, 3, '部门管理', 2, '/organization/dept', '@/pages/organization/dept', 'organization:dept', 'apartment', 1, 1, 1, 1, 1, 0, 0, '部门管理页面'),
(302, 3, '职位管理', 2, '/organization/post', '@/pages/organization/post', 'organization:post', 'solution', 2, 1, 1, 1, 1, 0, 0, '职位管理页面'),
(303, 3, '字典管理', 2, '/organization/dict', '@/pages/organization/dict', 'organization:dict', 'book', 3, 1, 1, 1, 1, 0, 0, '字典管理页面'),
(401, 4, '员工列表', 2, '/employee/list', '@/pages/employee', 'employee:list', 'unordered-list', 1, 1, 1, 1, 1, 0, 0, '员工列表页面'),
(402, 4, '新增员工', 2, '/employee/create', '@/pages/employee/edit', 'employee:create', 'plus', 2, 0, 1, 1, 1, 0, 0, '新增员工页面（隐藏菜单）'),
(403, 4, '编辑员工', 2, '/employee/:id/edit', '@/pages/employee/edit', 'employee:update', 'edit', 3, 0, 1, 1, 1, 0, 0, '编辑员工页面（隐藏菜单）'),
(404, 4, '员工详情', 2, '/employee/detail/:id', '@/pages/employee/detail', 'employee:detail', 'file-text', 4, 0, 1, 1, 1, 0, 0, '员工详情页面（隐藏菜单）'),
(405, 4, '合同管理', 2, '/employee/contract', '@/pages/employee/contract', 'employee:contract', 'file', 5, 1, 1, 1, 1, 0, 0, '合同管理页面'),
(501, 5, '入职申请', 2, '/process/entry', '@/pages/process/entry', 'process:entry', 'user-add', 1, 1, 1, 1, 1, 0, 0, '入职申请页面'),
(502, 5, '转正申请', 2, '/process/regular', '@/pages/process/regular', 'process:regular', 'check-circle', 2, 1, 1, 1, 1, 0, 0, '转正申请页面'),
(503, 5, '调岗申请', 2, '/process/transfer', '@/pages/process/transfer', 'process:transfer', 'swap', 3, 1, 1, 1, 1, 0, 0, '调岗申请页面'),
(504, 5, '离职申请', 2, '/process/leave', '@/pages/process/leave', 'process:leave', 'user-delete', 4, 1, 1, 1, 1, 0, 0, '离职申请页面'),
(601, 6, '员工打卡', 2, '/attendance/punch', '@/pages/attendance/punch', 'attendance:punch', 'clock-circle', 1, 1, 1, 1, 1, 0, 0, '员工打卡页面'),
(602, 6, '考勤记录', 2, '/attendance/record', '@/pages/attendance/record', 'attendance:record', 'calendar', 2, 1, 1, 1, 1, 0, 0, '考勤记录页面'),
(603, 6, '考勤配置', 2, '/attendance/groups', '@/pages/attendance/groups', 'attendance:config', 'setting', 3, 1, 1, 1, 1, 0, 0, '考勤配置页面'),
(604, 6, '我的请假', 2, '/attendance/leave', '@/pages/attendance/leave', 'attendance:leave', 'form', 4, 0, 1, 1, 1, 0, 0, '我的请假页面（隐藏菜单）'),
(605, 6, '请假管理', 2, '/attendance/leaveManage', '@/pages/attendance/leaveManage', 'attendance:leave-manage', 'audit', 5, 1, 1, 1, 1, 0, 0, '请假管理页面'),
(606, 6, '考勤统计', 2, '/attendance/summary', '@/pages/attendance/summary', 'attendance:summary', 'bar-chart', 6, 1, 1, 1, 1, 0, 0, '考勤统计页面'),
(701, 7, '薪资账套', 2, '/salary/account', '@/pages/salary/account', 'salary:account', 'wallet', 1, 1, 1, 1, 1, 0, 0, '薪资账套页面'),
(702, 7, '薪资核算', 2, '/salary/batch', '@/pages/salary/batch', 'salary:batch', 'calculator', 2, 1, 1, 1, 1, 0, 0, '薪资核算页面'),
(703, 7, '工资条', 2, '/salary/payslip', '@/pages/salary/payslip', 'salary:payslip', 'pay-circle', 3, 1, 1, 1, 1, 0, 0, '工资条页面'),
(801, 8, '审批工作台', 2, '/approval/workspace', '@/pages/approval/workspace', 'approval:workspace', 'desktop', 1, 1, 1, 1, 1, 0, 0, '审批工作台页面'),
(802, 8, '委托审批', 2, '/approval/delegation', '@/pages/approval/delegation', 'approval:delegation', 'user-switch', 2, 1, 1, 1, 1, 0, 0, '委托审批页面'),
(803, 8, '审批详情', 2, '/approval/detail/:id', '@/pages/approval/detail', 'approval:detail', 'file-text', 3, 0, 1, 1, 1, 0, 0, '审批详情页面（隐藏菜单）'),
(901, 9, '我的首页', 2, '/profile/index', '@/pages/profile/index', 'profile:index', 'home', 1, 1, 1, 1, 1, 0, 0, '我的首页页面'),
(902, 9, '我的档案', 2, '/profile/archive', '@/pages/profile/archive', 'profile:archive', 'folder', 2, 1, 1, 1, 1, 0, 0, '我的档案页面'),
(903, 9, '我的考勤', 2, '/profile/attendance', '@/pages/profile/attendance', 'profile:attendance', 'calendar', 3, 1, 1, 1, 1, 0, 0, '我的考勤页面'),
(904, 9, '我的请假', 2, '/profile/leave', '@/pages/profile/leave', 'profile:leave', 'form', 4, 1, 1, 1, 1, 0, 0, '我的请假页面'),
(905, 9, '我的薪资', 2, '/profile/salary', '@/pages/profile/salary', 'profile:salary', 'pay-circle', 5, 1, 1, 1, 1, 0, 0, '我的薪资页面'),
(906, 9, '账号安全', 2, '/profile/security', '@/pages/profile/security', 'profile:security', 'lock', 6, 1, 1, 1, 1, 0, 0, '账号安全页面')
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`), `parent_id` = VALUES(`parent_id`), `path` = VALUES(`path`), `component` = VALUES(`component`), `permission` = VALUES(`permission`), `sort_no` = VALUES(`sort_no`), `status` = VALUES(`status`), `remark` = VALUES(`remark`), `update_by` = VALUES(`update_by`), `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `real_name`, `phone`, `email`, `avatar_url`, `dept_id`, `employee_id`, `status`, `last_login_time`, `last_login_ip`, `need_change_password`, `password_update_time`, `login_fail_count`, `lock_time`, `create_by`, `update_by`, `is_deleted`, `version`, `remark`) VALUES
(1, 'admin', '$2b$12$T5qSEbFF5OFSEj1GEH5UDedLtO0ljBiIDrRtJmLcpIuzL0/.RfCWC', '管理员', '系统管理员', '13800000000', 'admin@company.com', NULL, NULL, NULL, 1, NULL, NULL, 0, NULL, 0, NULL, NULL, 1, 0, 0, NULL),
(11001, 'hr_user01', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '林晓彤', '林晓彤', '13900011001', 'hr_user01@hrms.local', NULL, 2301, 21001, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'hr用户'),
(11002, 'hr_user02', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '周雅宁', '周雅宁', '13900011002', 'hr_user02@hrms.local', NULL, 2302, 21002, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'hr用户'),
(11003, 'hr_user03', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '何思远', '何思远', '13900011003', 'hr_user03@hrms.local', NULL, 2303, 21003, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'hr用户'),
(11004, 'hr_user04', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '许清妍', '许清妍', '13900011004', 'hr_user04@hrms.local', NULL, 2304, 21004, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'hr用户'),
(11005, 'hr_user05', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '苏瑾瑜', '苏瑾瑜', '13900011005', 'hr_user05@hrms.local', NULL, 2301, 21005, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'hr用户'),
(12001, 'mgr_user01', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '陈景川', '陈景川', '13900012001', 'mgr_user01@hrms.local', NULL, 2309, 22001, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'manager用户'),
(12002, 'mgr_user02', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '季若凡', '季若凡', '13900012002', 'mgr_user02@hrms.local', NULL, 2313, 22002, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'manager用户'),
(12003, 'mgr_user03', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '程沐阳', '程沐阳', '13900012003', 'mgr_user03@hrms.local', NULL, 2311, 22003, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'manager用户'),
(12004, 'mgr_user04', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '高承泽', '高承泽', '13900012004', 'mgr_user04@hrms.local', NULL, 2317, 22004, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'manager用户'),
(12005, 'mgr_user05', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '孟知远', '孟知远', '13900012005', 'mgr_user05@hrms.local', NULL, 2321, 22005, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'manager用户'),
(13001, 'fin_user01', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '沈嘉禾', '沈嘉禾', '13900013001', 'fin_user01@hrms.local', NULL, 2305, 23001, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'finance用户'),
(13002, 'fin_user02', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '顾明哲', '顾明哲', '13900013002', 'fin_user02@hrms.local', NULL, 2306, 23002, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'finance用户'),
(13003, 'fin_user03', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '唐诗雨', '唐诗雨', '13900013003', 'fin_user03@hrms.local', NULL, 2307, 23003, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'finance用户'),
(13004, 'fin_user04', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '韩亦辰', '韩亦辰', '13900013004', 'fin_user04@hrms.local', NULL, 2308, 23004, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'finance用户'),
(13005, 'fin_user05', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '陆星河', '陆星河', '13900013005', 'fin_user05@hrms.local', NULL, 2305, 23005, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'finance用户'),
(14001, 'emp_user01', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '赵一诺', '赵一诺', '13900014001', 'emp_user01@hrms.local', NULL, 2310, 24001, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'employee用户'),
(14002, 'emp_user02', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '钱子睿', '钱子睿', '13900014002', 'emp_user02@hrms.local', NULL, 2312, 24002, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'employee用户'),
(14003, 'emp_user03', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '孙可心', '孙可心', '13900014003', 'emp_user03@hrms.local', NULL, 2314, 24003, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'employee用户'),
(14004, 'emp_user04', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '李辰皓', '李辰皓', '13900014004', 'emp_user04@hrms.local', NULL, 2315, 24004, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'employee用户'),
(14005, 'emp_user05', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '吴沐宸', '吴沐宸', '13900014005', 'emp_user05@hrms.local', NULL, 2318, 24005, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'employee用户'),
(14006, 'emp_user06', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '郑雨桐', '郑雨桐', '13900014006', 'emp_user06@hrms.local', NULL, 2320, 24006, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'employee用户'),
(14007, 'emp_user07', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '王书言', '王书言', '13900014007', 'emp_user07@hrms.local', NULL, 2322, 24007, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'employee用户'),
(14008, 'emp_user08', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '冯嘉琪', '冯嘉琪', '13900014008', 'emp_user08@hrms.local', NULL, 2323, 24008, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'employee用户'),
(14009, 'emp_user09', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '褚逸凡', '褚逸凡', '13900014009', 'emp_user09@hrms.local', NULL, 2324, 24009, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'employee用户'),
(14010, 'emp_user10', '$2a$10$ndE.7MaUNuBfzgZOeoEa7eEQS1xmhunKtn8sHqSZSIvyrrCL9Raoq', '卫清荷', '卫清荷', '13900014010', 'emp_user10@hrms.local', NULL, 2316, 24010, 1, NULL, NULL, 0, NULL, 0, NULL, 1, 1, 0, 0, 'employee用户')
ON DUPLICATE KEY UPDATE `nickname` = VALUES(`nickname`), `real_name` = VALUES(`real_name`), `dept_id` = VALUES(`dept_id`), `employee_id` = VALUES(`employee_id`), `status` = VALUES(`status`), `need_change_password` = VALUES(`need_change_password`), `remark` = VALUES(`remark`), `update_by` = VALUES(`update_by`), `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_by`, `update_by`, `is_deleted`, `version`) VALUES
(50001, 1, 1, 1, 1, 0, 0),
(50007, 11001, 2, 1, 1, 0, 0),
(50010, 11002, 2, 1, 1, 0, 0),
(50012, 11003, 2, 1, 1, 0, 0),
(50014, 11004, 2, 1, 1, 0, 0),
(50016, 11005, 2, 1, 1, 0, 0),
(50018, 12001, 3, 1, 1, 0, 0),
(50020, 12002, 3, 1, 1, 0, 0),
(50022, 12003, 3, 1, 1, 0, 0),
(50024, 12004, 3, 1, 1, 0, 0),
(50026, 12005, 3, 1, 1, 0, 0),
(50028, 13001, 4, 1, 1, 0, 0),
(50031, 13002, 4, 1, 1, 0, 0),
(50033, 13003, 4, 1, 1, 0, 0),
(50035, 13004, 4, 1, 1, 0, 0),
(50037, 13005, 4, 1, 1, 0, 0),
(50039, 14001, 5, 1, 1, 0, 0),
(50040, 14002, 5, 1, 1, 0, 0),
(50041, 14003, 5, 1, 1, 0, 0),
(50042, 14004, 5, 1, 1, 0, 0),
(50043, 14005, 5, 1, 1, 0, 0),
(50044, 14006, 5, 1, 1, 0, 0),
(50045, 14007, 5, 1, 1, 0, 0),
(50046, 14008, 5, 1, 1, 0, 0),
(50047, 14009, 5, 1, 1, 0, 0),
(50048, 14010, 5, 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`), `update_by` = VALUES(`update_by`), `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `create_by`, `update_by`, `is_deleted`, `version`)
SELECT 1, id, 1, 1, 0, 0 FROM `sys_menu`
ON DUPLICATE KEY UPDATE `update_by` = VALUES(`update_by`), `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `create_by`, `update_by`, `is_deleted`, `version`)
SELECT 2, id, 1, 1, 0, 0 FROM `sys_menu` WHERE id IN (
    1,
    3, 301, 302, 303,
    4, 401, 402, 403, 404, 405,
    5, 501, 502, 503, 504,
    6, 601, 602, 603, 604, 605, 606,
    8, 801, 802, 803,
    9, 901, 902, 903, 904, 905, 906,
    10
)
ON DUPLICATE KEY UPDATE `update_by` = VALUES(`update_by`), `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `create_by`, `update_by`, `is_deleted`, `version`)
SELECT 3, id, 1, 1, 0, 0 FROM `sys_menu` WHERE id IN (
    1,
    4, 401, 402, 403, 404, 405,
    5, 501, 502, 503, 504,
    6, 601, 602, 603, 604, 605, 606,
    8, 801, 802, 803,
    9, 901, 902, 903, 904, 905, 906,
    10
)
ON DUPLICATE KEY UPDATE `update_by` = VALUES(`update_by`), `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `create_by`, `update_by`, `is_deleted`, `version`)
SELECT 4, id, 1, 1, 0, 0 FROM `sys_menu` WHERE id IN (
    1,
    7, 701, 702, 703,
    8, 801, 802, 803,
    9, 901, 902, 903, 904, 905, 906,
    10
)
ON DUPLICATE KEY UPDATE `update_by` = VALUES(`update_by`), `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `create_by`, `update_by`, `is_deleted`, `version`)
SELECT 5, id, 1, 1, 0, 0 FROM `sys_menu` WHERE id IN (
    1,
    6, 601, 604,
    9, 901, 902, 903, 904, 905, 906,
    10
)
ON DUPLICATE KEY UPDATE `update_by` = VALUES(`update_by`), `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `sys_field_permission` (`id`, `biz_type`, `field_name`, `field_desc`, `role_id`, `viewable`, `editable`, `flow_required`, `create_time`, `update_time`) VALUES
(1, 'employee', 'employee_name', '员工姓名', 1, 1, 1, 0, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
(2, 'employee', 'dept_id', '所属部门', 1, 1, 1, 1, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
(3, 'employee', 'base_salary', '基础工资', 1, 1, 1, 1, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
(4, 'employee', 'employee_name', '员工姓名', 2, 1, 1, 0, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
(5, 'employee', 'dept_id', '所属部门', 2, 1, 1, 1, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
(6, 'salary', 'base_salary', '基础工资', 4, 1, 0, 1, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
(7, 'attendance', 'clock_in_time', '上班打卡时间', 3, 1, 0, 0, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
(8, 'employee', 'bank_account', '银行卡号', 2, 0, 0, 0, '2026-01-01 09:00:00', '2026-01-01 09:00:00')
ON DUPLICATE KEY UPDATE `field_desc` = VALUES(`field_desc`), `viewable` = VALUES(`viewable`), `editable` = VALUES(`editable`), `flow_required` = VALUES(`flow_required`), `update_time` = VALUES(`update_time`);


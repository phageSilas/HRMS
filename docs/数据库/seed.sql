-- ============================================================
-- 初始化数据脚本（精简版 - 修复主键冲突）
-- ============================================================

-- ----------------------------------------
-- 1. 系统用户 (sys_user)
-- 密码: 123456 (BCrypt加密, cost=10)
-- ----------------------------------------
INSERT IGNORE INTO `sys_user` (
    `id`,
    `username`,
    `password`,
    `nickname`,
    `real_name`,
    `phone`,
    `email`,
    `avatar_url`,
    `dept_id`,
    `employee_id`,
    `status`,
    `last_login_time`,
    `last_login_ip`,
    `need_change_password`,
    `password_update_time`,
    `login_fail_count`,
    `lock_time`,
    `create_by`,
    `create_time`,
    `update_by`,
    `update_time`,
    `is_deleted`,
    `version`,
    `remark`
) VALUES (
    1,
    'admin',
    '$2b$12$T5qSEbFF5OFSEj1GEH5UDedLtO0ljBiIDrRtJmLcpIuzL0/.RfCWC',
    '管理员',
    '系统管理员',
    '13800000000',
    'admin@company.com',
    NULL,                                 -- avatar_url
    NULL,                                 -- dept_id（管理员不属任何部门）
    NULL,                                 -- employee_id（未关联员工）
    1,                                    -- status
    NULL,                                 -- last_login_time
    NULL,                                 -- last_login_ip
    0,                                    -- need_change_password（初始不需要强制修改）
    NULL,                                 -- password_update_time
    0,                                    -- login_fail_count
    NULL,                                 -- lock_time
    NULL,                                 -- create_by
    NOW(),                                -- create_time
    NULL,                                 -- update_by
    NOW(),                                -- update_time
    0,                                    -- is_deleted
    0,                                    -- version
    NULL                                  -- remark
);
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `data_scope`, `status`, `sort_no`, `remark`) VALUES
                                                                                                         (1, '系统管理员', 'ADMIN', 4, 1, 1, '拥有系统全部权限，可管理所有数据'),
                                                                                                         (2, 'HR专员', 'HR', 3, 1, 2, '负责人力资源管理，可查看本部门及下属部门数据'),
                                                                                                         (3, '部门主管', 'MANAGER', 3, 1, 3, '负责部门管理和审批，可查看本部门及下属部门数据'),
                                                                                                         (4, '财务专员', 'FINANCE', 2, 1, 4, '负责薪资核算和财务相关业务，可查看本部门数据'),
                                                                                                         (5, '普通员工', 'EMPLOYEE', 1, 1, 5, '普通员工角色，仅可查看本人数据');

-- ============================================================
-- 2. 初始菜单数据
-- ============================================================
-- 菜单类型说明：
--   1 = 目录（一级菜单，可包含子菜单）
--   2 = 菜单（二级菜单，具体页面）
-- ============================================================

-- 一级菜单（目录）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_no`, `visible`, `status`, `remark`) VALUES
                                                                                                                                                              (1, 0, '首页', 1, '/home', '@/pages/Home', 'home', 'home', 1, 1, 1, '首页工作台'),
                                                                                                                                                              (2, 0, '权限体系', 1, '/system', 'Layout', 'system', 'setting', 2, 1, 1, '系统管理目录'),
                                                                                                                                                              (3, 0, '组织架构', 1, '/organization', 'Layout', 'organization', 'apartment', 3, 1, 1, '组织架构目录'),
                                                                                                                                                              (4, 0, '员工档案', 1, '/employee', 'Layout', 'employee', 'team', 4, 1, 1, '员工档案目录'),
                                                                                                                                                              (5, 0, '入转调离', 1, '/process', 'Layout', 'process', 'swap', 5, 1, 1, '入转调离目录'),
                                                                                                                                                              (6, 0, '考勤管理', 1, '/attendance', 'Layout', 'attendance', 'clock-circle', 6, 1, 1, '考勤管理目录'),
                                                                                                                                                              (7, 0, '薪资管理', 1, '/salary', 'Layout', 'salary', 'pay-circle', 7, 1, 1, '薪资管理目录'),
                                                                                                                                                              (8, 0, '审批中心', 1, '/approval', 'Layout', 'approval', 'check-circle', 8, 1, 1, '审批中心目录'),
                                                                                                                                                              (9, 0, '个人中心', 1, '/profile', 'Layout', 'profile', 'user', 9, 1, 1, '个人中心目录'),
                                                                                                                                                              (10, 0, 'AI智能助手', 1, '/ai', '@/pages/ai', 'ai', 'robot', 10, 1, 1, 'AI智能助手');

-- 权限体系子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_no`, `visible`, `status`, `remark`) VALUES
                                                                                                                                                              (101, 2,
                                                                                                                                                               '用户管理', 2, '/system/user', '@/pages/system/user', 'system:user', 'user', 1, 1, 1, '用户管理页面'),
                                                                                                                                                              (102, 2, '角色管理', 2, '/system/role', '@/pages/system/role', 'system:role', 'team', 2, 1, 1, '角色管理页面'),
                                                                                                                                                              (103, 2, '菜单管理', 2, '/system/menu', '@/pages/system/menu', 'system:menu', 'menu', 3, 1, 1, '菜单管理页面');

-- 组织架构子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_no`, `visible`, `status`, `remark`) VALUES
                                                                                                                                                              (301, 3, '部门管理', 2, '/organization/dept', '@/pages/organization/dept', 'organization:dept', 'apartment', 1, 1, 1, '部门管理页面'),
                                                                                                                                                              (302, 3, '职位管理', 2, '/organization/post', '@/pages/organization/post', 'organization:post', 'solution', 2, 1, 1, '职位管理页面'),
                                                                                                                                                              (303, 3, '字典管理', 2, '/organization/dict', '@/pages/organization/dict', 'organization:dict', 'book', 3, 1, 1, '字典管理页面');

-- 员工档案子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_no`, `visible`, `status`, `remark`) VALUES
                                                                                                                                                              (401, 4, '员工列表', 2, '/employee/list', '@/pages/employee', 'employee:list', 'unordered-list', 1, 1, 1, '员工列表页面'),
                                                                                                                                                              (402, 4, '新增员工', 2, '/employee/create', '@/pages/employee/edit', 'employee:create', 'plus', 2, 0, 1, '新增员工页面（隐藏菜单）'),
                                                                                                                                                              (403, 4, '编辑员工', 2, '/employee/:id/edit', '@/pages/employee/edit', 'employee:update', 'edit', 3, 0, 1, '编辑员工页面（隐藏菜单）'),
                                                                                                                                                              (404, 4, '员工详情', 2, '/employee/detail/:id', '@/pages/employee/detail', 'employee:detail', 'file-text', 4, 0, 1, '员工详情页面（隐藏菜单）'),
                                                                                                                                                              (405, 4, '合同管理', 2, '/employee/contract', '@/pages/employee/contract', 'employee:contract', 'file', 5, 1, 1, '合同管理页面');

-- 入转调离子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_no`, `visible`, `status`, `remark`) VALUES
                                                                                                                                                              (501, 5, '入职申请', 2, '/process/entry', '@/pages/process/entry', 'process:entry', 'user-add', 1, 1, 1, '入职申请页面'),
                                                                                                                                                              (502, 5, '转正申请', 2, '/process/regular', '@/pages/process/regular', 'process:regular', 'check-circle', 2, 1, 1, '转正申请页面'),
                                                                                                                                                              (503, 5, '调岗申请', 2, '/process/transfer', '@/pages/process/transfer', 'process:transfer', 'swap', 3, 1, 1, '调岗申请页面'),
                                                                                                                                                              (504, 5, '离职申请', 2, '/process/leave', '@/pages/process/leave', 'process:leave', 'user-delete', 4, 1, 1, '离职申请页面');

-- 考勤管理子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_no`, `visible`, `status`, `remark`) VALUES
                                                                                                                                                              (601, 6, '员工打卡', 2, '/attendance/punch', '@/pages/attendance/punch', 'attendance:punch', 'clock-circle', 1, 1, 1, '员工打卡页面'),
                                                                                                                                                              (602, 6, '考勤记录', 2, '/attendance/record', '@/pages/attendance/record', 'attendance:record', 'calendar', 2, 1, 1, '考勤记录页面'),
                                                                                                                                                              (603, 6, '考勤配置', 2, '/attendance/groups', '@/pages/attendance/groups', 'attendance:config', 'setting', 3, 1, 1, '考勤配置页面'),
                                                                                                                                                              (604, 6, '我的请假', 2, '/attendance/leave', '@/pages/attendance/leave', 'attendance:leave', 'form', 4, 0, 1, '我的请假页面（隐藏菜单）'),
                                                                                                                                                              (605, 6, '请假管理', 2, '/attendance/leaveManage', '@/pages/attendance/leaveManage', 'attendance:leave-manage', 'audit', 5, 1, 1, '请假管理页面'),
                                                                                                                                                              (606, 6, '考勤统计', 2, '/attendance/summary', '@/pages/attendance/summary', 'attendance:summary', 'bar-chart', 6, 1, 1, '考勤统计页面');

-- 薪资管理子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_no`, `visible`, `status`, `remark`) VALUES
                                                                                                                                                              (701, 7, '薪资账套', 2, '/salary/account', '@/pages/salary/account', 'salary:account', 'wallet', 1, 1, 1, '薪资账套页面'),
                                                                                                                                                              (702, 7, '薪资核算', 2, '/salary/batch', '@/pages/salary/batch', 'salary:batch', 'calculator', 2, 1, 1, '薪资核算页面'),
                                                                                                                                                              (703, 7, '工资条', 2, '/salary/payslip', '@/pages/salary/payslip', 'salary:payslip', 'pay-circle', 3, 1, 1, '工资条页面');

-- 审批中心子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_no`, `visible`, `status`, `remark`) VALUES
                                                                                                                                                              (801, 8, '审批工作台', 2, '/approval/workspace', '@/pages/approval/workspace', 'approval:workspace', 'desktop', 1, 1, 1, '审批工作台页面'),
                                                                                                                                                              (802, 8, '委托审批', 2, '/approval/delegation', '@/pages/approval/delegation', 'approval:delegation', 'user-switch', 2, 1, 1, '委托审批页面'),
                                                                                                                                                              (803, 8, '审批详情', 2, '/approval/detail/:id', '@/pages/approval/detail', 'approval:detail', 'file-text', 3, 0, 1, '审批详情页面（隐藏菜单）');

-- 个人中心子菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_no`, `visible`, `status`, `remark`) VALUES
                                                                                                                                                              (901, 9, '我的首页', 2, '/profile/index', '@/pages/profile/index', 'profile:index', 'home', 1, 1, 1, '我的首页页面'),
                                                                                                                                                              (902, 9, '我的档案', 2, '/profile/archive', '@/pages/profile/archive', 'profile:archive', 'folder', 2, 1, 1, '我的档案页面'),
                                                                                                                                                              (903, 9, '我的考勤', 2, '/profile/attendance', '@/pages/profile/attendance', 'profile:attendance', 'calendar', 3, 1, 1, '我的考勤页面'),
                                                                                                                                                              (904, 9, '我的请假', 2, '/profile/leave', '@/pages/profile/leave', 'profile:leave', 'form', 4, 1, 1, '我的请假页面（重定向）'),
                                                                                                                                                              (905, 9, '我的薪资', 2, '/profile/salary', '@/pages/profile/salary', 'profile:salary', 'pay-circle', 5, 1, 1, '我的薪资页面'),
                                                                                                                                                              (906, 9, '账号安全', 2, '/profile/security', '@/pages/profile/security', 'profile:security', 'lock', 6, 1, 1, '账号安全页面');

-- ============================================================
-- 3. 角色菜单关联数据
-- ============================================================

-- 系统管理员(ADMIN) - 拥有全部菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu`;

-- HR专员(HR) - 排除权限体系模块，拥有其他业务模块权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, id FROM `sys_menu` WHERE id NOT IN (
    -- 排除权限体系相关菜单
                                              2, 101, 102, 103
    );

-- 部门主管(MANAGER) - 员工档案、入转调离、考勤管理、审批中心、个人中心、AI助手
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 3, id FROM `sys_menu` WHERE id IN (
    -- 首页
                                          1,
    -- 员工档案
                                          4, 401, 402, 403, 404, 405,
    -- 入转调离
                                          5, 501, 502, 503, 504,
    -- 考勤管理
                                          6, 601, 602, 603, 604, 605, 606,
    -- 审批中心
                                          8, 801, 802, 803,
    -- 个人中心
                                          9, 901, 902, 903, 904, 905, 906,
    -- AI助手
                                          10
    );

-- 财务专员(FINANCE) - 薪资管理、审批中心、个人中心、AI助手
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 4, id FROM `sys_menu` WHERE id IN (
    -- 首页
                                          1,
    -- 薪资管理
                                          7, 701, 702, 703,
    -- 审批中心
                                          8, 801, 802, 803,
    -- 个人中心
                                          9, 901, 902, 903, 904, 905, 906,
    -- AI助手
                                          10
    );

-- 普通员工(EMPLOYEE) - 仅打卡、个人中心、AI助手
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 5, id FROM `sys_menu` WHERE id IN (
    -- 首页
                                          1,
    -- 考勤打卡
                                          6, 601, 604,
    -- 个人中心
                                          9, 901, 902, 903, 904, 905, 906,
    -- AI助手
                                          10
    );
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

-- ----------------------------------------
-- 10. 字段权限配置 (sys_field_permission)
-- ----------------------------------------
INSERT IGNORE INTO `sys_field_permission` (`biz_type`, `field_name`, `field_desc`, `role_id`, `viewable`, `editable`, `flow_required`) VALUES
-- ADMIN 角色：员工模块全部可见可编辑
('employee', 'name', '姓名', 1, 1, 1, 0),
('employee', 'gender', '性别', 1, 1, 1, 0),
('employee', 'phone', '手机号', 1, 1, 1, 0),
('employee', 'email', '邮箱', 1, 1, 1, 0),
('employee', 'idCardNo', '身份证号', 1, 1, 1, 0),
('employee', 'deptId', '部门ID', 1, 1, 1, 1),
('employee', 'postId', '职位ID', 1, 1, 1, 1),
('employee', 'entryDate', '入职日期', 1, 1, 1, 0),
('employee', 'bankAccount', '银行卡号', 1, 1, 1, 0),
-- HR 角色：员工模块部分可见可编辑
('employee', 'name', '姓名', 2, 1, 1, 0),
('employee', 'gender', '性别', 2, 1, 0, 0),
('employee', 'phone', '手机号', 2, 1, 0, 0),
('employee', 'email', '邮箱', 2, 1, 1, 0),
('employee', 'idCardNo', '身份证号', 2, 1, 0, 0),
('employee', 'deptId', '部门ID', 2, 1, 0, 1),
('employee', 'postId', '职位ID', 2, 1, 0, 1),
('employee', 'entryDate', '入职日期', 2, 1, 0, 0),
('employee', 'bankAccount', '银行卡号', 2, 0, 0, 0);

-- ----------------------------------------
-- 11. 考勤组成员 (hr_attendance_group_member)
-- ----------------------------------------
INSERT IGNORE INTO hr_attendance_group_member (
    group_id,
    employee_id,
    effective_start_date,
    effective_end_date,
    status,
    remark
) VALUES (
             1,
             1,
             CURDATE(),
             NULL,
             1,
             '张三加入默认考勤组'
         );

-- ============================================================
-- 12. 新增测试用户 (sys_user) — 审批流测试用
-- 密码统一: 123456 (BCrypt加密, cost=12)
-- employee_id 先填占位值，后续员工插入后回补
-- ============================================================
-- INSERT IGNORE INTO `sys_user` (`id`, `username`, `password`, `nickname`, `real_name`, `phone`, `email`, `employee_id`, `status`, `need_change_password`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
--     (2, 'zhangsan', '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', '张三', '张三', '13800000001', 'zhangsan@company.com', 6, 1, 0, NOW(), NOW(), 0, 0),
--     (3, 'lisi',    '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', '李四', '李四', '13800000002', 'lisi@company.com', 2, 1, 0, NOW(), NOW(), 0, 0),
--     (4, 'wangwu',  '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', '王五', '王五', '13800000003', 'wangwu@company.com', 3, 1, 0, NOW(), NOW(), 0, 0),
--     (5, 'zhaoliu', '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', '赵六', '赵六', '13800000004', 'zhaoliu@company.com', 4, 1, 0, NOW(), NOW(), 0, 0),
--     (6, 'sunqi',   '$2b$12$DM3WJuIpv4IN4aXoqp8EFOZKsfV1DXlCwbDsrYelpGlGINCLznpJe', '孙七', '孙七', '13800000005', 'sunqi@company.com', 5, 1, 0, NOW(), NOW(), 0, 0);

-- ============================================================
-- 13. 补充部门数据 (sys_dept) — 设置负责人
-- ============================================================
-- 更新公司总部，负责人=赵六(BOSS)
UPDATE sys_dept SET leader_user_id = 5, leader_employee_id = 4 WHERE id = 1;

-- 如果部门已存在（被业务代码初始化），则更新负责人；否则插入
INSERT IGNORE INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `dept_code`, `ancestors`, `dept_level`, `leader_user_id`, `leader_employee_id`, `sort_no`, `status`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
    (2, 1, '技术部', 'JSB', '1', 2, 2, 6, 2, 1, NOW(), NOW(), 0, 0),
    (3, 1, '人事部', 'RSB', '1', 2, 3, 2, 3, 1, NOW(), NOW(), 0, 0),
    (4, 1, '财务部', 'CWB', '1', 2, 4, 3, 4, 1, NOW(), NOW(), 0, 0);
-- 如果 INSERT IGNORE 因主键冲突被跳过（部门已存在），则用 UPDATE 回补 leader
UPDATE sys_dept SET leader_user_id = 2, leader_employee_id = 6 WHERE id = 2 AND leader_user_id IS NULL;
UPDATE sys_dept SET leader_user_id = 3, leader_employee_id = 2 WHERE id = 3 AND leader_user_id IS NULL;
UPDATE sys_dept SET leader_user_id = 4, leader_employee_id = 3 WHERE id = 4 AND leader_user_id IS NULL;

-- ============================================================
-- 14. 员工数据 (hr_employee)
-- 表结构: id, employee_no, user_id, dept_id, post_id, leader_id,
--          employee_name, gender, phone, email, employment_status,
--          hire_date, job_level, create_time, update_time, is_deleted, version
-- ============================================================
INSERT IGNORE INTO `hr_employee` (`id`, `employee_no`, `user_id`, `dept_id`, `post_id`, `leader_id`, `employee_name`, `gender`, `phone`, `email`, `employment_status`, `hire_date`, `job_level`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
    (6, 'EMP2026006', 2, 2, 1, NULL, '张三', 1, '13800000006', 'zhangsan@company.com', 2, '2024-01-01', 'M4', NOW(), NOW(), 0, 0),
    (2, 'EMP2026002', 3, 3, 3, NULL, '李四', 1, '13800000002', 'lisi@company.com', 2, '2024-01-01', 'M4', NOW(), NOW(), 0, 0),
    (3, 'EMP2026003', 4, 4, 1, NULL, '王五', 1, '13800000003', 'wangwu@company.com', 2, '2024-01-01', 'M4', NOW(), NOW(), 0, 0),
    (4, 'EMP2026004', 5, 1, 1, NULL, '赵六', 1, '13800000004', 'zhaoliu@company.com', 2, '2024-01-01', 'M5', NOW(), NOW(), 0, 0),
    (5, 'EMP2026005', 6, 2, 2, 6, '孙七', 1, '13800000005', 'sunqi@company.com', 1, '2026-07-01', 'P3', NOW(), NOW(), 0, 0);

-- 回补：如果张三的 employee_id 未正确关联（INSERT IGNORE 被跳过时），手动更新
UPDATE sys_user SET employee_id = 6 WHERE id = 2 AND employee_id = 1;
-- 回补：如果孙七的 leader 还是指向旧数据（admin），修正为张三
UPDATE hr_employee SET leader_id = 6 WHERE id = 5 AND leader_id != 6;

-- ============================================================
-- 15. 审批角色 (sys_role) — 用于 ApproverResolver 角色解析
--      role_code = HR_HEAD / FINANCE_HEAD / BOSS
-- ============================================================
INSERT IGNORE INTO `sys_role` (`id`, `role_name`, `role_code`, `data_scope`, `status`, `sort_no`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
    (10, 'HR负责人',     'HR_HEAD',       3, 1, 10, NOW(), NOW(), 0, 0),
    (11, '财务负责人',   'FINANCE_HEAD',   3, 1, 11, NOW(), NOW(), 0, 0),
    (12, '老板',         'BOSS',           4, 1, 12, NOW(), NOW(), 0, 0);

-- ============================================================
-- 16. 用户角色关联 (sys_user_role) — 分配审批角色
--      李四(id=3) → HR_HEAD
--      王五(id=4) → FINANCE_HEAD
--      赵六(id=5) → BOSS
-- ============================================================
INSERT IGNORE INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
    (100, 3, 10, NOW(), NOW(), 0, 0),
    (101, 4, 11, NOW(), NOW(), 0, 0),
    (102, 5, 12, NOW(), NOW(), 0, 0);

-- ============================================================
-- 17. 薪资批次数据 (hr_salary_batch) — 测试工资条用
--     状态为 RELEASED 才能在工资条列表中可见
-- ============================================================
INSERT IGNORE INTO `hr_salary_batch` (`id`, `batch_no`, `salary_month`, `scope_type`, `batch_status`, `total_count`, `total_gross_salary`, `total_net_salary`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
    (1, 'BATCH20260601', '2026-06', 'ALL', 'RELEASED', 5, 150000.00, 120000.00, NOW(), NOW(), 0, 0),
    (2, 'BATCH20260501', '2026-05', 'ALL', 'RELEASED', 5, 148000.00, 118000.00, NOW(), NOW(), 0, 0);

-- ============================================================
-- 18. 薪资明细数据 (hr_salary_batch_item) — 员工工资条
--       张三 employee_id=6, 孙七 employee_id=5
-- ============================================================
INSERT IGNORE INTO `hr_salary_batch_item` (`id`, `batch_id`, `employee_id`, `base_salary`, `allowance`, `performance_bonus`, `overtime_pay`, `late_deduction`, `leave_deduction`, `social_insurance`, `housing_fund`, `income_tax`, `gross_salary`, `deduction_total`, `net_salary`, `create_time`, `update_time`) VALUES
    (1, 1, 6, 25000.00, 2000.00, 5000.00, 0.00, 0.00, 0.00, 2250.00, 1500.00, 2150.00, 32000.00, 5900.00, 26100.00, NOW(), NOW()),
    (2, 1, 5, 8000.00, 500.00, 2000.00, 300.00, 50.00, 100.00, 720.00, 480.00, 345.00, 10800.00, 1695.00, 9105.00, NOW(), NOW()),
    (3, 2, 6, 25000.00, 2000.00, 4500.00, 0.00, 0.00, 0.00, 2250.00, 1500.00, 2075.00, 31500.00, 5825.00, 25675.00, NOW(), NOW()),
    (4, 2, 5, 8000.00, 500.00, 1500.00, 200.00, 0.00, 0.00, 720.00, 480.00, 300.00, 10200.00, 1500.00, 8700.00, NOW(), NOW());

-- ============================================================
-- 19. 回补薪资明细：如果 INSERT IGNORE 因主键冲突未生效，用 UPDATE 修正 employee_id
-- ============================================================
UPDATE hr_salary_batch_item SET employee_id = 6 WHERE id IN (1,3) AND employee_id = 1;

-- ============================================================
-- 21. 加班申请数据 (hr_attendance_overtime)
--       孙七 employee_id=5，已通过的加班记录
-- ============================================================
INSERT IGNORE INTO `hr_attendance_overtime` (`id`, `employee_id`, `overtime_date`, `duration`, `reason`, `approval_status`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
    (1, 5, '2026-07-10 18:00:00', 3.0, '项目上线紧急支持', 2, NOW(), NOW(), 0, 0);

-- # 新增用户职级字段
START TRANSACTION;

INSERT INTO sys_dict_type (
    dict_name,
    dict_type,
    status,
    remark,
    create_by,
    update_by,
    is_deleted
) VALUES (
             '职级',
             'job_level',
             1,
             '薪资账套适用范围-职级',
             1,
             1,
             0
         );

INSERT INTO sys_dict_data (
    dict_type,
    dict_label,
    dict_value,
    css_class,
    sort,
    status,
    remark,
    create_by,
    update_by,
    is_deleted
) VALUES
      ('job_level', 'P1', 'P1', NULL, 1, 1, '职级', 1, 1, 0),
      ('job_level', 'P2', 'P2', NULL, 2, 1, '职级', 1, 1, 0),
      ('job_level', 'P3', 'P3', NULL, 3, 1, '职级', 1, 1, 0),
      ('job_level', 'P4', 'P4', NULL, 4, 1, '职级', 1, 1, 0),
      ('job_level', 'P5', 'P5', NULL, 5, 1, '职级', 1, 1, 0),
      ('job_level', 'P6', 'P6', NULL, 6, 1, '职级', 1, 1, 0),
      ('job_level', 'P7', 'P7', NULL, 7, 1, '职级', 1, 1, 0),
      ('job_level', 'P8', 'P8', NULL, 8, 1, '职级', 1, 1, 0),
      ('job_level', 'M1', 'M1', NULL, 9, 1, '职级', 1, 1, 0),
      ('job_level', 'M2', 'M2', NULL, 10, 1, '职级', 1, 1, 0),
      ('job_level', 'M3', 'M3', NULL, 11, 1, '职级', 1, 1, 0);

COMMIT;
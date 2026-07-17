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
(3, 0, 'AI 智能助手', 1, '/ai', '@/pages/ai', 'ai:chat', 'robot', 3, 1, 1, NOW(), NOW(), 0, 0),
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
-- ADMIN: AI 智能助手
(15, 1, 3, NOW(), NOW(), 0, 0),
-- EMPLOYEE: 仅个人中心
(16, 5, 2, NOW(), NOW(), 0, 0),
(17, 5, 201, NOW(), NOW(), 0, 0),
(18, 5, 202, NOW(), NOW(), 0, 0),

-- AI 智能助手菜单 (菜单ID: 3)
-- HR 角色
(19, 2, 3, NOW(), NOW(), 0, 0),
-- MANAGER 角色
(20, 3, 3, NOW(), NOW(), 0, 0),
-- EMPLOYEE 角色
(21, 5, 3, NOW(), NOW(), 0, 0),
-- FINANCE 角色
(22, 4, 3, NOW(), NOW(), 0, 0);

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
    (100, 3, 10, NOW(), NOW(), 0, 0),   -- 李四 → HR_HEAD
    (101, 4, 11, NOW(), NOW(), 0, 0),   -- 王五 → FINANCE_HEAD
    (102, 5, 12, NOW(), NOW(), 0, 0),   -- 赵六 → BOSS
    -- 2026.07.17 10:28 补充角色分配（审批权限区分）
    (103, 2, 3,  NOW(), NOW(), 0, 0),   -- 张三 → MANAGER（技术部经理，审批人权限）
    (104, 6, 5,  NOW(), NOW(), 0, 0);   -- 孙七 → EMPLOYEE（普通员工，无审批权限）

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

-- ============================================================
-- 22. 审批中心测试数据 (M7)
-- 添加时间: 2026.07.17 10:28
-- 说明: 包含请假、补卡、加班、入职、转正等多种审批类型的实例与任务
-- ============================================================

-- ============================================================
-- 22a. 审批实例数据 (hr_approval_instance)
-- 申请人孙七(user_id=6)的请假/补卡/加班审批
-- ============================================================
INSERT IGNORE INTO `hr_approval_instance` (`id`, `approval_no`, `approval_type`, `biz_id`, `title`, `applicant_user_id`, `applicant_employee_id`, `current_node_name`, `approval_status`, `form_json`, `apply_time`, `finish_time`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
(1,  'APR20260716210434', 'LEAVE_REQUEST', 2,  '年假申请-07/20', 6, 5, '直接上级审批', 1,
  '{"leaveType":"ANNUAL","startTime":"2026-07-20 09:00","endTime":"2026-07-20 18:00","totalDays":1,"totalHours":8,"leaveReason":"test"}',
  '2026-07-16 21:04:34', NULL, NOW(), NOW(), 0, 0),
(3,  'APR20260716210737', 'CORRECTION',   2,  '补卡申请-07/15上班卡', 6, 5, '直接上级审批', 1,
  '{"correctionDate":"2026-07-15","correctionType":"CLOCK_IN","correctionReason":"test"}',
  '2026-07-16 21:07:38', NULL, NOW(), NOW(), 0, 0),
(4,  'APR20260716210818', 'CORRECTION',   3,  '补卡申请-07/14下班卡', 6, 5, '直接上级审批', 1,
  '{"correctionDate":"2026-07-14","correctionType":"CLOCK_OUT","correctionReason":"test"}',
  '2026-07-16 21:08:19', NULL, NOW(), NOW(), 0, 0),
(5,  'APR20260716210913', 'LEAVE_REQUEST', 4,  '年假申请-07/21', 6, 5, '直接上级审批', 1,
  '{"leaveType":"ANNUAL","startTime":"2026-07-21 09:00","endTime":"2026-07-21 18:00","totalDays":1,"totalHours":8,"leaveReason":"年假测试"}',
  '2026-07-16 21:09:14', NULL, NOW(), NOW(), 0, 0),
(6,  'APR20260716211444', 'LEAVE_REQUEST', 5,  '病假申请-07/22', 6, 5, '直接上级审批', 1,
  '{"leaveType":"SICK","startTime":"2026-07-22 09:00","endTime":"2026-07-22 18:00","totalDays":1,"totalHours":8,"leaveReason":"病假测试"}',
  '2026-07-16 21:14:44', NULL, NOW(), NOW(), 0, 0),
(7,  'APR20260716211521', 'LEAVE_REQUEST', 6,  '年假申请-07/23', 6, 5, '直接上级审批', 1,
  '{"leaveType":"ANNUAL","startTime":"2026-07-23 09:00","endTime":"2026-07-23 18:00","totalDays":1,"totalHours":8,"leaveReason":"测试上级审批"}',
  '2026-07-16 21:15:22', NULL, NOW(), NOW(), 0, 0),
(8,  'APR20260716214754', 'OVERTIME',      3,  '加班申请-紧急Bug修复', 6, 5, '直接上级审批', 2,
  '{"overtimeDate":"2026-07-16 19:00","duration":2.5,"reason":"紧急Bug修复"}',
  '2026-07-16 21:47:54', '2026-07-16 21:49:42', NOW(), NOW(), 0, 1),
(9,  'APR20260716214951', 'OVERTIME',      4,  '加班申请-Night build', 6, 5, '直接上级审批', 3,
  '{"overtimeDate":"2026-07-17 20:00","duration":1.5,"reason":"Night build"}',
  '2026-07-16 21:49:52', '2026-07-16 21:50:04', NOW(), NOW(), 0, 1);

-- ============================================================
-- 22b. 审批任务数据 (hr_approval_task)
-- 审批人为张三(user_id=2，孙七的直接上级)
-- ============================================================
INSERT IGNORE INTO `hr_approval_task` (`id`, `instance_id`, `node_code`, `node_name`, `approver_user_id`, `original_approver_id`, `delegate_flag`, `task_status`, `approve_result`, `approve_comment`, `receive_time`, `approve_time`, `deadline_time`, `sort_no`, `create_time`, `update_time`) VALUES
(1, 1, 'SUPERIOR', '直接上级审批', 2, NULL, 0, 0, NULL, NULL, '2026-07-16 21:04:35', NULL, '2026-07-19 21:04:34', 1, NOW(), NOW()),
(2, 3, 'SUPERIOR', '直接上级审批', 2, NULL, 0, 0, NULL, NULL, '2026-07-16 21:07:38', NULL, '2026-07-19 21:07:37', 1, NOW(), NOW()),
(3, 4, 'SUPERIOR', '直接上级审批', 2, NULL, 0, 0, NULL, NULL, '2026-07-16 21:08:19', NULL, '2026-07-19 21:08:18', 1, NOW(), NOW()),
(4, 5, 'SUPERIOR', '直接上级审批', 2, NULL, 0, 0, NULL, NULL, '2026-07-16 21:09:14', NULL, '2026-07-19 21:09:13', 1, NOW(), NOW()),
(5, 6, 'SUPERIOR', '直接上级审批', 2, NULL, 0, 0, NULL, NULL, '2026-07-16 21:14:44', NULL, '2026-07-19 21:14:44', 1, NOW(), NOW()),
(6, 7, 'SUPERIOR', '直接上级审批', 2, NULL, 0, 0, NULL, NULL, '2026-07-16 21:15:22', NULL, '2026-07-19 21:15:21', 1, NOW(), NOW()),
(7, 8, 'SUPERIOR', '直接上级审批', 2, NULL, 0, 1, 1, 'Approved',  '2026-07-16 21:47:54', '2026-07-16 21:49:42', '2026-07-19 21:47:54', 1, NOW(), NOW()),
(8, 9, 'SUPERIOR', '直接上级审批', 2, NULL, 0, 1, 2, 'Rejected, work hours only', '2026-07-16 21:49:52', '2026-07-16 21:50:04', '2026-07-19 21:49:51', 1, NOW(), NOW());

-- ============================================================
-- 22c. 委托审批数据 (hr_approval_delegation)
-- ============================================================
INSERT IGNORE INTO `hr_approval_delegation` (`id`, `delegator_id`, `delegator_name`, `delegate_to_id`, `delegate_to_name`, `start_date`, `end_date`, `reason`, `status`, `create_time`, `update_time`) VALUES
(1, 1, '系统管理员', 2, '张三', '2026-07-15 00:00:00', '2026-07-31 23:59:59', '出差委托', 0, NOW(), NOW()),
(2, 1, '系统管理员', 2, '张三', '2026-07-25 00:00:00', '2026-07-26 23:59:59', '出差审批委托', 1, NOW(), NOW());

-- ============================================================
-- 23. 数据修复（针对已有数据库的存量数据修正）
-- 添加时间: 2026.07.17 10:28
-- 说明: 如果上述 INSERT IGNORE 因主键冲突未生效（数据库已存在旧数据），
--       则执行以下 UPDATE 修复旧数据中的字段值问题。
--       新数据库无需执行（INSERT IGNORE 已插入正确值）。
-- ============================================================

-- 23a. 修复委托记录中的名称（旧数据存储了用户ID而非姓名） 2026.07.17 10:58
UPDATE hr_approval_delegation SET delegator_name = '系统管理员' WHERE id = 1 AND delegator_name NOT IN ('系统管理员', '张三', '李四', '王五', '赵六', '孙七');
UPDATE hr_approval_delegation SET delegate_to_name = '张三' WHERE id = 1 AND delegate_to_name NOT IN ('系统管理员', '张三', '李四', '王五', '赵六', '孙七');
UPDATE hr_approval_delegation SET delegate_to_name = '张三' WHERE id = 2 AND (delegate_to_name = '系统管理员' OR delegate_to_name = '1');

-- 23b. 补充任务截止时间（按申请时间+72h设置默认截止时间） 2026.07.17 10:58
UPDATE hr_approval_task t
  JOIN hr_approval_instance i ON t.instance_id = i.id AND i.is_deleted = 0
  SET t.deadline_time = DATE_ADD(i.apply_time, INTERVAL 3 DAY)
  WHERE t.deadline_time IS NULL;

-- 23d. 修复已有截止时间早于申请时间或明显错误的旧数据（如2024年的错误日期） 2026.07.17 10:58
UPDATE hr_approval_task t
  JOIN hr_approval_instance i ON t.instance_id = i.id AND i.is_deleted = 0
  SET t.deadline_time = DATE_ADD(i.apply_time, INTERVAL 3 DAY)
  WHERE t.deadline_time IS NOT NULL
    AND (t.deadline_time < i.apply_time
      OR t.deadline_time < '2026-01-01 00:00:00');

-- 23c. 补充用户角色关联（确保审批权限区分） 2026.07.17 10:58
INSERT IGNORE INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`, `update_time`, `is_deleted`, `version`) VALUES
    (103, 2, 3, NOW(), NOW(), 0, 0),   -- 张三 → MANAGER
    (104, 6, 5, NOW(), NOW(), 0, 0);   -- 孙七 → EMPLOYEE
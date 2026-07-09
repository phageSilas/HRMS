-- ========================================
-- HRMS 初始化数据 - 字典类型
-- 创建时间: 2026-07-09
-- ========================================

-- 字典类型
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`) VALUES
(1, '性别', 'gender', 1, '用户性别'),
(2, '员工在职状态', 'employment_status', 1, '员工在职状态'),
(3, '合同类型', 'contract_type', 1, '员工合同类型'),
(4, '入职类型', 'hire_type', 1, '员工入职类型'),
(5, '审批状态', 'approval_status', 1, '审批流程状态'),
(6, '职位序列', 'post_sequence', 1, '职位序列类型'),
(7, '职级', 'job_level', 1, '职级范围'),
(8, '数据权限范围', 'data_scope', 1, '数据权限范围');

-- ========================================
-- 字典数据 - 性别
-- ========================================
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `label`, `value`, `sort_no`, `status`, `is_default`) VALUES
(1, 'gender', '男', '1', 1, 1, 1),
(2, 'gender', '女', '2', 2, 1, 0);

-- ========================================
-- 字典数据 - 员工在职状态
-- ========================================
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `label`, `value`, `sort_no`, `status`, `is_default`) VALUES
(3, 'employment_status', '试用期', '1', 1, 1, 1),
(4, 'employment_status', '正式', '2', 2, 1, 0),
(5, 'employment_status', '待离职', '3', 3, 1, 0),
(6, 'employment_status', '已离职', '4', 4, 1, 0);

-- ========================================
-- 字典数据 - 合同类型
-- ========================================
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `label`, `value`, `sort_no`, `status`, `is_default`) VALUES
(7, 'contract_type', '固定期限', '1', 1, 1, 1),
(8, 'contract_type', '无固定期限', '2', 2, 1, 0),
(9, 'contract_type', '劳务合同', '3', 3, 1, 0);

-- ========================================
-- 字典数据 - 入职类型
-- ========================================
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `label`, `value`, `sort_no`, `status`, `is_default`) VALUES
(10, 'hire_type', '全职', '1', 1, 1, 1),
(11, 'hire_type', '兼职', '2', 2, 1, 0),
(12, 'hire_type', '实习', '3', 3, 1, 0);

-- ========================================
-- 字典数据 - 审批状态
-- ========================================
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `label`, `value`, `sort_no`, `status`, `is_default`) VALUES
(13, 'approval_status', '草稿', '0', 1, 1, 1),
(14, 'approval_status', '审批中', '1', 2, 1, 0),
(15, 'approval_status', '已通过', '2', 3, 1, 0),
(16, 'approval_status', '已驳回', '3', 4, 1, 0),
(17, 'approval_status', '已撤回', '4', 5, 1, 0);

-- ========================================
-- 字典数据 - 职位序列
-- ========================================
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `label`, `value`, `sort_no`, `status`, `is_default`) VALUES
(18, 'post_sequence', 'M序列(管理)', 'M', 1, 1, 0),
(19, 'post_sequence', 'P序列(专业)', 'P', 2, 1, 1),
(20, 'post_sequence', 'S序列(支持)', 'S', 3, 1, 0);

-- ========================================
-- 字典数据 - 职级
-- ========================================
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `label`, `value`, `sort_no`, `status`, `is_default`) VALUES
(21, 'job_level', 'M1', 'M1', 1, 1, 0),
(22, 'job_level', 'M2', 'M2', 2, 1, 0),
(23, 'job_level', 'M3', 'M3', 3, 1, 0),
(24, 'job_level', 'M4', 'M4', 4, 1, 0),
(25, 'job_level', 'M5', 'M5', 5, 1, 0),
(26, 'job_level', 'P1', 'P1', 11, 1, 0),
(27, 'job_level', 'P2', 'P2', 12, 1, 0),
(28, 'job_level', 'P3', 'P3', 13, 1, 0),
(29, 'job_level', 'P4', 'P4', 14, 1, 0),
(30, 'job_level', 'P5', 'P5', 15, 1, 1),
(31, 'job_level', 'P6', 'P6', 16, 1, 0),
(32, 'job_level', 'P7', 'P7', 17, 1, 0),
(33, 'job_level', 'P8', 'P8', 18, 1, 0),
(34, 'job_level', 'P9', 'P9', 19, 1, 0),
(35, 'job_level', 'P10', 'P10', 20, 1, 0),
(36, 'job_level', 'S1', 'S1', 31, 1, 0),
(37, 'job_level', 'S2', 'S2', 32, 1, 0),
(38, 'job_level', 'S3', 'S3', 33, 1, 0),
(39, 'job_level', 'S4', 'S4', 34, 1, 0),
(40, 'job_level', 'S5', 'S5', 35, 1, 0);

-- ========================================
-- 字典数据 - 数据权限范围
-- ========================================
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `label`, `value`, `sort_no`, `status`, `is_default`) VALUES
(41, 'data_scope', '全部数据', 'ALL', 1, 1, 0),
(42, 'data_scope', '本部门及下级', 'DEPT_AND_CHILD', 2, 1, 0),
(43, 'data_scope', '仅本人', 'SELF', 3, 1, 1);
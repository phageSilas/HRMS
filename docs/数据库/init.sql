-- ============================================================
-- HRMS 人资管理系统 数据库初始化脚本
-- ============================================================
-- 版本: v1.0
-- 创建时间: 2026-07-13
-- 说明: 包含 HRMS 系统全部 32 张数据表
-- ============================================================
-- 表清单（共 32 张）
-- ============================================================
-- M5 权限体系 (6张): sys_user, sys_role, sys_menu, sys_user_role, sys_role_menu, sys_field_permission
-- M6 组织架构 (4张): sys_dept, sys_post, sys_dict_type, sys_dict_data
-- M1 员工档案 (2张): hr_employee, hr_employee_contract
-- M2 入转调离 (4张): hr_entry_application, hr_transfer_application, hr_regular_application, hr_leave_application
-- M3 考勤管理 (4张): hr_attendance_group, hr_attendance_record, hr_leave_request, hr_attendance_correction
-- M4 薪资管理 (5张): hr_salary_template, hr_salary_template_item, hr_employee_salary_profile, hr_salary_batch, hr_salary_batch_item
-- M7 审批中心 (3张): hr_approval_instance, hr_approval_task, hr_approval_delegation
-- M9 AI 助手  (1张): hr_ai_conversation
-- 公共模块   (3张): sys_file, sys_operate_log, sys_login_log
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `hrms`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `hrms`;

-- ============================================================
-- M5 权限体系模块
-- ============================================================

-- ----------------------------------------
-- sys_user（系统用户表）
-- ----------------------------------------
CREATE TABLE `sys_user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
  `password` VARCHAR(255) NOT NULL COMMENT '登录密码（BCrypt 加密，Cost=10）',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '用户昵称',
  `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  `employee_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联员工 ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录 IP',
  `need_change_password` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '首次登录强制修改密码',
  `password_update_time` DATETIME DEFAULT NULL COMMENT '密码最后更新时间',
  `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `lock_time` DATETIME DEFAULT NULL COMMENT '账号锁定时间',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  UNIQUE KEY `uk_sys_user_phone` (`phone`),
  KEY `idx_sys_user_employee_id` (`employee_id`),
  KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

-- ----------------------------------------
-- sys_role（角色表）
-- ----------------------------------------
CREATE TABLE `sys_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
  `data_scope` TINYINT NOT NULL DEFAULT 1 COMMENT '数据权限范围：1-仅本人 2-本部门 3-本部门及子部门 4-全部',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_role_code` (`role_code`),
  KEY `idx_sys_role_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- ----------------------------------------
-- sys_menu（菜单表）
-- ----------------------------------------
CREATE TABLE `sys_menu` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级菜单 ID',
  `menu_name` VARCHAR(64) NOT NULL COMMENT '菜单名称',
  `menu_type` TINYINT NOT NULL COMMENT '菜单类型：1-目录 2-菜单 3-按钮',
  `path` VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
  `component` VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径',
  `permission` VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `visible` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可见',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_sys_menu_parent_id` (`parent_id`),
  KEY `idx_sys_menu_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- ----------------------------------------
-- sys_user_role（用户角色关联表）
-- ----------------------------------------
CREATE TABLE `sys_user_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色 ID',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_role_user_role` (`user_id`, `role_id`),
  KEY `idx_sys_user_role_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

-- ----------------------------------------
-- sys_role_menu（角色菜单关联表）
-- ----------------------------------------
CREATE TABLE `sys_role_menu` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色 ID',
  `menu_id` BIGINT UNSIGNED NOT NULL COMMENT '菜单 ID',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_menu_role_menu` (`role_id`, `menu_id`),
  KEY `idx_sys_role_menu_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';

-- ----------------------------------------
-- sys_field_permission（字段权限配置表）
-- ----------------------------------------
CREATE TABLE `sys_field_permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型：employee、sawmlary、attendance 等',
  `field_name` VARCHAR(64) NOT NULL COMMENT '字段名',
  `field_desc` VARCHAR(128) DEFAULT NULL COMMENT '字段描述',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `viewable` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见：1-是 0-否',
  `editable` TINYINT NOT NULL DEFAULT 0 COMMENT '是否可编辑：1-是 0-否',
  `flow_required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否需审批：1-是 0-否',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_field_perm_biz_role_field` (`biz_type`, `role_id`, `field_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字段权限配置表';

-- ============================================================
-- M6 组织架构模块
-- ============================================================

-- ----------------------------------------
-- sys_dept（部门表）
-- ----------------------------------------
CREATE TABLE `sys_dept` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '上级部门 ID，0=根部门',
  `dept_name` VARCHAR(64) NOT NULL COMMENT '部门名称',
  `dept_code` VARCHAR(16) NOT NULL COMMENT '部门编码（用于工号生成）',
  `leader_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '部门负责人用户 ID',
  `leader_employee_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '部门负责人员工 ID',
  `ancestors` VARCHAR(500) DEFAULT NULL COMMENT '祖级路径（逗号分隔的祖先部门ID链）',
  `dept_level` INT NOT NULL DEFAULT 1 COMMENT '部门层级（根=1，最大=5）',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `employee_count` INT NOT NULL DEFAULT 0 COMMENT '在职员工数缓存（含本部门及所有下属部门）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dept_dept_code` (`dept_code`),
  KEY `idx_sys_dept_parent_id` (`parent_id`),
  KEY `idx_sys_dept_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- ----------------------------------------
-- sys_post（职位表）
-- ----------------------------------------
CREATE TABLE `sys_post` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_name` VARCHAR(64) NOT NULL COMMENT '职位名称',
  `post_code` VARCHAR(64) NOT NULL COMMENT '职位编码',
  `sequence_code` VARCHAR(16) NOT NULL COMMENT '职位序列：M-管理序列 P-专业序列 S-支持序列',
  `dept_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '所属部门 ID，NULL=全公司通用',
  `job_level_min` VARCHAR(16) DEFAULT NULL COMMENT '职级下限（如 P3）',
  `job_level_max` VARCHAR(16) DEFAULT NULL COMMENT '职级上限（如 P7）',
  `default_probation_month` INT NOT NULL DEFAULT 3 COMMENT '默认试用期（月）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '职位描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_post_post_code` (`post_code`),
  KEY `idx_sys_post_dept_id` (`dept_id`),
  KEY `idx_sys_post_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='职位表';

-- ----------------------------------------
-- sys_dict_type（字典类型表）
-- ----------------------------------------
CREATE TABLE `sys_dict_type` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_name` VARCHAR(64) NOT NULL COMMENT '字典名称',
  `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型编码',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_type_type` (`dict_type`),
  KEY `idx_sys_dict_type_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- ----------------------------------------
-- sys_dict_data（字典数据表）
-- ----------------------------------------
CREATE TABLE `sys_dict_data` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型编码',
  `dict_label` VARCHAR(64) NOT NULL COMMENT '字典标签',
  `dict_value` VARCHAR(64) NOT NULL COMMENT '字典值',
  `css_class` VARCHAR(64) DEFAULT NULL COMMENT '样式属性',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_dict_data_type` (`dict_type`),
  KEY `idx_sys_dict_data_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- ============================================================
-- M1 员工档案模块
-- ============================================================

-- ----------------------------------------
-- hr_employee（员工主档表）
-- ----------------------------------------
CREATE TABLE `hr_employee` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_no` VARCHAR(32) NOT NULL COMMENT '工号',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联系统用户ID',
  `dept_id` BIGINT UNSIGNED NOT NULL COMMENT '所属部门ID',
  `post_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '职位ID',
  `leader_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '直接汇报人员工ID',
  `employee_name` VARCHAR(64) NOT NULL COMMENT '员工姓名',
  `gender` TINYINT DEFAULT NULL COMMENT '性别：1-男 2-女',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `id_card_no` VARCHAR(255) DEFAULT NULL COMMENT '身份证号（AES-256 GCM 加密存储）',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `domicile_address` VARCHAR(255) DEFAULT NULL COMMENT '户籍地址',
  `current_address` VARCHAR(255) DEFAULT NULL COMMENT '现居住地址',
  `job_level` VARCHAR(16) DEFAULT NULL COMMENT '职级',
  `work_location` VARCHAR(128) DEFAULT NULL COMMENT '工作地点',
  `hire_type` TINYINT DEFAULT NULL COMMENT '入职类型：1-全职 2-兼职 3-实习',
  `employment_status` TINYINT NOT NULL COMMENT '在职状态：1-试用期 2-正式 3-待离职 4-已离职',
  `hire_date` DATE NOT NULL COMMENT '入职日期',
  `probation_month` INT DEFAULT 3 COMMENT '试用期（月）',
  `probation_salary_ratio` DECIMAL(5,2) DEFAULT 100.00 COMMENT '试用期薪资比例（%）',
  `contract_type` TINYINT DEFAULT NULL COMMENT '合同类型：1-固定期限 2-无固定期限 3-劳务合同',
  `contract_expire_date` DATE DEFAULT NULL COMMENT '合同到期日',
  `salary_template_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '薪资账套ID',
  `base_salary` DECIMAL(12,2) DEFAULT NULL COMMENT '基本工资',
  `bank_account` VARCHAR(255) DEFAULT NULL COMMENT '银行账号（AES-256 GCM 加密存储）',
  `bank_name` VARCHAR(128) DEFAULT NULL COMMENT '开户行',
  `emergency_contact` VARCHAR(64) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系人电话',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_employee_no` (`employee_no`),
  UNIQUE KEY `uk_hr_employee_phone` (`phone`),
  UNIQUE KEY `uk_hr_employee_user_id` (`user_id`),
  KEY `idx_hr_employee_dept_id` (`dept_id`),
  KEY `idx_hr_employee_post_id` (`post_id`),
  KEY `idx_hr_employee_leader_id` (`leader_id`),
  KEY `idx_hr_employee_employment_status` (`employment_status`),
  KEY `idx_hr_employee_hire_date` (`hire_date`),
  KEY `idx_hr_employee_contract_expire` (`contract_expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工主档表';

-- ----------------------------------------
-- hr_employee_contract（员工合同表）
-- ----------------------------------------
CREATE TABLE `hr_employee_contract` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `contract_no` VARCHAR(64) DEFAULT NULL COMMENT '合同编号',
  `contract_type` TINYINT NOT NULL COMMENT '合同类型：1-固定期限 2-无固定期限 3-劳务合同',
  `start_date` DATE DEFAULT NULL COMMENT '合同开始日期',
  `end_date` DATE DEFAULT NULL COMMENT '合同结束日期',
  `probation_month` INT DEFAULT NULL COMMENT '试用期（月）',
  `probation_salary_ratio` DECIMAL(5,2) DEFAULT NULL COMMENT '试用期薪资比例（%）',
  `attachment_file_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '附件文件ID',
  `signing_count` INT NOT NULL DEFAULT 1 COMMENT '续签次数',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_hr_emp_contract_employee_id` (`employee_id`),
  KEY `idx_hr_emp_contract_end_date` (`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工合同表';

-- ============================================================
-- M2 入转调离模块
-- ============================================================

-- ----------------------------------------
-- hr_entry_application（入职申请表）
-- ----------------------------------------
DROP TABLE IF EXISTS `hr_entry_application`;

CREATE TABLE `hr_entry_application` (
   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
   `candidate_name` VARCHAR(64) NOT NULL COMMENT '候选人姓名',
   `gender` TINYINT DEFAULT NULL COMMENT '性别',
   `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
   `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
   `id_card_no` VARCHAR(255) DEFAULT NULL COMMENT '身份证号',
   `dept_id` BIGINT UNSIGNED NOT NULL COMMENT '拟入职部门ID',
   `post_id` BIGINT UNSIGNED NOT NULL COMMENT '拟入职职位ID',
   `hire_type` TINYINT NOT NULL COMMENT '录用类型：1-全职 2-兼职 3-实习',
   `probation_month` INT NOT NULL COMMENT '试用期（月）',
   `probation_salary_ratio` DECIMAL(5,2) NOT NULL DEFAULT 80.00 COMMENT '试用期薪资比例（%）',
   `expected_hire_date` DATE NOT NULL COMMENT '预计入职日期',
   `leader_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '直接汇报人',
   `approval_instance_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批实例ID',
   `approval_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态：0-草稿 1-审批中 2-已通过 3-已拒绝 5-已入职',
   `actual_hire_date` DATE DEFAULT NULL COMMENT '实际入职日期（HR确认时填写）',
   `employee_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '确认入职后关联的员工ID',
   `employee_no` VARCHAR(32) DEFAULT NULL COMMENT '确认入职后关联的员工工号',
   `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
   `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
   `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
   `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
   PRIMARY KEY (`id`),
   UNIQUE KEY `uk_hr_entry_app_phone` (`phone`),
   KEY `idx_hr_entry_app_status` (`approval_status`),
   KEY `idx_hr_entry_app_dept` (`dept_id`),
   KEY `idx_hr_entry_app_employee_id` (`employee_id`),
   KEY `idx_hr_entry_app_employee_no` (`employee_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入职申请表';

-- ----------------------------------------
-- hr_transfer_application（调岗申请表）
-- ----------------------------------------
CREATE TABLE `hr_transfer_application` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `from_dept_id` BIGINT UNSIGNED NOT NULL COMMENT '原部门ID',
  `to_dept_id` BIGINT UNSIGNED NOT NULL COMMENT '新部门ID',
  `from_post_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '原职位ID',
  `to_post_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '新职位ID',
  `from_job_level` VARCHAR(16) DEFAULT NULL COMMENT '原职级',
  `to_job_level` VARCHAR(16) DEFAULT NULL COMMENT '新职级',
  `from_leader_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '原汇报人ID',
  `to_leader_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '新汇报人ID',
  `salary_adjustment` DECIMAL(12,2) DEFAULT NULL COMMENT '薪资调整金额（正=调增 负=调减）',
  `effective_date` DATE NOT NULL COMMENT '生效日期',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '调岗原因',
  `approval_instance_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批实例ID',
  `approval_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_hr_transfer_app_employee` (`employee_id`),
  KEY `idx_hr_transfer_app_status` (`approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调岗申请表';

-- ----------------------------------------
-- hr_regular_application（转正申请表）
-- ----------------------------------------
CREATE TABLE `hr_regular_application` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `probation_start_date` DATE DEFAULT NULL COMMENT '试用期开始日期',
  `probation_end_date` DATE DEFAULT NULL COMMENT '试用期结束日期',
  `evaluate_result` TINYINT NOT NULL DEFAULT 1 COMMENT '评估结果：1-转正 2-延长试用 3-辞退',
  `extend_month` INT DEFAULT NULL COMMENT '延长试用月数（延长试用时填写）',
  `salary_adjustment` DECIMAL(12,2) DEFAULT NULL COMMENT '调薪金额',
  `evaluate_opinion` VARCHAR(500) DEFAULT NULL COMMENT '评估意见',
  `approval_instance_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批实例ID',
  `approval_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态：0-草稿 1-审批中 2-已通过 3-已拒绝',
  `regular_date` DATE DEFAULT NULL COMMENT '实际转正日期（审批通过后填写）',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_hr_regular_app_employee` (`employee_id`),
  KEY `idx_hr_regular_app_status` (`approval_status`),
  KEY `idx_hr_regular_app_probation_end` (`probation_end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转正申请表';

-- ----------------------------------------
-- hr_leave_application（离职申请表）
-- ----------------------------------------
CREATE TABLE `hr_leave_application` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `leave_type` TINYINT NOT NULL COMMENT '离职类型：1-主动辞职 2-被动辞退 3-合同到期不续签 4-其他',
  `leave_reason` VARCHAR(500) DEFAULT NULL COMMENT '离职原因',
  `apply_date` DATE NOT NULL COMMENT '申请日期',
  `expected_last_work_date` DATE NOT NULL COMMENT '预计最后工作日',
  `last_work_date` DATE DEFAULT NULL COMMENT '实际最后工作日（审批通过后填写）',
  `handover_employee_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '交接人员工ID',
  `handover_status` TINYINT NOT NULL DEFAULT 0 COMMENT '交接状态：0-未交接 1-交接中 2-已交接',
  `handover_note` VARCHAR(500) DEFAULT NULL COMMENT '交接说明',
  `approval_instance_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批实例ID',
  `approval_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态：0-草稿 1-审批中 2-已通过 3-已拒绝',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_hr_leave_app_employee` (`employee_id`),
  KEY `idx_hr_leave_app_status` (`approval_status`),
  KEY `idx_hr_leave_app_last_work` (`last_work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='离职申请表';

-- ============================================================
-- M3 考勤管理模块
-- ============================================================

-- ----------------------------------------
-- hr_attendance_group（考勤组表）
-- ----------------------------------------
CREATE TABLE `hr_attendance_group` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_name` VARCHAR(64) NOT NULL COMMENT '考勤组名称',
  `shift_type` VARCHAR(32) NOT NULL COMMENT '班次类型：FIXED/FLEXIBLE/SCHEDULED',
  `work_start_time` TIME NOT NULL COMMENT '上班时间',
  `work_end_time` TIME NOT NULL COMMENT '下班时间',
  `rest_start_time` TIME DEFAULT NULL COMMENT '午休开始',
  `rest_end_time` TIME DEFAULT NULL COMMENT '午休结束',
  `flexible_start_time` TIME DEFAULT NULL COMMENT '弹性最早打卡',
  `flexible_end_time` TIME DEFAULT NULL COMMENT '弹性最晚打卡',
  `late_threshold_minutes` INT NOT NULL DEFAULT 15 COMMENT '迟到阈值（分钟）',
  `early_leave_threshold_minutes` INT NOT NULL DEFAULT 15 COMMENT '早退阈值（分钟）',
  `clock_ip_whitelist` VARCHAR(500) DEFAULT NULL COMMENT 'IP白名单，逗号分隔',
  `clock_gps_scope` VARCHAR(500) DEFAULT NULL COMMENT 'GPS范围配置（中心点+半径）',
  `monthly_correction_limit` INT NOT NULL DEFAULT 2 COMMENT '月补卡次数上限',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_hr_att_group_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤组表';

-- ----------------------------------------
-- hr_attendance_record（打卡记录表）
-- ----------------------------------------
CREATE TABLE `hr_attendance_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `group_id` BIGINT UNSIGNED NOT NULL COMMENT '考勤组ID',
  `record_date` DATE NOT NULL COMMENT '打卡日期',
  `clock_in_time` DATETIME DEFAULT NULL COMMENT '上班打卡时间',
  `clock_out_time` DATETIME DEFAULT NULL COMMENT '下班打卡时间',
  `clock_in_status` VARCHAR(32) DEFAULT NULL COMMENT '上班状态：NORMAL/LATE/MISSING/ABSENCE',
  `clock_out_status` VARCHAR(32) DEFAULT NULL COMMENT '下班状态：NORMAL/EARLY_LEAVE/MISSING/ABSENCE',
  `clock_in_ip` VARCHAR(64) DEFAULT NULL COMMENT '上班打卡IP',
  `clock_out_ip` VARCHAR(64) DEFAULT NULL COMMENT '下班打卡IP',
  `clock_in_gps` VARCHAR(128) DEFAULT NULL COMMENT '上班打卡GPS',
  `clock_out_gps` VARCHAR(128) DEFAULT NULL COMMENT '下班打卡GPS',
  `device_info` VARCHAR(255) DEFAULT NULL COMMENT '设备信息',
  `correction_status` VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT '补卡状态：NONE/PENDING/APPROVED',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_att_rec_emp_date` (`employee_id`, `record_date`),
  KEY `idx_hr_att_rec_group_date` (`group_id`, `record_date`),
  KEY `idx_hr_att_rec_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='打卡记录表';

-- ----------------------------------------
-- hr_leave_request（请假申请表）
-- ----------------------------------------
CREATE TABLE `hr_leave_request` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `leave_type` VARCHAR(32) NOT NULL COMMENT '请假类型：ANNUAL-年假 COMPASSIONATE-调休 SICK-病假 PERSONAL-事假 MARRIAGE-婚假 MATERNITY-产假 FUNERAL-丧假',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `total_days` DECIMAL(5,1) NOT NULL COMMENT '请假天数',
  `total_hours` DECIMAL(5,1) DEFAULT NULL COMMENT '请假小时数（按小时请假时使用）',
  `leave_reason` VARCHAR(500) DEFAULT NULL COMMENT '请假原因',
  `attachment_url` VARCHAR(500) DEFAULT NULL COMMENT '附件地址（病假需病历等）',
  `approval_instance_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批实例ID',
  `approval_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态：0-草稿 1-审批中 2-已通过 3-已拒绝 4-已撤回',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_hr_leave_req_employee` (`employee_id`),
  KEY `idx_hr_leave_req_status` (`approval_status`),
  KEY `idx_hr_leave_req_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='请假申请表';

-- ----------------------------------------
-- hr_attendance_correction（补卡申请表）
-- ----------------------------------------
CREATE TABLE `hr_attendance_correction` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `record_id` BIGINT UNSIGNED NOT NULL COMMENT '打卡记录ID',
  `correction_date` DATE NOT NULL COMMENT '补卡日期',
  `correction_type` VARCHAR(32) NOT NULL COMMENT '补卡类型：CLOCK_IN-上班补卡 CLOCK_OUT-下班补卡',
  `correction_reason` VARCHAR(500) NOT NULL COMMENT '补卡原因',
  `approval_instance_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批实例ID',
  `approval_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态：0-草稿 1-审批中 2-已通过 3-已拒绝',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_hr_att_corr_employee` (`employee_id`),
  KEY `idx_hr_att_corr_record` (`record_id`),
  KEY `idx_hr_att_corr_date` (`correction_date`),
  KEY `idx_hr_att_corr_status` (`approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='补卡申请表';

-- ============================================================
-- M4 薪资管理模块
-- ============================================================

-- ----------------------------------------
-- hr_salary_template（薪资账套表）
-- ----------------------------------------
CREATE TABLE `hr_salary_template` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` VARCHAR(64) NOT NULL COMMENT '账套名称',
  `template_code` VARCHAR(64) NOT NULL COMMENT '账套编码',
  `scope_type` VARCHAR(32) NOT NULL DEFAULT 'ALL' COMMENT '适用范围：ALL-全部 DEPT-指定部门',
  `scope_value` VARCHAR(500) DEFAULT NULL COMMENT '适用范围值（部门ID列表等）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_sal_tpl_code` (`template_code`),
  KEY `idx_hr_sal_tpl_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资账套表';

-- ----------------------------------------
-- hr_salary_template_item（薪资账套项目表）
-- ----------------------------------------
CREATE TABLE `hr_salary_template_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` BIGINT UNSIGNED NOT NULL COMMENT '账套ID',
  `item_code` VARCHAR(32) NOT NULL COMMENT '工资项目编码',
  `item_name` VARCHAR(64) NOT NULL COMMENT '工资项目名称',
  `category` VARCHAR(32) NOT NULL COMMENT '分类：INCOME-收入 DEDUCTION-扣除',
  `calc_rule` VARCHAR(500) DEFAULT NULL COMMENT '计算规则',
  `default_value` DECIMAL(12,2) DEFAULT NULL COMMENT '默认值',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_hr_sal_tpl_item_tpl` (`template_id`),
  KEY `idx_hr_sal_tpl_item_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资账套项目表';

-- ----------------------------------------
-- hr_employee_salary_profile（员工薪资档案表）
-- ----------------------------------------
CREATE TABLE `hr_employee_salary_profile` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `template_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '适用账套ID',
  `base_salary` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '基本工资',
  `allowance` DECIMAL(12,2) DEFAULT 0 COMMENT '岗位津贴',
  `performance_base` DECIMAL(12,2) DEFAULT 0 COMMENT '绩效基数',
  `social_insurance_base` DECIMAL(12,2) DEFAULT 0 COMMENT '社保基数',
  `housing_fund_base` DECIMAL(12,2) DEFAULT 0 COMMENT '公积金基数',
  `bank_name` VARCHAR(64) DEFAULT NULL COMMENT '开户银行',
  `bank_account` VARCHAR(255) DEFAULT NULL COMMENT '银行卡号（加密存储）',
  `effective_date` DATE DEFAULT NULL COMMENT '生效日期',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_emp_sal_prof_employee` (`employee_id`),
  KEY `idx_hr_emp_sal_prof_tpl` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工薪资档案表';

-- ----------------------------------------
-- hr_salary_batch（薪资批次表）
-- ----------------------------------------
CREATE TABLE `hr_salary_batch` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '薪资批次编号',
  `salary_month` CHAR(7) NOT NULL COMMENT '薪资月份 yyyy-MM',
  `scope_type` VARCHAR(32) NOT NULL DEFAULT 'ALL' COMMENT '核算范围：ALL/DEPT/EMPLOYEE',
  `scope_value` VARCHAR(500) DEFAULT NULL COMMENT '核算范围值',
  `batch_status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '批次状态',
  `approval_instance_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批实例ID',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '核算员工总数',
  `total_gross_salary` DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '应发总额',
  `total_net_salary` DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '实发总额',
  `yellow_warning_count` INT NOT NULL DEFAULT 0 COMMENT '黄色预警数',
  `red_warning_count` INT NOT NULL DEFAULT 0 COMMENT '红色预警数',
  `block_count` INT NOT NULL DEFAULT 0 COMMENT '阻断异常数',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_salary_batch_no` (`batch_no`),
  KEY `idx_hr_salary_batch_status` (`batch_status`),
  KEY `idx_hr_salary_batch_month` (`salary_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资批次表';

-- ----------------------------------------
-- hr_salary_batch_item（薪资明细表）
-- ----------------------------------------
CREATE TABLE `hr_salary_batch_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` BIGINT UNSIGNED NOT NULL COMMENT '批次ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `base_salary` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '基本工资',
  `allowance` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '岗位津贴',
  `performance_bonus` DECIMAL(12,2) DEFAULT 0 COMMENT '绩效奖金',
  `overtime_pay` DECIMAL(12,2) DEFAULT 0 COMMENT '加班费',
  `late_deduction` DECIMAL(10,2) DEFAULT 0 COMMENT '迟到扣款',
  `leave_deduction` DECIMAL(10,2) DEFAULT 0 COMMENT '请假扣款',
  `social_insurance` DECIMAL(10,2) DEFAULT 0 COMMENT '社保（个人部分）',
  `housing_fund` DECIMAL(10,2) DEFAULT 0 COMMENT '公积金（个人部分）',
  `income_tax` DECIMAL(10,2) DEFAULT 0 COMMENT '个人所得税',
  `gross_salary` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '应发合计',
  `deduction_total` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '应扣合计',
  `net_salary` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '实发工资',
  `warning_level` VARCHAR(32) DEFAULT NULL COMMENT '预警级别：NONE/YELLOW/RED/BLOCK',
  `warning_reason` VARCHAR(500) DEFAULT NULL COMMENT '预警原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_hr_sal_item_batch` (`batch_id`),
  KEY `idx_hr_sal_item_employee` (`employee_id`),
  KEY `idx_hr_sal_item_warning` (`warning_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资明细表';

-- ============================================================
-- M7 审批中心模块
-- ============================================================

-- ----------------------------------------
-- hr_approval_instance（审批实例表）
-- ----------------------------------------
CREATE TABLE `hr_approval_instance` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `approval_no` VARCHAR(64) NOT NULL COMMENT '审批单号',
  `approval_type` VARCHAR(32) NOT NULL COMMENT '审批类型编码',
  `biz_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '审批标题',
  `applicant_user_id` BIGINT UNSIGNED NOT NULL COMMENT '申请人用户ID',
  `applicant_employee_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '申请人员工ID',
  `current_node_name` VARCHAR(64) DEFAULT NULL COMMENT '当前节点名称',
  `approval_status` TINYINT NOT NULL COMMENT '状态：0-草稿 1-审批中 2-已通过 3-已驳回 4-已撤回',
  `form_json` JSON DEFAULT NULL COMMENT '表单快照（审批时的业务数据副本）',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_apr_inst_no` (`approval_no`),
  KEY `idx_hr_apr_inst_type` (`approval_type`),
  KEY `idx_hr_apr_inst_status` (`approval_status`),
  KEY `idx_hr_apr_inst_applicant` (`applicant_user_id`),
  KEY `idx_hr_apr_inst_biz` (`approval_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批实例表';

-- ----------------------------------------
-- hr_approval_task（审批任务表）
-- ----------------------------------------
CREATE TABLE `hr_approval_task` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `instance_id` BIGINT UNSIGNED NOT NULL COMMENT '审批实例ID',
  `node_code` VARCHAR(64) NOT NULL COMMENT '节点编码',
  `node_name` VARCHAR(64) NOT NULL COMMENT '节点名称',
  `approver_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '实际审批人用户ID',
  `original_approver_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '原审批人（委托场景）',
  `delegate_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否代审：0-本人 1-代审',
  `task_status` TINYINT NOT NULL COMMENT '任务状态：0-待处理 1-已处理 2-已转交',
  `approve_result` TINYINT DEFAULT NULL COMMENT '结果：1-通过 2-驳回 3-转交',
  `approve_comment` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `receive_time` DATETIME DEFAULT NULL COMMENT '接收时间',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `deadline_time` DATETIME DEFAULT NULL COMMENT '截止时间（超时升级依据）',
  `sort_no` INT NOT NULL DEFAULT 1 COMMENT '节点顺序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_hr_apr_task_instance` (`instance_id`),
  KEY `idx_hr_apr_task_approver` (`approver_user_id`, `task_status`),
  KEY `idx_hr_apr_task_deadline` (`deadline_time`, `task_status`),
  KEY `idx_hr_apr_task_sort` (`instance_id`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批任务表';

-- ----------------------------------------
-- hr_approval_delegation（委托审批表）
-- ----------------------------------------
CREATE TABLE `hr_approval_delegation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `delegator_id` BIGINT UNSIGNED NOT NULL COMMENT '委托人用户ID',
  `delegator_name` VARCHAR(32) NOT NULL COMMENT '委托人姓名',
  `delegate_to_id` BIGINT UNSIGNED NOT NULL COMMENT '被委托人用户ID',
  `delegate_to_name` VARCHAR(32) NOT NULL COMMENT '被委托人姓名',
  `start_date` DATETIME NOT NULL COMMENT '委托生效时间',
  `end_date` DATETIME NOT NULL COMMENT '委托结束时间',
  `reason` VARCHAR(256) DEFAULT NULL COMMENT '委托原因',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已取消 1-生效中 2-已过期',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_hr_apr_del_delg` (`delegator_id`),
  KEY `idx_hr_apr_del_delt` (`delegate_to_id`),
  KEY `idx_hr_apr_del_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='委托审批表';

-- ============================================================
-- M9 AI 助手模块
-- ============================================================

-- ----------------------------------------
-- hr_ai_conversation（AI对话记录表）
-- ----------------------------------------
CREATE TABLE `hr_ai_conversation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID（游客为NULL）',
  `session_token` VARCHAR(64) DEFAULT NULL COMMENT '游客会话标识',
  `title` VARCHAR(200) NOT NULL DEFAULT '新对话' COMMENT '对话标题',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-活跃 2-已归档',
  `message_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息总数',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后消息时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_hr_ai_conv_user` (`user_id`),
  KEY `idx_hr_ai_conv_update` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI对话记录表';

-- ============================================================
-- 公共模块
-- ============================================================

-- ----------------------------------------
-- sys_file（文件表）
-- ----------------------------------------
CREATE TABLE `sys_file` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `file_type` VARCHAR(64) DEFAULT NULL COMMENT '文件类型',
  `mime_type` VARCHAR(128) DEFAULT NULL COMMENT 'MIME类型',
  `md5` VARCHAR(64) DEFAULT NULL COMMENT '文件MD5',
  `business_type` VARCHAR(32) DEFAULT NULL COMMENT '业务类型',
  `business_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务ID',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '上传人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_file_type` (`business_type`),
  KEY `idx_sys_file_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件表';

-- ----------------------------------------
-- sys_operate_log（操作日志表）
-- ----------------------------------------
CREATE TABLE `sys_operate_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
  `username` VARCHAR(64) DEFAULT NULL COMMENT '操作用户名',
  `operate_type` VARCHAR(32) NOT NULL COMMENT '操作类型',
  `operate_module` VARCHAR(64) DEFAULT NULL COMMENT '操作模块',
  `operate_desc` VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
  `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求地址',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
  `response_result` TEXT DEFAULT NULL COMMENT '响应结果',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `execute_time` INT DEFAULT NULL COMMENT '执行时长（毫秒）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '执行状态：1-成功 0-失败',
  `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_oper_log_user` (`user_id`),
  KEY `idx_sys_oper_log_type` (`operate_type`),
  KEY `idx_sys_oper_log_module` (`operate_module`),
  KEY `idx_sys_oper_log_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

-- ----------------------------------------
-- sys_login_log（登录日志表）
-- ----------------------------------------
CREATE TABLE `sys_login_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '登录用户ID',
  `username` VARCHAR(64) DEFAULT NULL COMMENT '登录用户名',
  `login_type` VARCHAR(32) DEFAULT NULL COMMENT '登录类型：ACCOUNT-账号登录 TOKEN-令牌登录',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
  `login_location` VARCHAR(255) DEFAULT NULL COMMENT '登录地点',
  `browser` VARCHAR(64) DEFAULT NULL COMMENT '浏览器',
  `os` VARCHAR(64) DEFAULT NULL COMMENT '操作系统',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '登录状态：1-成功 0-失败',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_login_log_user` (`user_id`),
  KEY `idx_sys_login_log_status` (`status`),
  KEY `idx_sys_login_log_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

-- ============================================================
-- 初始化完成
-- ============================================================
-- 共计 32+1+1 张表
-- M5 权限体系: 6张
-- M6 组织架构: 4张
-- M1 员工档案: 2张
-- M2 入转调离: 4张
-- M3 考勤管理: 4张+1 + 1 共6张
-- M4 薪资管理: 5张
-- M7 审批中心: 3张
-- M9 AI 助手:  1张
-- 公共模块:   3张
-- ============================================================

# 新增 hr_leave_balance，属于考勤管理模块, 专门存员工每年每类假期余额。
CREATE TABLE `hr_leave_balance` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `employee_id` bigint NOT NULL COMMENT '员工ID',
                                    `leave_type` varchar(32) NOT NULL COMMENT '假期类型：ANNUAL-年假 SICK-病假 PERSONAL-事假 COMPASSIONATE-调休 MARRIAGE-婚假 MATERNITY-产假 FUNERAL-丧假',
                                    `balance_year` int NOT NULL COMMENT '余额所属年份',
                                    `total_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '本年度应得天数',
                                    `used_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '已使用天数',
                                    `frozen_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '审批中冻结天数',
                                    `remaining_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '剩余可用天数',
                                    `carryover_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '上年结转天数',
                                    `adjust_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '人工调整天数，可正可负',
                                    `expire_date` date DEFAULT NULL COMMENT '余额过期日期',
                                    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-停用 1-启用',
                                    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                    `create_by` bigint DEFAULT NULL COMMENT '创建人',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_by` bigint DEFAULT NULL COMMENT '更新人',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
                                    `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_employee_leave_year` (`employee_id`, `leave_type`, `balance_year`, `is_deleted`),
                                    KEY `idx_employee_year` (`employee_id`, `balance_year`),
                                    KEY `idx_leave_type_year` (`leave_type`, `balance_year`),
                                    KEY `idx_expire_date` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工假期余额表';

# 新增员工-考勤组生效关系表 hr_attendance_group_member，属于考勤管理模块, 支持后续调岗、换组、历史追溯。
CREATE TABLE `hr_attendance_group_member` (
                                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                              `group_id` bigint NOT NULL COMMENT '考勤组ID',
                                              `employee_id` bigint NOT NULL COMMENT '员工ID',
                                              `effective_start_date` date NOT NULL COMMENT '生效开始日期',
                                              `effective_end_date` date DEFAULT NULL COMMENT '生效结束日期，空表示长期有效',
                                              `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-停用 1-启用',
                                              `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                              `create_by` bigint DEFAULT NULL COMMENT '创建人',
                                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              `update_by` bigint DEFAULT NULL COMMENT '更新人',
                                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
                                              `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
                                              PRIMARY KEY (`id`),
                                              UNIQUE KEY `uk_employee_group_effective` (`employee_id`, `group_id`, `effective_start_date`, `is_deleted`),
                                              KEY `idx_employee_effective` (`employee_id`, `status`, `effective_start_date`, `effective_end_date`),
                                              KEY `idx_group_id` (`group_id`),
                                              KEY `idx_effective_date` (`effective_start_date`, `effective_end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤组成员关系表';
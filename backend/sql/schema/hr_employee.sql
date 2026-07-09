-- ========================================
-- HRMS 业务模块表 - hr_employee
-- 模块归属: hrms-business
-- 跨模块契约字段:
--   - user_id -> sys_user.id
--   - dept_id -> sys_dept.id
--   - post_id -> sys_post.id
--   - leader_id -> hr_employee.id
--   - id -> hr_approval_instance.applicant_employee_id
-- 创建时间: 2026-07-09
-- ========================================

CREATE TABLE `hr_employee` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_no` VARCHAR(32) NOT NULL COMMENT '工号',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联系统用户ID',
  `dept_id` BIGINT UNSIGNED NOT NULL COMMENT '所属部门ID',
  `post_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '职位ID',
  `leader_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '直接汇报人员工ID',
  `employee_name` VARCHAR(64) NOT NULL COMMENT '员工姓名',
  `gender` VARCHAR(16) DEFAULT NULL COMMENT '性别',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `id_card_no` VARCHAR(128) DEFAULT NULL COMMENT '身份证号（建议加密存储）',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `domicile_address` VARCHAR(255) DEFAULT NULL COMMENT '户籍地址',
  `current_address` VARCHAR(255) DEFAULT NULL COMMENT '现居住地址',
  `job_level` VARCHAR(16) DEFAULT NULL COMMENT '职级',
  `work_location` VARCHAR(128) DEFAULT NULL COMMENT '工作地点',
  `hire_type` VARCHAR(32) DEFAULT NULL COMMENT '入职类型',
  `employment_status` VARCHAR(32) NOT NULL COMMENT '在职状态',
  `hire_date` DATE NOT NULL COMMENT '入职日期',
  `probation_month` INT DEFAULT 3 COMMENT '试用期（月）',
  `probation_salary_ratio` DECIMAL(5,2) DEFAULT 100.00 COMMENT '试用期薪资比例',
  `contract_type` VARCHAR(32) DEFAULT NULL COMMENT '合同类型',
  `contract_expire_date` DATE DEFAULT NULL COMMENT '合同到期日',
  `salary_template_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '薪资账套ID',
  `base_salary` DECIMAL(12,2) DEFAULT NULL COMMENT '基本工资',
  `bank_account` VARCHAR(128) DEFAULT NULL COMMENT '银行账号（建议加密存储）',
  `bank_name` VARCHAR(128) DEFAULT NULL COMMENT '开户行',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '记录状态',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_employee_employee_no` (`employee_no`),
  UNIQUE KEY `uk_hr_employee_phone` (`phone`),
  KEY `idx_hr_employee_user_id` (`user_id`),
  KEY `idx_hr_employee_dept_id` (`dept_id`),
  KEY `idx_hr_employee_post_id` (`post_id`),
  KEY `idx_hr_employee_leader_id` (`leader_id`),
  KEY `idx_hr_employee_employment_status` (`employment_status`),
  KEY `idx_hr_employee_hire_date` (`hire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工主档表';
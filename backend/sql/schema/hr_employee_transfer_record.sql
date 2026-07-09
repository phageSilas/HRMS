-- ========================================
-- HRMS 业务模块表 - hr_employee_transfer_record
-- 模块归属: hrms-business
-- 跨模块契约字段:
--   - employee_id -> hr_employee.id
--   - from_dept_id, to_dept_id -> sys_dept.id
--   - from_post_id, to_post_id -> sys_post.id
--   - from_leader_id, to_leader_id -> hr_employee.id
-- 创建时间: 2026-07-09
-- ========================================

CREATE TABLE `hr_employee_transfer_record` (
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
  `salary_before` DECIMAL(12,2) DEFAULT NULL COMMENT '调整前薪资',
  `salary_after` DECIMAL(12,2) DEFAULT NULL COMMENT '调整后薪资',
  `effective_date` DATE NOT NULL COMMENT '生效日期',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '调岗原因',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_hr_employee_transfer_record_employee_id` (`employee_id`),
  KEY `idx_hr_employee_transfer_record_effective_date` (`effective_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工调岗记录表';
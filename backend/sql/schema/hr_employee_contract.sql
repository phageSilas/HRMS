-- ========================================
-- HRMS 业务模块表 - hr_employee_contract
-- 模块归属: hrms-business
-- 跨模块契约字段: employee_id -> hr_employee.id
-- 创建时间: 2026-07-09
-- ========================================

CREATE TABLE `hr_employee_contract` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `contract_no` VARCHAR(64) DEFAULT NULL COMMENT '合同编号',
  `contract_type` VARCHAR(32) NOT NULL COMMENT '合同类型',
  `start_date` DATE DEFAULT NULL COMMENT '合同开始日期',
  `end_date` DATE DEFAULT NULL COMMENT '合同结束日期',
  `probation_month` INT DEFAULT NULL COMMENT '试用期（月）',
  `probation_salary_ratio` DECIMAL(5,2) DEFAULT NULL COMMENT '试用期薪资比例',
  `attachment_file_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '附件文件ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_hr_employee_contract_employee_id` (`employee_id`),
  KEY `idx_hr_employee_contract_end_date` (`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工合同表';
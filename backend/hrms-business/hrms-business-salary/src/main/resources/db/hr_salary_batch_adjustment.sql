CREATE TABLE IF NOT EXISTS `hr_salary_batch_adjustment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` BIGINT UNSIGNED NOT NULL COMMENT '薪资批次ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `item_code` VARCHAR(64) NOT NULL COMMENT '薪资项目编码',
  `adjust_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '调整金额，正数增加、负数减少',
  `reason` VARCHAR(500) NOT NULL COMMENT '人工调整原因',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_salary_adjust_batch_employee` (`batch_id`, `employee_id`),
  KEY `idx_salary_adjust_batch_item` (`batch_id`, `employee_id`, `item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资批次人工调整表';

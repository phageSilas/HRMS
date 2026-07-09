-- ========================================
-- HRMS 系统模块表 - sys_post
-- 模块归属: hrms-system
-- 跨模块契约字段: id (被 hr_employee.post_id 引用)
-- 创建时间: 2026-07-09
-- ========================================

CREATE TABLE `sys_post` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_name` VARCHAR(64) NOT NULL COMMENT '职位名称',
  `post_code` VARCHAR(64) NOT NULL COMMENT '职位编码',
  `sequence_code` VARCHAR(16) NOT NULL COMMENT '职位序列：M/P/S',
  `dept_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '所属部门ID，为空表示通用职位',
  `job_level_min` VARCHAR(16) DEFAULT NULL COMMENT '职级下限',
  `job_level_max` VARCHAR(16) DEFAULT NULL COMMENT '职级上限',
  `default_probation_month` INT NOT NULL DEFAULT 3 COMMENT '默认试用期（月）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '职位描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
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
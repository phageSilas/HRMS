-- ========================================
-- HRMS 系统模块表 - sys_dept
-- 模块归属: hrms-system
-- 跨模块契约字段: id (被 hr_employee.dept_id 引用)
-- 创建时间: 2026-07-09
-- ========================================

CREATE TABLE `sys_dept` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '上级部门ID',
  `dept_name` VARCHAR(64) NOT NULL COMMENT '部门名称',
  `dept_code` VARCHAR(16) NOT NULL COMMENT '部门编码',
  `leader_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '部门负责人用户ID',
  `leader_employee_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '部门负责人员工ID',
  `ancestors` VARCHAR(500) DEFAULT NULL COMMENT '祖级路径',
  `dept_level` INT NOT NULL DEFAULT 1 COMMENT '部门层级',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '部门描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `employee_count` INT NOT NULL DEFAULT 0 COMMENT '在职员工数缓存',
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
-- ========================================
-- HRMS 业务模块表 - hr_approval_instance
-- 模块归属: hrms-business
-- 跨模块契约字段:
--   - applicant_user_id -> sys_user.id
--   - applicant_employee_id -> hr_employee.id
-- 创建时间: 2026-07-09
-- ========================================

CREATE TABLE `hr_approval_instance` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `approval_no` VARCHAR(64) NOT NULL COMMENT '审批单号',
  `approval_type` VARCHAR(32) NOT NULL COMMENT '审批类型：ENTRY/REGULAR/TRANSFER/LEAVE',
  `biz_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '审批标题',
  `applicant_user_id` BIGINT UNSIGNED NOT NULL COMMENT '申请人用户ID',
  `applicant_employee_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '申请人员工ID',
  `current_node_name` VARCHAR(64) DEFAULT NULL COMMENT '当前节点名称',
  `approval_status` VARCHAR(32) NOT NULL COMMENT '审批状态',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `form_json` JSON DEFAULT NULL COMMENT '表单快照',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_approval_instance_approval_no` (`approval_no`),
  KEY `idx_hr_approval_instance_type` (`approval_type`),
  KEY `idx_hr_approval_instance_status` (`approval_status`),
  KEY `idx_hr_approval_instance_biz_id` (`biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批实例表';
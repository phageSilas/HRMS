-- ========================================
-- HRMS 业务模块表 - hr_approval_task
-- 模块归属: hrms-business
-- 跨模块契约字段:
--   - instance_id -> hr_approval_instance.id
--   - approver_user_id -> sys_user.id
--   - approver_employee_id -> hr_employee.id
-- 创建时间: 2026-07-09
-- ========================================

CREATE TABLE `hr_approval_task` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `instance_id` BIGINT UNSIGNED NOT NULL COMMENT '审批实例ID',
  `node_code` VARCHAR(64) NOT NULL COMMENT '节点编码',
  `node_name` VARCHAR(64) NOT NULL COMMENT '节点名称',
  `approver_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批人用户ID',
  `approver_employee_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批人员工ID',
  `task_status` VARCHAR(32) NOT NULL COMMENT '任务状态',
  `approve_result` VARCHAR(32) DEFAULT NULL COMMENT '审批结果',
  `approve_comment` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `receive_time` DATETIME DEFAULT NULL COMMENT '接收时间',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `deadline_time` DATETIME DEFAULT NULL COMMENT '截止时间',
  `sort_no` INT NOT NULL DEFAULT 1 COMMENT '节点顺序',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_hr_approval_task_instance_id` (`instance_id`),
  KEY `idx_hr_approval_task_approver_user_id` (`approver_user_id`),
  KEY `idx_hr_approval_task_task_status` (`task_status`),
  KEY `idx_hr_approval_task_deadline_time` (`deadline_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批任务表';
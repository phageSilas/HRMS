-- ========================================
-- HRMS 入转调离缺失表 DDL
-- 模块: M2 入转调离
-- 说明: 转正申请表、离职申请表
-- ========================================

-- ----------------------------------------
-- hr_regular_application（转正申请表）
-- 用于员工试用期转正评估流程
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
-- 用于员工离职流程管理
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
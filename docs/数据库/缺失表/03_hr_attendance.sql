-- ========================================
-- HRMS 考勤管理缺失表 DDL
-- 模块: M3 考勤管理
-- 说明: 请假申请表、补卡申请表
-- ========================================

-- ----------------------------------------
-- hr_leave_request（请假申请表）
-- 用于员工请假申请及审批流程
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
-- 用于员工缺卡补卡申请
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
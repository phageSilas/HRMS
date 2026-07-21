-- 考勤建表
USE `hrms`;

-- hr_attendance_group（考勤组表）
CREATE TABLE `hr_attendance_group` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_name` VARCHAR(64) NOT NULL COMMENT '考勤组名称',
  `shift_type` VARCHAR(32) NOT NULL COMMENT '班次类型：FIXED/FLEXIBLE/SCHEDULED',
  `work_start_time` TIME NOT NULL COMMENT '上班时间',
  `work_end_time` TIME NOT NULL COMMENT '下班时间',
  `rest_start_time` TIME DEFAULT NULL COMMENT '午休开始时间',
  `rest_end_time` TIME DEFAULT NULL COMMENT '午休结束',
  `flexible_start_time` TIME DEFAULT NULL COMMENT '弹性最早打卡时间',
  `flexible_end_time` TIME DEFAULT NULL COMMENT '弹性最晚打卡时间',
  `late_threshold_minutes` INT NOT NULL DEFAULT 15 COMMENT '迟到阈值（分钟）',
  `early_leave_threshold_minutes` INT NOT NULL DEFAULT 15 COMMENT '早退阈值（分钟）',
  `clock_ip_whitelist` VARCHAR(500) DEFAULT NULL COMMENT 'IP白名单，逗号分隔',
  `clock_gps_scope` VARCHAR(500) DEFAULT NULL COMMENT 'GPS范围配置（中心点+半径）',
  `monthly_correction_limit` INT NOT NULL DEFAULT 2 COMMENT '每月补卡次数上限',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_hr_att_group_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤组表';

-- hr_attendance_record（考勤记录表）
CREATE TABLE `hr_attendance_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `group_id` BIGINT UNSIGNED NOT NULL COMMENT '考勤组ID',
  `record_date` DATE NOT NULL COMMENT '打卡日期',
  `clock_in_time` DATETIME DEFAULT NULL COMMENT '上班打卡时间',
  `clock_out_time` DATETIME DEFAULT NULL COMMENT '下班打卡时间',
  `clock_in_status` VARCHAR(32) DEFAULT NULL COMMENT '上班状态：NORMAL/LATE/MISSING/ABSENCE',
  `clock_out_status` VARCHAR(32) DEFAULT NULL COMMENT '下班状态：NORMAL/EARLY_LEAVE/MISSING/ABSENCE',
  `clock_in_ip` VARCHAR(64) DEFAULT NULL COMMENT '上班打卡IP',
  `clock_out_ip` VARCHAR(64) DEFAULT NULL COMMENT '下班打卡IP',
  `clock_in_gps` VARCHAR(128) DEFAULT NULL COMMENT '上班打卡GPS',
  `clock_out_gps` VARCHAR(128) DEFAULT NULL COMMENT '下班打卡GPS',
  `device_info` VARCHAR(255) DEFAULT NULL COMMENT '设备信息',
  `correction_status` VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT '补卡状态：NONE/PENDING/APPROVED',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_att_rec_emp_date` (`employee_id`, `record_date`),
  KEY `idx_hr_att_rec_group_date` (`group_id`, `record_date`),
  KEY `idx_hr_att_rec_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='打卡记录表';

-- hr_leave_request（请假申请表）
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

-- hr_attendance_correction（补卡申请表）
CREATE TABLE `hr_attendance_correction` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `record_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '打卡记录ID（补卡时可先无打卡记录）',
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

-- hr_leave_balance（假期余额表）
CREATE TABLE `hr_leave_balance` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `employee_id` bigint NOT NULL COMMENT '员工ID',
                                    `leave_type` varchar(32) NOT NULL COMMENT '假期类型：ANNUAL-年假 SICK-病假 PERSONAL-事假 COMPASSIONATE-调休 MARRIAGE-婚假 MATERNITY-产假 FUNERAL-丧假',
                                    `balance_year` int NOT NULL COMMENT '余额年度',
                                    `total_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '本年度应得天数',
                                    `used_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '已使用天数',
                                    `frozen_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '审批冻结天数',
                                    `remaining_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '剩余可用天数',
                                    `carryover_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '上年结转天数',
                                    `adjust_days` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '人工调整天数，可正可负',
                                    `expire_date` date DEFAULT NULL COMMENT '余额过期日期',
                                    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-停用 1-启用',
                                    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                    `create_by` bigint DEFAULT NULL COMMENT '创建人',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_by` bigint DEFAULT NULL COMMENT '更新人',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
                                    `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_employee_leave_year` (`employee_id`, `leave_type`, `balance_year`, `is_deleted`),
                                    KEY `idx_employee_year` (`employee_id`, `balance_year`),
                                    KEY `idx_leave_type_year` (`leave_type`, `balance_year`),
                                    KEY `idx_expire_date` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工假期余额表';

-- hr_attendance_group_member（考勤组成员关系表）
CREATE TABLE `hr_attendance_group_member` (
                                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                              `group_id` bigint NOT NULL COMMENT '考勤组ID',
                                              `employee_id` bigint NOT NULL COMMENT '员工ID',
                                              `effective_start_date` date NOT NULL COMMENT '生效开始日期',
                                              `effective_end_date` date DEFAULT NULL COMMENT '生效结束日期，空表示长期有效',
                                              `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-停用 1-启用',
                                              `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                              `create_by` bigint DEFAULT NULL COMMENT '创建人',
                                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              `update_by` bigint DEFAULT NULL COMMENT '更新人',
                                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
                                              `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
                                              PRIMARY KEY (`id`),
                                              UNIQUE KEY `uk_employee_group_effective` (`employee_id`, `group_id`, `effective_start_date`, `is_deleted`),
                                              KEY `idx_employee_effective` (`employee_id`, `status`, `effective_start_date`, `effective_end_date`),
                                              KEY `idx_group_id` (`group_id`),
                                              KEY `idx_effective_date` (`effective_start_date`, `effective_end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤组成员关系表';

-- hr_attendance_overtime（加班申请表）
CREATE TABLE `hr_attendance_overtime` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `overtime_date` DATETIME NOT NULL COMMENT '加班日期',
  `duration` DECIMAL(5,1) NOT NULL COMMENT '加班时长（小时）',
  `reason` VARCHAR(500) NOT NULL COMMENT '加班事由',
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
  KEY `idx_hr_att_overtime_employee` (`employee_id`),
  KEY `idx_hr_att_overtime_date` (`overtime_date`),
  KEY `idx_hr_att_overtime_status` (`approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='加班申请表';

ALTER TABLE `hr_attendance_correction`
    MODIFY COLUMN `record_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '打卡记录ID（补卡时可先无打卡记录）';



ALTER TABLE hr_attendance_group
    ADD COLUMN scope_type varchar(32) NULL COMMENT '适用范围类型：DEPT/POST/EMPLOYEE' AFTER monthly_correction_limit,
    ADD COLUMN scope_value varchar(1000) NULL COMMENT '适用范围值，JSON字符串' AFTER scope_type;


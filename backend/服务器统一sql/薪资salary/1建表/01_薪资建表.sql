-- 薪资建表
USE `hrms`;

-- hr_salary_template（薪资账套表）
CREATE TABLE `hr_salary_template` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` VARCHAR(64) NOT NULL COMMENT '账套名称',
  `template_code` VARCHAR(64) NOT NULL COMMENT '账套编码',
  `scope_type` VARCHAR(32) NOT NULL DEFAULT 'ALL' COMMENT '适用范围：ALL-全部 DEPT-指定部门',
  `scope_value` VARCHAR(500) DEFAULT NULL COMMENT '适用范围值（部门ID列表等）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_sal_tpl_code` (`template_code`),
  KEY `idx_hr_sal_tpl_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资账套表';

-- hr_salary_template_item（薪资账套项目表）
CREATE TABLE `hr_salary_template_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` BIGINT UNSIGNED NOT NULL COMMENT '账套ID',
  `item_code` VARCHAR(32) NOT NULL COMMENT '工资项目编码',
  `item_name` VARCHAR(64) NOT NULL COMMENT '工资项目名称',
  `category` VARCHAR(32) NOT NULL COMMENT '分类：INCOME-收入 DEDUCTION-扣除',
  `calc_rule` VARCHAR(500) DEFAULT NULL COMMENT '计算规则',
  `default_value` DECIMAL(12,2) DEFAULT NULL COMMENT '默认值',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_hr_sal_tpl_item_tpl` (`template_id`),
  KEY `idx_hr_sal_tpl_item_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资账套项目表';

-- hr_employee_salary_profile（员工薪资档案表）
CREATE TABLE `hr_employee_salary_profile` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `template_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '适用账套ID',
  `base_salary` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '基本工资',
  `allowance` DECIMAL(12,2) DEFAULT 0 COMMENT '岗位津贴',
  `performance_base` DECIMAL(12,2) DEFAULT 0 COMMENT '绩效基数',
  `social_insurance_base` DECIMAL(12,2) DEFAULT 0 COMMENT '社保基数',
  `housing_fund_base` DECIMAL(12,2) DEFAULT 0 COMMENT '公积金基数',
  `bank_name` VARCHAR(64) DEFAULT NULL COMMENT '开户银行',
  `bank_account` VARCHAR(255) DEFAULT NULL COMMENT '银行卡号（加密存储）',
  `effective_date` DATE DEFAULT NULL COMMENT '生效日期',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_emp_sal_prof_employee` (`employee_id`),
  KEY `idx_hr_emp_sal_prof_tpl` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工薪资档案表';

-- hr_salary_batch（薪资批次表）
CREATE TABLE `hr_salary_batch` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` VARCHAR(64) NOT NULL COMMENT '薪资批次编号',
  `salary_month` CHAR(7) NOT NULL COMMENT '薪资月份 yyyy-MM',
  `scope_type` VARCHAR(32) NOT NULL DEFAULT 'ALL' COMMENT '核算范围：ALL/DEPT/EMPLOYEE',
  `scope_value` VARCHAR(500) DEFAULT NULL COMMENT '核算范围值',
  `batch_status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '批次状态',
  `approval_instance_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审批实例ID',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '核算员工总数',
  `total_gross_salary` DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '应发总额',
  `total_net_salary` DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '实发总额',
  `yellow_warning_count` INT NOT NULL DEFAULT 0 COMMENT '黄色预警数',
  `red_warning_count` INT NOT NULL DEFAULT 0 COMMENT '红色预警数',
  `block_count` INT NOT NULL DEFAULT 0 COMMENT '阻断异常',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_salary_batch_no` (`batch_no`),
  KEY `idx_hr_salary_batch_status` (`batch_status`),
  KEY `idx_hr_salary_batch_month` (`salary_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资批次表';

-- hr_salary_batch_item（薪资批次明细表）
CREATE TABLE `hr_salary_batch_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` BIGINT UNSIGNED NOT NULL COMMENT '批次ID',
  `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
  `base_salary` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '基本工资',
  `allowance` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '岗位津贴',
  `performance_bonus` DECIMAL(12,2) DEFAULT 0 COMMENT '绩效奖金',
  `overtime_pay` DECIMAL(12,2) DEFAULT 0 COMMENT '加班费',
  `late_deduction` DECIMAL(10,2) DEFAULT 0 COMMENT '迟到扣款',
  `leave_deduction` DECIMAL(10,2) DEFAULT 0 COMMENT '请假扣款',
  `social_insurance` DECIMAL(10,2) DEFAULT 0 COMMENT '社保（个人部分）',
  `housing_fund` DECIMAL(10,2) DEFAULT 0 COMMENT '公积金（个人部分）',
  `income_tax` DECIMAL(10,2) DEFAULT 0 COMMENT '个人所得税',
  `gross_salary` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '应发合计',
  `deduction_total` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '应扣合计',
  `net_salary` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '实发工资',
  `warning_level` VARCHAR(32) DEFAULT NULL COMMENT '预警级别：NONE/YELLOW/RED/BLOCK',
  `warning_reason` VARCHAR(500) DEFAULT NULL COMMENT '预警原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_hr_sal_item_batch` (`batch_id`),
  KEY `idx_hr_sal_item_employee` (`employee_id`),
  KEY `idx_hr_sal_item_warning` (`warning_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资明细表';

-- hr_salary_batch_adjustment（薪资批次调整表）
CREATE TABLE IF NOT EXISTS `hr_salary_batch_adjustment` (
                                                            `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                            `batch_id` BIGINT UNSIGNED NOT NULL COMMENT '薪资批次ID',
                                                            `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
                                                            `item_code` VARCHAR(64) NOT NULL COMMENT '工资项目编码',
                                                            `adjust_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '调整金额，正数加负数减',
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

-- hr_salary_payslip_view_record（工资条查看记录表）
CREATE TABLE IF NOT EXISTS `hr_salary_payslip_view_record` (
                                                               `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                               `payslip_item_id` BIGINT UNSIGNED NOT NULL COMMENT '工资条明细ID，应hr_salary_batch_item.id',
                                                               `batch_id` BIGINT UNSIGNED NOT NULL COMMENT '薪资批次ID',
                                                               `employee_id` BIGINT UNSIGNED NOT NULL COMMENT '员工ID',
                                                               `salary_month` CHAR(7) NOT NULL COMMENT '薪资月份 yyyy-MM',
                                                               `first_view_time` DATETIME NOT NULL COMMENT '首次查看时间',
                                                               `last_view_time` DATETIME NOT NULL COMMENT '最近查看时间',
                                                               `view_count` INT NOT NULL DEFAULT 1 COMMENT '查看次数',
                                                               `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
                                                               `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                               `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
                                                               `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                               `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                                               `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
                                                               `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
                                                               PRIMARY KEY (`id`),
                                                               UNIQUE KEY `uk_salary_payslip_view_item` (`payslip_item_id`),
                                                               KEY `idx_salary_payslip_view_employee_month` (`employee_id`, `salary_month`),
                                                               KEY `idx_salary_payslip_view_batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工资条查看记录表';


-- 为薪资模板补充账套生效日期字段（兼容重复执行）
SET @salary_template_effective_date_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'hr_salary_template'
      AND COLUMN_NAME = 'effective_date'
);

SET @salary_template_effective_date_sql := IF(
    @salary_template_effective_date_exists = 0,
    'ALTER TABLE `hr_salary_template` ADD COLUMN `effective_date` DATE DEFAULT NULL COMMENT ''账套生效日期'' AFTER `scope_value`',
    'SELECT 1'
);

PREPARE stmt FROM @salary_template_effective_date_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

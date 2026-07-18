-- 审批建表
USE `hrms`;

-- hr_approval_instance（审批实例表）
CREATE TABLE `hr_approval_instance` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `approval_no` VARCHAR(64) NOT NULL COMMENT '审批单号',
  `approval_type` VARCHAR(32) NOT NULL COMMENT '审批类型编码',
  `biz_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '审批标题',
  `applicant_user_id` BIGINT UNSIGNED NOT NULL COMMENT '申请人用户ID',
  `applicant_employee_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '申请人员工ID',
  `current_node_name` VARCHAR(64) DEFAULT NULL COMMENT '当前节点名称',
  `approval_status` TINYINT NOT NULL COMMENT '状态：0-草稿 1-审批中 2-已通过 3-已驳回 4-已撤回',
  `form_json` JSON DEFAULT NULL COMMENT '表单快照（审批时的业务数据副本）',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_apr_inst_no` (`approval_no`),
  KEY `idx_hr_apr_inst_type` (`approval_type`),
  KEY `idx_hr_apr_inst_status` (`approval_status`),
  KEY `idx_hr_apr_inst_applicant` (`applicant_user_id`),
  KEY `idx_hr_apr_inst_biz` (`approval_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批实例表';

-- hr_approval_task（审批任务表）
CREATE TABLE `hr_approval_task` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `instance_id` BIGINT UNSIGNED NOT NULL COMMENT '审批实例ID',
  `node_code` VARCHAR(64) NOT NULL COMMENT '节点编码',
  `node_name` VARCHAR(64) NOT NULL COMMENT '节点名称',
  `approver_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '实际审批人用户ID',
  `original_approver_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '原审批人ID（委托场景）',
  `delegate_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否代审：0-否 1-是',
  `task_status` TINYINT NOT NULL COMMENT '任务状态：0-待处理 1-已处理 2-已转交',
  `approve_result` TINYINT DEFAULT NULL COMMENT '审批结果：1-通过 2-驳回 3-转交',
  `approve_comment` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `receive_time` DATETIME DEFAULT NULL COMMENT '接收时间',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `deadline_time` DATETIME DEFAULT NULL COMMENT '截止时间（超时升级依据）',
  `sort_no` INT NOT NULL DEFAULT 1 COMMENT '节点顺序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_hr_apr_task_instance` (`instance_id`),
  KEY `idx_hr_apr_task_approver` (`approver_user_id`, `task_status`),
  KEY `idx_hr_apr_task_deadline` (`deadline_time`, `task_status`),
  KEY `idx_hr_apr_task_sort` (`instance_id`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批任务表';

-- hr_approval_delegation（审批委托表）
CREATE TABLE `hr_approval_delegation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `delegator_id` BIGINT UNSIGNED NOT NULL COMMENT '委托人用户ID',
  `delegator_name` VARCHAR(32) NOT NULL COMMENT '委托人',
  `delegate_to_id` BIGINT UNSIGNED NOT NULL COMMENT '被委托人用户ID',
  `delegate_to_name` VARCHAR(32) NOT NULL COMMENT '被委托人姓名',
  `start_date` DATETIME NOT NULL COMMENT '委托生效时间',
  `end_date` DATETIME NOT NULL COMMENT '委托结束时间',
  `reason` VARCHAR(256) DEFAULT NULL COMMENT '委托原因',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已取消 1-生效中 2-已过期',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_hr_apr_del_delg` (`delegator_id`),
  KEY `idx_hr_apr_del_delt` (`delegate_to_id`),
  KEY `idx_hr_apr_del_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批委托表';


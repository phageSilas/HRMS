-- ========================================
-- HRMS 系统模块表 - sys_operate_log
-- 模块归属: hrms-system
-- 创建时间: 2026-07-09
-- ========================================

CREATE TABLE `sys_operate_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module_name` VARCHAR(64) NOT NULL COMMENT '模块名称',
  `biz_type` VARCHAR(64) DEFAULT NULL COMMENT '业务类型',
  `biz_id` VARCHAR(64) DEFAULT NULL COMMENT '业务ID',
  `operate_type` VARCHAR(32) NOT NULL COMMENT '操作类型',
  `request_uri` VARCHAR(255) DEFAULT NULL COMMENT '请求URI',
  `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
  `operator_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人用户ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
  `request_ip` VARCHAR(64) DEFAULT NULL COMMENT '请求IP',
  `request_param` TEXT DEFAULT NULL COMMENT '请求参数',
  `response_data` TEXT DEFAULT NULL COMMENT '响应数据',
  `result_code` VARCHAR(32) DEFAULT NULL COMMENT '结果码',
  `result_message` VARCHAR(255) DEFAULT NULL COMMENT '结果信息',
  `success_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否成功',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_sys_operate_log_module` (`module_name`),
  KEY `idx_sys_operate_log_operator` (`operator_user_id`),
  KEY `idx_sys_operate_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';
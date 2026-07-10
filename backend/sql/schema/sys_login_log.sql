-- ========================================
-- HRMS 系统模块表 - sys_login_log
-- 模块归属: hrms-system
-- 创建时间: 2026-07-09
-- 更新时间: 2026-07-10 - 修正字段名以匹配LoginLogDO实体类
-- ========================================

CREATE TABLE `sys_login_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID',
  `ip_address` VARCHAR(64) DEFAULT NULL COMMENT '登录IP地址',
  `location` VARCHAR(128) DEFAULT NULL COMMENT '登录地点',
  `device_info` VARCHAR(255) DEFAULT NULL COMMENT '设备信息(浏览器+操作系统)',
  `browser` VARCHAR(128) DEFAULT NULL COMMENT '浏览器(保留用于统计)',
  `os` VARCHAR(128) DEFAULT NULL COMMENT '操作系统(保留用于统计)',
  `success` TINYINT NOT NULL DEFAULT 1 COMMENT '登录结果(0失败1成功)',
  `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_sys_login_log_username` (`username`),
  KEY `idx_sys_login_log_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

-- ========================================
-- 字段修改说明
-- ========================================
-- 2026-07-10 修复:
-- login_ip → ip_address (匹配实体类字段)
-- login_status → success (匹配实体类字段)
-- message → fail_reason (匹配实体类字段)
-- login_location → location (匹配实体类字段)
-- 新增 device_info 字段 (合并browser和os)
--
-- 保留 browser 和 os 字段用于统计分析
-- ========================================
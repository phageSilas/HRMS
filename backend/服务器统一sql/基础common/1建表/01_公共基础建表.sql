-- 公共基础建表
USE `hrms`;

-- sys_file（文件表）
CREATE TABLE `sys_file` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原文件',
  `file_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `file_type` VARCHAR(64) DEFAULT NULL COMMENT '文件类型',
  `mime_type` VARCHAR(128) DEFAULT NULL COMMENT 'MIME类型',
  `md5` VARCHAR(64) DEFAULT NULL COMMENT '文件MD5',
  `business_type` VARCHAR(32) DEFAULT NULL COMMENT '业务类型',
  `business_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务ID',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '上传人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_file_type` (`business_type`),
  KEY `idx_sys_file_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件表';

-- sys_operate_log（操作日志表）
CREATE TABLE `sys_operate_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
  `username` VARCHAR(64) DEFAULT NULL COMMENT '操作用户',
  `operate_type` VARCHAR(32) NOT NULL COMMENT '操作类型',
  `operate_module` VARCHAR(64) DEFAULT NULL COMMENT '操作模块',
  `operate_desc` VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
  `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求地址',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
  `response_result` TEXT DEFAULT NULL COMMENT '响应结果',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `execute_time` INT DEFAULT NULL COMMENT '执行时长（毫秒）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '执行状态：1-成功 0-失败',
  `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_oper_log_user` (`user_id`),
  KEY `idx_sys_oper_log_type` (`operate_type`),
  KEY `idx_sys_oper_log_module` (`operate_module`),
  KEY `idx_sys_oper_log_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

-- sys_login_log（登录日志表）
CREATE TABLE `sys_login_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '登录用户ID',
  `username` VARCHAR(64) DEFAULT NULL COMMENT '登录用户',
  `login_type` VARCHAR(32) DEFAULT NULL COMMENT '登录类型：ACCOUNT-账号登录 TOKEN-令牌登录',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
  `login_location` VARCHAR(255) DEFAULT NULL COMMENT '登录地点',
  `browser` VARCHAR(64) DEFAULT NULL COMMENT '浏览器',
  `os` VARCHAR(64) DEFAULT NULL COMMENT '操作系统',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '登录状态：1-成功 0-失败',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_login_log_user` (`user_id`),
  KEY `idx_sys_login_log_status` (`status`),
  KEY `idx_sys_login_log_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';


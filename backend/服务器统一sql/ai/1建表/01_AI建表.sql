-- AI建表
USE `hrms`;

-- hr_ai_conversation（AI会话表）
CREATE TABLE `hr_ai_conversation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID（游客为NULL）',
  `session_token` VARCHAR(64) DEFAULT NULL COMMENT '游客会话标识',
  `title` VARCHAR(200) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-活跃 2-已归档',
  `message_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息总数',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后消息时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_hr_ai_conv_user` (`user_id`),
  KEY `idx_hr_ai_conv_update` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI会话记录表';

-- hr_ai_message（AI消息表）
CREATE TABLE `hr_ai_message` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：user/assistant',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `metadata` JSON DEFAULT NULL COMMENT '元数据（意图、引用来源等）',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI消息记录表';

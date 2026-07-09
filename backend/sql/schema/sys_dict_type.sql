-- ========================================
-- HRMS 系统模块表 - sys_dict_type
-- 模块归属: hrms-system
-- 创建时间: 2026-07-09
-- ========================================

CREATE TABLE `sys_dict_type` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_name` VARCHAR(64) NOT NULL COMMENT '字典名称',
  `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_type_type` (`dict_type`),
  KEY `idx_sys_dict_type_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';
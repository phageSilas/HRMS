-- ========================================
-- HRMS 系统模块表 - sys_dict_data
-- 模块归属: hrms-system
-- 创建时间: 2026-07-09
-- ========================================

CREATE TABLE `sys_dict_data` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型',
  `label` VARCHAR(64) NOT NULL COMMENT '字典标签',
  `value` VARCHAR(64) NOT NULL COMMENT '字典值',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `tag_type` VARCHAR(32) DEFAULT NULL COMMENT '标签样式',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_data_type_value` (`dict_type`, `value`),
  KEY `idx_sys_dict_data_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';
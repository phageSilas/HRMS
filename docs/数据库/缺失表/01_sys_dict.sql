-- ========================================
-- HRMS 字典表 DDL
-- 模块: M6 组织架构
-- 说明: 字典类型和字典数据表，用于系统枚举值管理
-- ========================================

-- ----------------------------------------
-- sys_dict_type（字典类型表）
-- 用于管理字典分类，如请假类型、证件类型、学历等
-- ----------------------------------------
CREATE TABLE `sys_dict_type` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_name` VARCHAR(64) NOT NULL COMMENT '字典名称',
  `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型编码',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_type_type` (`dict_type`),
  KEY `idx_sys_dict_type_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- ----------------------------------------
-- sys_dict_data（字典数据表）
-- 用于管理字典项，如年假、事假、病假等
-- ----------------------------------------
CREATE TABLE `sys_dict_data` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型编码',
  `dict_label` VARCHAR(64) NOT NULL COMMENT '字典标签',
  `dict_value` VARCHAR(64) NOT NULL COMMENT '字典值',
  `css_class` VARCHAR(64) DEFAULT NULL COMMENT '样式属性',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_dict_data_type` (`dict_type`),
  KEY `idx_sys_dict_data_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';
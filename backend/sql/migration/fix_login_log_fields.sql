-- ========================================
-- HRMS 紧急修复脚本 - 登录日志表字段映射
-- ========================================
-- 创建时间: 2026-07-10
-- 优先级: P0 (紧急)
-- 影响范围: 登录功能
--
-- 问题说明:
-- 实体类LoginLogDO的字段名与数据库表sys_login_log的字段名不匹配
-- 导致登录时插入日志失败,登录功能完全不可用
--
-- 修复方案:
-- 调整数据库字段名以匹配Java实体类命名规范
-- ========================================

USE hrms;

-- ========================================
-- 1. 备份当前表结构
-- ========================================
-- 建议先备份表结构和数据
-- CREATE TABLE sys_login_log_backup AS SELECT * FROM sys_login_log;

-- ========================================
-- 2. 修改字段名以匹配实体类
-- ========================================
-- 修改 login_status -> success
ALTER TABLE sys_login_log
CHANGE COLUMN login_status success TINYINT NOT NULL DEFAULT 1 COMMENT '登录结果(0失败1成功)';

-- 修改 login_ip -> ip_address
ALTER TABLE sys_login_log
CHANGE COLUMN login_ip ip_address VARCHAR(64) NULL COMMENT '登录IP地址';

-- 修改 message -> fail_reason
ALTER TABLE sys_login_log
CHANGE COLUMN message fail_reason VARCHAR(255) NULL COMMENT '失败原因';

-- ========================================
-- 3. 验证修改结果
-- ========================================
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'hrms'
  AND TABLE_NAME = 'sys_login_log'
ORDER BY ORDINAL_POSITION;

-- ========================================
-- 4. 测试插入数据
-- ========================================
-- 插入测试数据验证字段映射
INSERT INTO sys_login_log (
    user_id,
    username,
    success,
    ip_address,
    device_info,
    login_time,
    fail_reason
) VALUES (
    1,
    'admin',
    1,
    '127.0.0.1',
    'Test Browser',
    NOW(),
    NULL
);

-- 查询验证
SELECT
    id,
    username,
    success,
    ip_address,
    device_info,
    login_time,
    fail_reason,
    create_time
FROM sys_login_log
ORDER BY id DESC
LIMIT 1;

-- 删除测试数据
DELETE FROM sys_login_log WHERE username = 'admin' AND device_info = 'Test Browser';

-- ========================================
-- 修复完成
-- ========================================
-- 执行此脚本后,请重启应用并测试登录功能
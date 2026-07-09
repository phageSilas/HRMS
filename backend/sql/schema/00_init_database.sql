-- ========================================
-- HRMS 数据库初始化脚本
-- 创建时间: 2026-07-09
-- 说明: 本脚本包含HRMS系统所有表的创建语句
-- 执行顺序: 按文件名顺序执行
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `hrms`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `hrms`;

-- ========================================
-- 系统模块表 (sys_*)
-- ========================================

-- 系统用户表
source sys_user.sql;

-- 角色表
source sys_role.sql;

-- 用户角色关联表
source sys_user_role.sql;

-- 菜单表
source sys_menu.sql;

-- 角色菜单关联表
source sys_role_menu.sql;

-- 部门表
source sys_dept.sql;

-- 职位表
source sys_post.sql;

-- 字典类型表
source sys_dict_type.sql;

-- 字典数据表
source sys_dict_data.sql;

-- 文件表
source sys_file.sql;

-- 操作日志表
source sys_operate_log.sql;

-- 登录日志表
source sys_login_log.sql;

-- ========================================
-- 业务模块表 (hr_*)
-- ========================================

-- 员工主档表
source hr_employee.sql;

-- 员工合同表
source hr_employee_contract.sql;

-- 员工调岗记录表
source hr_employee_transfer_record.sql;

-- 审批实例表
source hr_approval_instance.sql;

-- 审批任务表
source hr_approval_task.sql;
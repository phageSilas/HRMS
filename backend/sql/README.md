# HRMS 数据库初始化说明

## 数据库创建步骤

### 1. 创建数据库

```sql
-- 方式1: 直接执行初始化脚本
mysql -uroot -p < backend/sql/schema/00_init_database.sql

-- 方式2: 手动创建
CREATE DATABASE hrms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

### 2. 执行表结构脚本

```bash
# 进入数据库
mysql -uroot -p hrms

# 执行表结构脚本(按顺序)
source backend/sql/schema/sys_user.sql;
source backend/sql/schema/sys_role.sql;
source backend/sql/schema/sys_user_role.sql;
source backend/sql/schema/sys_menu.sql;
source backend/sql/schema/sys_role_menu.sql;
source backend/sql/schema/sys_dept.sql;
source backend/sql/schema/sys_post.sql;
source backend/sql/schema/sys_dict_type.sql;
source backend/sql/schema/sys_dict_data.sql;
source backend/sql/schema/sys_file.sql;
source backend/sql/schema/sys_operate_log.sql;
source backend/sql/schema/sys_login_log.sql;
source backend/sql/schema/hr_employee.sql;
source backend/sql/schema/hr_employee_contract.sql;
source backend/sql/schema/hr_employee_transfer_record.sql;
source backend/sql/schema/hr_approval_instance.sql;
source backend/sql/schema/hr_approval_task.sql;
```

### 3. 执行初始数据脚本

```bash
# 插入初始管理员和字典数据
source backend/sql/data/init_admin.sql;
source backend/sql/data/init_dict.sql;
```

---

## 默认登录凭证

- **用户名**: admin
- **密码**: password
- **角色**: 系统管理员

---

## 表结构说明

### 系统模块表 (sys_*)

| 表名 | 说明 | 模块 |
|------|------|------|
| sys_user | 系统用户表 | auth |
| sys_role | 角色表 | auth |
| sys_user_role | 用户角色关联表 | auth |
| sys_menu | 菜单表 | auth |
| sys_role_menu | 角色菜单关联表 | auth |
| sys_dept | 部门表 | organization |
| sys_post | 职位表 | organization |
| sys_dict_type | 字典类型表 | organization |
| sys_dict_data | 字典数据表 | organization |
| sys_file | 文件表 | file |
| sys_operate_log | 操作日志表 | log |
| sys_login_log | 登录日志表 | log |

### 业务模块表 (hr_*)

| 表名 | 说明 | 模块 |
|------|------|------|
| hr_employee | 员工主档表 | employee |
| hr_employee_contract | 员工合同表 | employee |
| hr_employee_transfer_record | 员工调岗记录表 | employee |
| hr_approval_instance | 审批实例表 | approval |
| hr_approval_task | 审批任务表 | approval |

---

## 重要更新记录

### 2026-07-10 字段映射修正

**sys_login_log 表字段修正**:

| 原字段名 | 新字段名 | 原因 |
|---------|---------|------|
| login_ip | ip_address | 匹配LoginLogDO实体类 |
| login_status | success | 匹配LoginLogDO实体类 |
| message | fail_reason | 匹配LoginLogDO实体类 |
| login_location | location | 匹配LoginLogDO实体类 |
| - | device_info | 新增字段,合并browser和os |

**密码hash更新**:
- 更新为标准的BCrypt hash格式(60字符)
- 密码: `password`

---

## 验证安装

```sql
-- 检查表是否创建成功
SHOW TABLES;

-- 检查管理员账号
SELECT id, username, real_name, status FROM sys_user WHERE username='admin';

-- 检查角色数据
SELECT id, role_name, role_code FROM sys_role;

-- 检查部门数据
SELECT id, dept_name, dept_code FROM sys_dept;
```

---

**最后更新**: 2026-07-10  
**验证状态**: ✅ 已验证可用
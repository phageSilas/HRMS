# HRMS 后端文档

本目录包含 HRMS 后端系统的开发文档。

---

## 📚 文档索引

### 开发指南

| 文档 | 说明 |
|------|------|
| [协同开发文档.md](./协同开发文档.md) | Git工作流、项目启动说明 |
| [API接口文档.md](./API接口文档.md) | 各模块API接口详细说明 |
| [开发问题修复记录.md](./开发问题修复记录.md) | 开发过程中的问题及解决方案 |
| [后端系统底座使用指南.md](./后端系统底座使用指南.md) | 已实现功能的使用说明 |

### 拓展文档

| 文档 | 说明 |
|------|------|
| [拓展.md](./拓展.md) | 运维配置、部署相关、安全加固等非当前必需文档 |

---

## 🎯 项目当前状态

### ✅ 已成功运行

**后端项目已成功启动并可正常运行！**

- ✅ Maven构建成功
- ✅ Spring Boot应用正常启动
- ✅ 数据库连接正常
- ✅ 登录接口验证成功
- ✅ 部门接口验证成功

### 默认登录凭证

```
用户名: admin
密码: password
```

---

## 🚀 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

```sql
-- 创建数据库
CREATE DATABASE hrms DEFAULT CHARACTER SET utf8mb4;

-- 执行表结构脚本
-- 见 docs/sql/schema/

-- 执行初始数据脚本
-- 见 docs/sql/data/
```

### 3. 启动后端

```bash
# 编译项目
cd backend
./mvnw clean install -DskipTests

# 启动应用
cd hrms-server
java -jar target/hrms-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 4. 验证启动

```bash
# 测试部门接口
curl http://localhost:8080/departments/tree

# 测试登录接口
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

---

## 📖 相关文档

- 项目规范：`/docs/03-HRMS模块划分规范.md`
- 前端开发：`/docs/前端版本信息与开发文档.md`
- SQL脚本：`/docs/sql/`

---

**文档更新时间：** 2026-07-10  
**项目状态：** ✅ 后端已成功运行
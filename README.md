# HRMS 人资管理系统

> 一个人力资源管理系统，支持员工档案管理、入转调离流程、考勤管理、薪资管理、审批中心等核心人事业务。

---

## 📚 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.5.16 |
| Java 版本 | Java | 17 |
| ORM | MyBatis-Plus | 3.5.12 |
| 数据库 | MySQL | 8.0+ |
| 前端框架 | React + TypeScript | 18 / 5 |
| 前端脚手架 | UmiJS | - |
| 包管理 | pnpm | 11.10.0 |

---

## 🚀 快速启动

### 后端启动

```bash
# 1. 配置环境变量（首次运行）
cp .env.example .env
# 编辑 .env 文件，设置 JWT_SECRET 和数据库密码

# 2. 启动后端
cd backend
mvn clean install
cd hrms-server
mvn spring-boot:run

# 3. 验证启动
curl http://localhost:8080/actuator/health
```

### 前端启动

```bash
# 1. 安装依赖
cd frontend/hrms-frontend-umi
pnpm install

# 2. 启动开发服务器
pnpm dev

# 3. 访问
open http://localhost:8000
```

---

## 📖 文档导航

### 📂 项目级文档

| 文档 | 说明 | 路径 |
|------|------|------|
| 模块划分规范 | 项目架构和模块划分详细说明 | [docs/03-HRMS模块划分规范.md](./docs/03-HRMS模块划分规范.md) |
| 前端开发文档 | 前端版本信息和开发指南 | [docs/前端版本信息与开发文档.md](./docs/前端版本信息与开发文档.md) |
| SQL 脚本 | 数据库表结构和初始数据 | [docs/sql/](./docs/sql/) |

### 🔧 后端文档

| 文档 | 说明 | 路径 |
|------|------|------|
| 后端系统底座使用指南 | 核心功能使用方法和接口说明 | [backend/docs/后端系统底座使用指南.md](./backend/docs/后端系统底座使用指南.md) |
| 部署配置完成总结 | 已完成的部署配置说明 | [backend/docs/部署配置完成总结.md](./backend/docs/部署配置完成总结.md) |
| 部署检查清单 | 生产环境部署检查项 | [backend/docs/部署检查清单.md](./backend/docs/部署检查清单.md) |
| HTTPS 配置指南 | SSL/TLS 证书配置教程 | [backend/docs/HTTPS配置指南.md](./backend/docs/HTTPS配置指南.md) |
| 防火墙配置指南 | 防火墙和安全组配置教程 | [backend/docs/防火墙配置指南.md](./backend/docs/防火墙配置指南.md) |
| 安全问题修复报告 | 代码审查发现的问题及修复 | [backend/docs/安全问题修复报告.md](./backend/docs/安全问题修复报告.md) |
| 协同开发文档 | 团队协作和开发规范 | [backend/docs/协同开发文档.md](./backend/docs/协同开发文档.md) |

### 💻 前端文档

| 文档 | 说明 | 路径 |
|------|------|------|
| 前端 README | 前端项目说明和快速开始 | [frontend/hrms-frontend-umi/docs/README.md](./frontend/hrms-frontend-umi/docs/README.md) |
| 前端开发指南 | 开发流程和规范 | [frontend/hrms-frontend-umi/docs/DEVELOPMENT.md](./frontend/hrms-frontend-umi/docs/DEVELOPMENT.md) |

---

## 📁 项目结构

```
HRMS/
├── backend/                    # 后端代码
│   ├── hrms-common/           # 公共基础模块
│   ├── hrms-system/           # 系统管理域
│   ├── hrms-business/         # 业务域
│   ├── hrms-server/           # 启动聚合模块
│   └── docs/                  # 后端文档
├── frontend/                   # 前端代码
│   └── hrms-frontend-umi/
│       └── docs/              # 前端文档
├── docs/                       # 项目级文档
│   └── sql/                   # SQL 脚本
└── openspec/                    # OpenSpec 配置
```

---

## 👥 开发团队

| 开发者 | 负责模块 |
|--------|----------|
| 地基搭建者 | hrms-common + file + log |
| 成员 A | auth + organization |
| 成员 B | employee + personnel |
| 成员 C | attendance + salary |
| 成员 D | approval + mycenter |

---

## 📝 Git 提交规范

采用 Conventional Commits 格式：

```
<type>(<scope>): <subject>

<body>
```

### 核心提交类型

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新增功能 | `feat(auth): 添加登录功能` |
| `fix` | 修复 Bug | `fix(user): 修复用户创建时的密码验证` |
| `docs` | 文档更新 | `docs(readme): 更新项目说明` |
| `refactor` | 代码重构 | `refactor(service): 优化用户查询逻辑` |
| `test` | 测试相关 | `test(auth): 添加登录接口测试` |
| `chore` | 构建/工具变动 | `chore(deps): 更新依赖版本` |

### 提交示例

```bash
feat(auth): 添加用户登录功能

- 实现登录接口和 JWT 认证
- 添加登录日志记录
- 集成权限校验

Closes #123
```

---

## 🔗 快速链接

- **后端接口文档**: 启动后访问 `http://localhost:8080/swagger-ui.html`（如已配置）
- **健康检查**: `http://localhost:8080/actuator/health`
- **前端开发服务器**: `http://localhost:8000`

---

## 📞 联系方式

如有问题，请联系项目负责人或查看相关文档。

---

**文档更新时间：** 2026-07-10
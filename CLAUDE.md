# HRMS 人资管理系统

## 项目概述

HRMS 是一个人力资源管理系统，采用前后端分离架构，支持员工档案管理、入转调离流程、考勤管理、薪资管理、审批中心等核心人事业务。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.5.16 |
| Java 版本 | Java | 17 |
| ORM | MyBatis-Plus | 3.5.12 |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | - |
| 消息队列 | RabbitMQ | - |
| 前端框架 | React + TypeScript | 18 / 5 |
| 前端脚手架 | UmiJS | - |
| 包管理 | pnpm | 11.10.0 |

## 项目结构

```
HRMS/
├── backend/                    # 后端代码
│   ├── hrms-common/           # 公共基础模块（只做能力，不做业务）
│   ├── hrms-system/           # 系统管理域（auth、organization、file、log）
│   ├── hrms-business/         # 业务域（employee、personnel、attendance、salary、approval、mycenter）
│   └── hrms-server/           # 启动聚合模块
├── frontend/                   # 前端代码
│   └── hrms-frontend-umi/
├── docs/                       # 项目文档
│   └── 03-HRMS模块划分规范.md   # 核心开发规范（必读）
└── openspec/                    # OpenSpec 配置
    └── config.yaml
```

## 后端模块职责

| 模块 | 职责 | 禁止事项 |
|------|------|----------|
| `hrms-common` | 公共基础能力（返回体、异常、枚举、BaseEntity、SecurityContextHolder）**只做能力，不做业务** | 禁止反向依赖业务模块、禁止包含 Controller、禁止涉及数据表 |
| `hrms-system` | 系统底座（auth、organization、file、log） | 禁止依赖 business |
| `hrms-business` | 核心业务（employee、personnel、attendance、salary、approval、mycenter） | - |
| `hrms-server` | 启动装配，不承载业务逻辑 | 禁止写 Controller/Service/Mapper |

**依赖关系**：
```
hrms-server → hrms-business → hrms-system → hrms-common
```

## hrms-system 子模块

| 子模块 | 包路径 | 职责 | 数据表 |
|--------|--------|------|--------|
| auth | com.hrms.system.auth | 用户、角色、菜单、登录认证 | sys_user、sys_role、sys_menu、sys_user_role、sys_role_menu |
| organization | com.hrms.system.organization | 部门、职位、字典 | sys_dept、sys_post、sys_dict_type、sys_dict_data |
| file | com.hrms.system.file | 附件管理 | sys_file |
| log | com.hrms.system.log | 操作日志、登录日志 | sys_operate_log、sys_login_log |

## hrms-business 子模块

| 子模块 | 包路径 | 职责 | 数据表 |
|--------|--------|------|--------|
| employee | com.hrms.business.employee | 员工档案管理 | hr_employee、hr_employee_contract、hr_employee_transfer_record |
| personnel | com.hrms.business.personnel | 入转调离流程 | hr_entry_application、hr_regular_application、hr_transfer_application、hr_leave_application（待建） |
| attendance | com.hrms.business.attendance | 考勤管理 | hr_attendance_record、hr_leave_request、hr_attendance_summary（待建） |
| salary | com.hrms.business.salary | 薪资管理 | hr_salary_account、hr_salary_item、hr_salary_batch、hr_salary_detail（待建） |
| approval | com.hrms.business.approval | 审批中心 | hr_approval_instance、hr_approval_task |
| mycenter | com.hrms.business.mycenter | 个人中心 | 无独立表 |

## 开发协作分工

| 开发者 | 负责模块 | 包路径 |
|--------|----------|--------|
| 地基搭建者 | hrms-common + file + log | com.hrms.common, com.hrms.system.file, com.hrms.system.log |
| 成员 A | auth + organization | com.hrms.system.auth, com.hrms.system.organization |
| 成员 B | employee + personnel | com.hrms.business.employee, com.hrms.business.personnel |
| 成员 C | attendance + salary | com.hrms.business.attendance, com.hrms.business.salary |
| 成员 D | approval + mycenter | com.hrms.business.approval, com.hrms.business.mycenter |

## 包结构规范

```
com.hrms.{module}
├── controller/     # 接口层
├── service/        # 业务层接口
│   └── impl/       # 业务层实现
├── mapper/         # 数据访问层
├── entity/         # 数据库实体（XxxDO）
├── dto/            # 入参 DTO
├── vo/             # 出参 VO
├── convert/        # 对象转换器
├── enums/          # 模块枚举
├── config/         # 模块配置
└── exception/      # 模块异常
```

## 统一返回体

所有接口使用 `Result<T>` 统一返回：

```java
// 成功
Result.success(data)
Result.success()

// 失败
Result.failure(ErrorCode.XXX)
Result.failure(code, message)
```

**响应格式**：
```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

## 错误码规范

| 码段 | 类型 | 说明 |
|------|------|------|
| 0 | 成功 | - |
| 40001-40099 | 参数错误 | 参数缺失、格式错误 |
| 40100-40199 | 认证授权错误 | 未登录、无权限 |
| 50001-50009 | 系统内部错误 | 数据库异常、缓存异常 |
| 50010-50019 | 附件模块错误 | 文件过大、格式不支持等 |
| 50020-50029 | 日志模块错误 | 日志导出失败等 |
| 60001-60099 | 档案模块业务错误 | - |
| 60100-60199 | 组织模块业务错误 | - |
| 60200-60299 | 入离职模块业务错误 | - |
| 60300-60399 | 考勤模块业务错误 | - |
| 60400-60499 | 薪资模块业务错误 | - |
| 60500-60599 | 审批模块业务错误 | - |

## 数据库命名规范

- 表名：小写下划线风格
- 系统表：`sys_` 前缀（如 `sys_user`、`sys_role`）
- 业务表：`hr_` 前缀（如 `hr_employee`、`hr_approval_instance`）
- 枚举字段：使用 `TINYINT` 类型存储编码值

## 公共字段约定（BaseEntity）

所有业务表必须包含以下公共字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT UNSIGNED | 主键 |
| `create_by` | BIGINT UNSIGNED | 创建人（自动填充） |
| `create_time` | DATETIME | 创建时间（自动填充） |
| `update_by` | BIGINT UNSIGNED | 更新人（自动填充） |
| `update_time` | DATETIME | 更新时间（自动填充） |
| `is_deleted` | TINYINT(1) | 逻辑删除标记，默认 0 |

## 核心跨模块接口

| 序号 | 接口名 | 路径 | 提供方 | 调用方 |
|------|--------|------|--------|--------|
| 1 | 获取员工简要信息 | /employees/brief/{id} | employee | attendance, salary, approval, mycenter |
| 2 | 生成工号 | /employees/gen-no | employee | personnel |
| 3 | 获取部门树 | /departments/tree | organization | employee, personnel, attendance, salary, approval |
| 4 | 发起审批任务 | /approval/start | approval | personnel |
| 5 | 审批回调 | /approval/callback | 各业务模块 | approval |
| 6 | 获取员工完整档案 | /employees/full/{id} | employee | personnel, salary, mycenter |
| 7 | 获取员工考勤汇总 | /attendance/summary/{employeeId}/{yearMonth} | attendance | salary |
| 8 | 获取员工薪资档案 | /salary/account/{employeeId} | salary | employee, personnel, mycenter |
| 9 | 按部门获取员工列表 | /employees/by-department/{departmentId} | employee | attendance, salary |
| 10 | 获取待审批数量 | /approval/pending-count | approval | mycenter |
| 11 | 获取字段权限 | /permissions/field | auth | 所有模块 |
| 12 | 获取数据权限范围 | /permissions/data-scope | auth | 所有模块 |

## 枚举使用规范

| 场景 | 规范 |
|------|------|
| 数据库存储 | TINYINT 类型，存储编码值（整数） |
| Java 传递 | 使用枚举类，通过 `getCode()` 获取编码 |
| 前端接收 | 返回编码值（整数），前端通过字典映射显示文案 |
| 比较方式 | 禁止硬编码 `if (status == 1)`，必须用枚举比较 |

## Git 提交规范

采用 Conventional Commits 格式：

```
<type>(<scope>): <subject>

<body>
```

**类型**：
| 类型 | 用途 | 是否影响版本号 |
|------|------|----------------|
| feat | 新增功能 | 是 |
| fix | 修复 Bug | 是 |
| refactor | 重构 | 否 |
| docs | 文档更新 | 否 |
| test | 测试相关 | 否 |
| chore | 构建/工具变动 | 否 |

**示例**：
```
feat(employee): 新增员工档案创建与详情查询能力

- 新增员工主档实体与查询接口
- 接入部门、职位与汇报人关联校验
- 补充员工工号生成规则与数据校验
```

## 分支管理

| 分支 | 用途 |
|------|------|
| master | 公共主分支，稳定代码 |
| LiShiHao | 个人开发分支 |
| GaoSiJun | 个人开发分支 |

## 开发流程

1. 拉取最新代码
2. 配置数据源（`application-dev.yaml`）
3. 启动 `HrmsServerApplication`
4. 在对应模块包下开发

## 地基搭建前置任务

> 以下任务由地基搭建者**优先实现**，其他成员开发业务前必须确认已完成。

### hrms-common 必须实现

| 类名 | 路径 | 作用 |
|------|------|------|
| MyMetaObjectHandler.java | com.hrms.common.handler | MyBatis-Plus 自动填充 |
| MybatisPlusConfig.java | com.hrms.common.config | MyBatis-Plus 配置（逻辑删除） |

### hrms-server 必须实现

| 类名 | 路径 | 作用 |
|------|------|------|
| CorsConfig.java | com.hrms.server.config | 跨域配置 |

### hrms-system.auth 必须实现

| 类名 | 路径 | 作用 | 负责人 |
|------|------|------|--------|
| JwtUtils.java | com.hrms.system.auth.util | JWT 生成与解析 | A 同学 |

## 相关文档

- `docs/03-HRMS模块划分规范.md`：完整的模块划分规范文档（必读）

## 快速开始

```bash
# 后端启动
cd backend
mvn clean install
java -jar hrms-server/target/hrms-server-0.0.1-SNAPSHOT.jar

# 前端启动
cd frontend/hrms-frontend-umi
pnpm install
pnpm dev
```

## 验证地基搭建成功

1. 项目能正常启动
2. 访问 `http://localhost:8080/actuator/health` 返回 UP
3. 能正常连接数据库
4. MyBatis-Plus 自动填充生效（insert 时 create_time 有值）
5. 逻辑删除生效（deleteById 变 UPDATE）
6. 前端可正常调用后端接口（CORS 配置生效）
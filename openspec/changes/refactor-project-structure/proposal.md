## 为什么

当前项目结构不符合 `03-HRMS模块划分规范.md` 定义的架构，存在以下问题：

1. **hrms-common 缺少核心基础设施**：没有 BaseEntity、SecurityContextHolder、MyMetaObjectHandler 等关键组件，导致业务开发时无法自动填充公共字段、无法统一获取当前用户。
2. **hrms-system 目录结构混乱**：只有占位文件，未按 auth、organization、file、log 四个子模块组织。
3. **hrms-business 目录结构混乱**：只有占位文件，未按 employee、personnel、attendance、salary、approval、mycenter 六个子模块组织。
4. **缺少跨域配置**：前后端分离项目需要 CORS 配置才能正常调用接口。

此变更为四人团队协作开发奠定基础，必须在地基搭建阶段完成。

## 变更内容

### hrms-common 新增文件

| 文件 | 作用 |
|------|------|
| entity/BaseEntity.java | 公共字段基类（id、createBy、createTime、updateBy、updateTime、isDeleted） |
| security/SecurityContextHolder.java | 用户上下文持有者（基于 ThreadLocal） |
| security/UserContext.java | 用户上下文对象（userId、deptId、roleIds） |
| handler/MyMetaObjectHandler.java | MyBatis-Plus 自动填充处理器 |
| config/MybatisPlusConfig.java | MyBatis-Plus 配置（逻辑删除） |

### hrms-system 目录重构

**删除**：现有占位文件（SystemModuleController.java、SystemModuleService.java、SystemModuleServiceImpl.java）

**新建**子模块目录结构：
- `auth/` - 权限体系（controller, service/impl, mapper, entity, dto, vo, convert, enums）
- `organization/` - 组织架构（controller, service/impl, mapper, entity, dto, vo, convert, enums）
- `file/` - 附件管理（controller, service/impl, mapper, entity, dto, vo, convert）
- `log/` - 日志管理（controller, service/impl, mapper, entity, dto, vo, enums）

### hrms-business 目录重构

**删除**：现有占位文件（BusinessModuleController.java、BusinessModuleService.java、BusinessModuleServiceImpl.java）

**新建**子模块目录结构：
- `employee/` - 员工档案（controller, service/impl, mapper, entity, dto, vo, convert, enums）
- `personnel/` - 入转调离（controller, service/impl, mapper, entity, dto, vo, convert, enums）
- `attendance/` - 考勤管理（controller, service/impl, mapper, entity, dto, vo, convert, enums）
- `salary/` - 薪资管理（controller, service/impl, mapper, entity, dto, vo, convert, enums）
- `approval/` - 审批中心（controller, service/impl, mapper, entity, dto, vo, convert, enums, handler）
- `mycenter/` - 个人中心（controller, service/impl, dto, vo, enums）

### hrms-server 新增文件

| 文件 | 作用 |
|------|------|
| config/CorsConfig.java | 跨域配置，允许前端调用后端接口 |

### 不修改的内容

- hrms-common 现有的 Result.java、PageResult.java、异常处理类、枚举类
- 所有 SQL 文件
- pom.xml 文件
- application.yaml 配置文件

## 功能 (Capabilities)

### 新增功能

- `base-entity`: 公共字段基类，所有业务表实体继承此类，支持 MyBatis-Plus 自动填充
- `security-context`: 用户上下文管理，基于 ThreadLocal 存储当前登录用户信息
- `mybatis-plus-config`: MyBatis-Plus 全局配置，包括逻辑删除和自动填充
- `cors-config`: 跨域配置，支持前后端分离开发
- `module-scaffolding`: 模块目录脚手架，为 10 个业务子模块创建标准分层目录结构

### 修改功能

无（此变更为新增基础设施，不修改现有功能需求）

## 影响

### 受影响的代码

| 模块 | 影响 |
|------|------|
| hrms-common | 新增 5 个文件，不修改现有代码 |
| hrms-system | 删除 3 个占位文件，新建 4 个子模块目录 |
| hrms-business | 删除 3 个占位文件，新建 6 个子模块目录 |
| hrms-server | 新增 CorsConfig.java |

### 受影响的 API

无（此变更不涉及 API 变更）

### 受影响的依赖

| 依赖 | 说明 |
|------|------|
| MyBatis-Plus | 需要配置逻辑删除和自动填充 |

### 系统影响

- 开发人员可以开始业务开发
- 前端可以正常调用后端接口
- 公共字段自动填充生效
- 逻辑删除生效
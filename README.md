## 基本:
master为公共主分支,里面有基础代码  
backend为后端相关代码,frontend为前端相关代码  
每人先提交自己的模块的到各自分支  
### 技术栈版本:
后端:springboot 3.5.16  
前端:  
  
## 提交规范:
Git 提交规范中最主流的是 **Conventional Commits（约定式提交）**，它通过结构化的提交信息，让代码变更历史清晰可读，并支持自动化工具生成变更日志（CHANGELOG）。

### 📝 核心提交类型 (Type)

提交信息的第一部分是 `type`，用于说明本次提交的性质。最核心、最常用的是 `feat` 和 `fix`：

| 类型 (Type) | 含义 | 是否影响版本号 | 说明 |
| :--- | :--- | :--- | :--- |
| **`feat`** | 新增功能 (Feature) | **是** (MINOR) | 为应用或库添加一个新特性。 |
| **`fix`** | 修复 Bug | **是** (PATCH) | 修复了一个代码缺陷。 |
| **`docs`** | 文档更新 | 否 | 仅修改文档，如 `README.md`。 |
| **`style`** | 代码格式调整 | 否 | 不影响代码逻辑的变动，如空格、缩进、分号等。 |
| **`refactor`** | 代码重构 | 否 | 既不修复 bug 也不添加新功能的代码修改。 |
| **`perf`** | 性能优化 | 否 | 提升性能的代码变更。 |
| **`test`** | 测试相关 | 否 | 添加或修改测试代码。 |
| **`chore`** | 构建/工具变动 | 否 | 构建流程、依赖管理等方面的改动。 |
| **`ci`** | CI配置变更 | 否 | 修改持续集成（CI）的配置文件和脚本。 |
| **`build`** | 构建系统变更 | 否 | 影响构建系统或外部依赖的更改。 |
| **`revert`** | 回滚提交 | 否 | 撤销之前的某次提交。 |

> 注：`feat` 和 `fix` 类型的提交会**自动出现在变更日志（CHANGELOG）** 中，其他类型则通常不会。

### 📐 提交信息格式

一个标准的提交信息包含三个部分，其中 **Header 是必需的**。

```
<type>(<scope>): <subject>
// 空一行
<body>
// 空一行
<footer>
```
IDEA内部提交示例:
```
feat(entity): 添加短链接访问统计实体的分组标识字段
- 在 LinkAccessStatsDO 实体中新增 gid 字段用于分组标识
- 为新字段添加相应的注释说明
- 保持实体类结构的一致性，支持按分组进行数据统计功能
```
# HRMS 后端单体多模块拆分方案

## 摘要
基于你现在的目录现状，`D:\IDEA-java\HRMS\backend` 目前还是一个标准的单模块 Spring Boot 骨架。  
如果你现在是要做“基础代码配置”，我更推荐你把 `backend` 调整成 **4 个 Maven 模块以内的单体多模块项目**，而不是直接按 8 个业务模块拆太细。

**主推荐方案：4 模块**
1. `hrms-common`
2. `hrms-system`
3. `hrms-business`
4. `hrms-server`

---

## 模块划分

### 1. `hrms-common`
定位：公共基础模块，所有模块都依赖它。

建议放的内容：
- 通用返回体 `Result`、分页对象、统一异常
- 公共工具类、日期/金额/字符串工具
- 基础枚举、常量、状态码
- 基础父类：如 `BaseEntity`、审计字段
- 通用配置：Jackson、跨域、Swagger/OpenAPI、MyBatis Plus 或 JPA 公共配置
- 安全上下文抽象、登录用户信息模型
- 公共注解、AOP、日志审计
- Redis、缓存、文件上传、导入导出公共能力

不要放的内容：
- 具体业务表、具体业务 Service
- 权限、员工、薪资这类明确属于业务域的代码

建议原则：
- `common` 只放“全局可复用能力”
- 不要把任何业务都塞进 `common`，否则后面会变成大杂烩

---

### 2. `hrms-system`
定位：系统基础域模块，负责全系统底座能力。

建议归属业务：
- `2. 权限体系`
- `3. 组织架构管理`
- `4. 员工档案管理`

建议放的内容：
- 用户、角色、菜单、按钮权限
- 数据权限、字段权限
- 部门、岗位、职级、组织树
- 员工主档、员工扩展信息、任职信息、合同基础信息
- 登录鉴权相关的业务实现
- 系统字典、区域、基础配置等偏底座内容

这样放的原因：
- 这 3 块本质上都是“系统主数据”
- 后面的审批、考勤、薪资、个人中心都会依赖这里
- 把员工档案放进 `system` 比放进 `business` 更稳，因为它是基础主数据，不只是某个流程的一部分

---

### 3. `hrms-business`
定位：核心业务域模块，承接 HR 具体流程与计算逻辑。

建议归属业务：
- `5. 入转调离流程`
- `6. 考勤管理`
- `7. 薪资管理`
- `8. 审批中心`
- `9. 个人中心`

建议放的内容：
- 入职、转正、调岗、离职流程
- 审批流、待办、已办、委托审批
- 考勤组、排班、打卡、请假、统计
- 薪资账套、薪资档案、月度核算、工资条
- 个人中心：我的档案、我的考勤、我的薪资、账号安全

这样放的原因：
- 这些都属于“建立在员工/组织/权限基础上的业务应用层”
- `审批中心` 应该和业务流程放一起，不建议单独抽成一个模块，除非以后你真打算做流程平台
- `考勤 + 薪资` 虽然复杂，但现在仍建议先放在 `business`，避免早期过度拆分

---

### 4. `hrms-server`
定位：启动聚合模块，只负责启动和装配，不承载具体业务代码。

建议放的内容：
- Spring Boot 启动类
- `application.yaml`、`application-dev.yaml`、`application-prod.yaml`
- 统一装配配置
- 组件扫描、Mapper 扫描
- 全局启动初始化逻辑

依赖关系建议：
- `hrms-server` 依赖 `hrms-common`
- `hrms-server` 依赖 `hrms-system`
- `hrms-server` 依赖 `hrms-business`
- `hrms-business` 依赖 `hrms-system`
- `hrms-system` 依赖 `hrms-common`
- `hrms-business` 依赖 `hrms-common`

不要反向依赖：
- `system` 不要依赖 `business`
- `common` 不要依赖任何业务模块

---

## 对应目录建议
建议最终后端目录长这样：

```text
backend
├─ pom.xml                      # 父工程
├─ hrms-common
│  └─ pom.xml
├─ hrms-system
│  └─ pom.xml
├─ hrms-business
│  └─ pom.xml
└─ hrms-server
   └─ pom.xml
```

包名建议统一，例如：

```text
com.hrms.common
com.hrms.system
com.hrms.business
com.hrms.server
```

---

## 关键接口与边界约定

- `hrms-system` 对外提供：用户、角色、部门、岗位、员工主数据查询与维护能力
- `hrms-business` 只通过 `system` 提供的服务访问员工/组织主数据
- `hrms-common` 不允许出现业务 SQL、业务实体、业务流程代码
- `hrms-server` 只负责启动，不写 Controller/Service/Mapper 业务实现
- Controller 可以按模块分别放在 `system` 和 `business` 中，`server` 只作为聚合启动器

---

## 测试与验收
基础代码配置完成后，至少要满足这些验收条件：

1. 父工程 `backend/pom.xml` 可以统一管理版本和依赖
2. `hrms-server` 可以单独启动成功
3. `hrms-system`、`hrms-business` 中的 Bean 能被正常扫描和注入
4. 公共异常、统一返回体、基础配置能在所有模块复用
5. 后续新增一个业务子包时，不需要改整体结构

---



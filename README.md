# 基本:
master为公共主分支,里面有基础代码  
backend为后端相关代码,frontend为前端相关代码  
每人先提交自己的模块的到各自分支  
## 技术栈版本:
后端:springboot 3.5.16  
前端:node:24.18.0, pnpm:11.10.0, React 18 + TypeScript 5
  
# 提交规范:
Git 提交规范中最主流的是 **Conventional Commits（约定式提交）**，它通过结构化的提交信息，让代码变更历史清晰可读，并支持自动化工具生成变更日志（CHANGELOG）。

## 📝 核心提交类型 (Type)

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

## 📐 提交信息格式

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
# HRMS 后端多模块方案

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


# 分层规范与参考
接下来包结构建议这样理解：

- 先按“模块”分：`system`、`business`
- 再按“层”分：`controller`、`service`、`mapper`、`entity`、`dto`、`vo` 等
- `common` 只放公共能力，不放具体业务

也就是说，不建议整个项目只有一套总的：

- `controller`
- `service`
- `mapper`

因为时间长了以后，权限、员工、考勤、薪资都会堆在一起，非常乱。

---

**二、推荐包结构(仅供参考)**

比如：

```text
com.hrms
├─ common
├─ system
│  ├─ controller
│  ├─ service
│  │  └─ impl
│  ├─ mapper
│  ├─ entity
│  ├─ dto
│  ├─ vo
│  ├─ convert
│  ├─ enums
│  └─ domain
└─ business
   ├─ controller
   ├─ service
   │  └─ impl
   ├─ mapper
   ├─ entity
   ├─ dto
   ├─ vo
   ├─ convert
   ├─ enums
   └─ domain
```

如果后面业务更多，还可以继续在模块下再按子业务拆：

```text
com.hrms.business.attendance
com.hrms.business.salary
com.hrms.business.approval
```

这会更清晰。

---

**三、每一层放什么**

**1. `controller`**
作用：对外提供接口，接收前端请求。

放的内容：
- REST 接口
- 参数接收
- 调用 `service`
- 返回统一结果 `Result`

不要放的内容：
- 复杂业务逻辑
- SQL
- 大量数据转换

一句话：`controller` 负责“接请求、调服务、回结果”。

---

**2. `service`**
作用：业务层接口，定义业务能力。

放的内容：
- 业务方法定义
- 比如新增员工、查询部门树、发起审批、计算工资

例如：
- `EmployeeService`
- `AttendanceService`

---

**3. `service.impl`**
作用：业务实现层。

放的内容：
- 具体业务逻辑实现
- 多表组合处理
- 事务控制
- 调用 `mapper`
- 调用其他模块服务

这里才是核心业务代码的主要位置。

一句话：`impl` 是“真正干活”的地方。

---

**4. `mapper`**
作用：数据访问层。

放的内容：
- MyBatis / MyBatis-Plus 的 Mapper 接口
- 数据库查询方法
- 自定义 SQL 对应的方法

不要放的内容：
- 业务流程判断
- controller 参数对象
- 页面返回拼装逻辑

一句话：`mapper` 只管“查库、写库”。

---

**5. `entity`**
作用：数据库实体对象。

放的内容：
- 对应表的实体类
- 比如 `Employee`、`Dept`、`SalaryRecord`

一般放：
- 表字段
- 基础 ORM 注解
- 少量基础属性方法

不要放太多业务逻辑。

---

**6. `dto`**
作用：数据传输对象，通常用于“入参”。

放的内容：
- 新增/修改/查询条件对象
- 比如：
    - `EmployeeCreateDTO`
    - `EmployeeQueryDTO`
    - `SalaryCalcDTO`

特点：
- 面向接口输入
- 可以加参数校验注解

---

**7. `vo`**
作用：视图对象，通常用于“出参”。

放的内容：
- 返回给前端的展示对象
- 比如：
    - `EmployeeDetailVO`
    - `DeptTreeVO`
    - `SalarySlipVO`

特点：
- 面向页面展示
- 字段不一定和数据库表一致
- 可以是多个表拼出来的结果

---

**8. `convert`**
作用：对象转换层。

放的内容：
- `entity`、`dto`、`vo` 之间的转换
- 可以用 `MapStruct`

比如：
- `EmployeeConvert`

这个包很有用，能避免在 `service` 里到处手写 `setXXX()`。

---

**9. `enums`**
作用：当前模块自己的枚举。

放的内容：
- 员工状态
- 审批状态
- 请假类型
- 薪资计算状态

如果是全局通用枚举，可以放 `common.enums`；
如果是某个业务独有，就放该模块下的 `enums`。

---

**10. `domain`**
作用：领域对象或业务聚合对象。

这个包不是所有项目都必须有，但复杂业务里很有用。

适合放：
- 比单纯 `entity` 更偏业务组合的数据结构
- 比如“员工档案聚合对象”“薪资核算上下文”“审批流上下文”

如果你们项目现在刚开始，可以先少用；后面复杂了再引入。

---

**11. `util`**
作用：工具类。

这个包一定要克制使用。

适合放：
- 纯工具方法
- 无状态、可复用逻辑
- 比如日期处理、编号生成辅助、脱敏工具

不要把业务代码塞进 `util`。
很多项目最后烂掉，就是因为把看不懂的逻辑都丢进 `util`。

建议：
- 通用工具放 `common.util`
- 模块专用工具放对应模块下 `util`

---

**12. `common`**
作用：公共基础层。

这里应该放：
- `Result` 统一返回体
- `PageResult`
- 全局异常
- 常量
- 通用枚举
- 基础配置类
- 公共注解
- AOP
- 安全上下文
- 基础工具类
- 基础父类

不应该放：
- 员工业务
- 考勤业务
- 薪资业务
- 权限表实体

一句话：`common` 是“全局复用能力”，不是“杂物间”。

---

**四、比 `util/common` 更重要的几个包**

实际项目里，除了你提到的这些，我更建议你重视这几个：

**1. `config`**
放配置类：
- Swagger/OpenAPI
- 跨域
- MyBatis-Plus
- Jackson
- Redis
- Sa-Token / Spring Security

**2. `exception`**
放异常相关：
- 业务异常
- 参数异常
- 全局异常处理器

**3. `constant`**
放常量：
- redis key
- 默认值
- 通用标识

**4. `assembler` / `convert`**
专门做对象转换，能显著提升代码整洁度。

---

**五、几个常见误区**

**1. 不要把所有东西都塞进 `util`**
这是最常见的问题。

**2. 不要让 `controller` 写业务**
`controller` 一复杂，后面维护非常痛苦。

**3. 不要让 `entity` 直接当接口出参**
否则后期字段暴露、扩展性、前后端适配都会出问题。

**4. 不要把所有公共代码都扔进 `common`**
`common` 一旦失控，后面会变成“全项目垃圾场”。

**5. 不要只按技术层分，不按业务域分**
只分 `controller/service/mapper`，项目一大必乱。

---

**六、一套相对规范的分层职责**

可以简单记成：

- `controller`：接收请求
- `dto`：接口入参
- `service`：定义业务能力
- `service.impl`：实现业务逻辑
- `mapper`：数据库操作
- `entity`：表实体
- `vo`：接口出参
- `convert`：对象转换
- `common`：公共能力
- `config`：配置
- `exception`：异常处理
- `util`：纯工具

---

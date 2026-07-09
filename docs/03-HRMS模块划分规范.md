# HRMS 模块划分规范

> 本文档整合原 01-HRMS全局开发规范.md 和 02-HRMS全局地基搭建规范.md，重新组织为八业务模块架构，供四人团队独立开发使用。

---

## 一、项目架构总览

### 1.1 四个顶层模块

| 模块 | 职责 | 禁止事项 |
|------|------|----------|
| `hrms-common` | 公共基础能力（返回体、异常、枚举、工具类、基础实体、安全上下文）**只做能力，不做业务** | 禁止反向依赖业务模块、禁止包含 Controller、禁止涉及数据表 |
| `hrms-system` | 系统底座（权限体系、组织架构、附件管理、日志管理） | 禁止依赖 business |
| `hrms-business` | 核心业务（员工、入转调离、考勤、薪资、审批、个人中心） | - |
| `hrms-server` | 启动装配，不承载业务逻辑 | 禁止写 Controller/Service/Mapper |

### 1.2 模块依赖关系

```
hrms-server → hrms-business → hrms-system → hrms-common
```

### 1.3 业务模块划分

#### hrms-system（系统管理域，4 个子模块）

| 模块编号 | 模块名称 | 包路径 | 职责 | 数据表 |
|----------|----------|--------|------|--------|
| 模块1 | auth | com.hrms.system.auth | 用户、角色、菜单、登录认证 | sys_user、sys_role、sys_menu、sys_user_role、sys_role_menu |
| 模块2 | organization | com.hrms.system.organization | 部门、职位、字典 | sys_dept、sys_post、sys_dict_type、sys_dict_data |
| 模块3 | file | com.hrms.system.file | 附件管理 | sys_file |
| 模块4 | log | com.hrms.system.log | 操作日志、登录日志 | sys_operate_log、sys_login_log |

#### hrms-business（业务域，6 个子模块）

| 模块编号 | 模块名称 | 包路径 | 职责 |
|----------|----------|--------|------|
| 模块5 | employee | com.hrms.business.employee | 员工档案管理 |
| 模块6 | personnel | com.hrms.business.personnel | 入转调离流程 |
| 模块7 | attendance | com.hrms.business.attendance | 考勤管理 |
| 模块8 | salary | com.hrms.business.salary | 薪资管理 |
| 模块9 | approval | com.hrms.business.approval | 审批中心 |
| 模块10 | mycenter | com.hrms.business.mycenter | 个人中心 |

### 1.4 开发协作分工

| 开发者 | 负责模块 | 包路径 |
|--------|----------|--------|
| 地基搭建者 | hrms-common + file + log | com.hrms.common, com.hrms.system.file, com.hrms.system.log |
| 成员 A | auth + organization | com.hrms.system.auth, com.hrms.system.organization |
| 成员 B | employee + personnel | com.hrms.business.employee, com.hrms.business.personnel |
| 成员 C | attendance + salary | com.hrms.business.attendance, com.hrms.business.salary |
| 成员 D | approval + mycenter | com.hrms.business.approval, com.hrms.business.mycenter |

---

## 二、开发规范

> 以下内容保留自 01-HRMS全局开发规范.md，命名、分层、接口、错误码、Git 规范不变。

### 2.1 命名规范

- 实体类：`XxxDO`（数据库实体）
- DTO：`XxxRequestDTO`、`XxxQueryDTO`、`XxxCommandDTO`
- VO：`XxxVO`、`XxxPageVO`
- Mapper：`XxxMapper`
- Service：`XxxService`
- Service 实现：`XxxServiceImpl`
- Controller：`XxxController`
- 转换器：`XxxConvert`
- 枚举：`XxxEnum`

### 2.2 分层规范

后端业务实现统一按照以下分层组织：

- `controller`：接口控制层
- `service`：业务服务接口
- `service.impl`：业务服务实现
- `mapper`：数据访问层
- `entity`：数据库实体
- `dto`：请求或过程数据传输对象
- `vo`：返回视图对象
- `convert`：对象转换器
- `enums`：模块内业务枚举
- `config`：模块配置
- `exception`：模块异常

### 2.3 接口规范

#### HTTP 语义规范

| 方法 | 用途 |
|------|------|
| `GET` | 查询 |
| `POST` | 新增、流程发起、复杂条件查询 |
| `PUT` | 全量更新 |
| `PATCH` | 部分更新、状态变更 |
| `DELETE` | 逻辑删除 |

#### 分页接口规范

分页查询至少包含：
- `pageNum`：页码（从 1 开始）
- `pageSize`：每页大小
- `total`：总记录数
- `records`：数据列表

#### 统一返回体

所有接口使用 `Result<T>` 统一返回：

```java
// 成功
Result.success(data)
Result.success()

// 失败
Result.failure(ErrorCode.XXX)
Result.failure(code, message)
```

响应格式：
```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

### 2.4 错误码规范

| 码段 | 类型 | 说明 |
|------|------|------|
| `0` | 成功 | - |
| `40001-40099` | 参数错误 | 参数缺失、格式错误 |
| `40100-40199` | 认证授权错误 | 未登录、无权限 |
| `50001-50009` | 系统内部错误 | 数据库异常、缓存异常 |
| `50010-50019` | 附件模块错误 | 文件过大、格式不支持等 |
| `50020-50029` | 日志模块错误 | 日志导出失败等 |
| `60001-60099` | 档案模块业务错误 | - |
| `60100-60199` | 组织模块业务错误 | - |
| `60200-60299` | 入离职模块业务错误 | - |
| `60300-60399` | 考勤模块业务错误 | - |
| `60400-60499` | 薪资模块业务错误 | - |
| `60500-60599` | 审批模块业务错误 | - |

### 2.5 Git 提交规范

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

### 2.6 枚举使用规范

**存储与传递规则**：

| 场景 | 规范 |
|------|------|
| 数据库存储 | TINYINT 类型，存储编码值（整数） |
| Java 传递 | 使用枚举类，通过 `getCode()` 获取编码 |
| 前端接收 | 返回编码值（整数），前端通过字典映射显示文案 |
| 比较方式 | 禁止硬编码 `if (status == 1)`，必须用枚举比较 |

**正确示例**：
```java
// 正确：使用枚举比较
if (employee.getEmploymentStatus() == EmployeeStatusEnum.FORMAL.getCode()) {
    // ...
}

// 正确：使用枚举类
if (EmployeeStatusEnum.FORMAL.equals(EmployeeStatusEnum.getByCode(status))) {
    // ...
}
```

**错误示例**：
```java
// 错误：硬编码
if (status == 2) {
    // ...
}
```

### 2.7 异常处理规范

**全局统一拦截**：

`GlobalExceptionHandler` 在 hrms-common 中全局统一拦截，禁止各模块单独配置。

**异常定义规则**：

1. 业务异常统一抛出 `GlobalException`
2. 传入对应的 `ErrorCode`
3. 各模块可定义模块专有错误码，但统一用 `GlobalException`

**正确示例**：
```java
// 正确：统一使用 GlobalException
public void updateEmployee(EmployeeUpdateDTO dto) {
    EmployeeDO employee = employeeMapper.selectById(dto.getId());
    if (employee == null) {
        throw new GlobalException(ErrorCode.EMPLOYEE_NOT_FOUND);
    }
    // ...
}

// 正确：模块自定义错误码
public class EmployeeErrorCode {
    public static final ErrorCode EMPLOYEE_NOT_FOUND = 
        new ErrorCode(60001, "员工不存在");
    public static final ErrorCode DUPLICATE_EMPLOYEE_NO = 
        new ErrorCode(60002, "工号重复");
}
```

### 2.8 MyBatis-Plus 自动填充规范

**实现归属**：

`MetaObjectHandler` 在 **hrms-common** 中统一实现，禁止各模块单独实现。

**填充规则**：

| 字段 | 填充时机 | 填充值来源 |
|------|----------|------------|
| `create_by` | INSERT | SecurityContextHolder.getUserId() |
| `create_time` | INSERT | LocalDateTime.now() |
| `update_by` | INSERT/UPDATE | SecurityContextHolder.getUserId() |
| `update_time` | INSERT/UPDATE | LocalDateTime.now() |

**实现示例**：
```java
/**
 * MyBatis-Plus 自动填充处理器
 * 统一在 hrms-common 中实现
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createBy", Long.class, 
            SecurityContextHolder.getUserId());
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, 
            LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateBy", Long.class, 
            SecurityContextHolder.getUserId());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, 
            LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateBy", Long.class, 
            SecurityContextHolder.getUserId());
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, 
            LocalDateTime.now());
    }
}
```

**实体类使用**：
```java
public class EmployeeDO extends BaseEntity {
    
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

> **禁止事项**：
> - 禁止各模块单独实现 `MetaObjectHandler`
> - 禁止在业务代码中手动 set createBy/createTime/updateBy/updateTime

### 2.9 认证上下文规范

**规范要求**：

登录用户信息（userId、deptId、roleIds）统一由 `com.hrms.common.security.SecurityContextHolder` 管理（基于 ThreadLocal），所有模块必须使用该工具类获取当前用户。

**禁止事项**：
- 禁止自行解析 Token
- 禁止在各模块重复实现用户上下文管理

**正确示例**：
```java
// 正确：使用 SecurityContextHolder 获取当前用户
public void createEmployee(EmployeeCreateDTO dto) {
    Long currentUserId = SecurityContextHolder.getUserId();
    Long currentDeptId = SecurityContextHolder.getDeptId();
    // ...
}
```

**工具类定义**：
```java
/**
 * 安全上下文持有者
 * 基于 ThreadLocal 存储当前登录用户信息
 */
public class SecurityContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    public static Long getUserId() {
        UserContext context = CONTEXT.get();
        return context != null ? context.getUserId() : null;
    }

    public static Long getDeptId() {
        UserContext context = CONTEXT.get();
        return context != null ? context.getDeptId() : null;
    }

    public static List<Long> getRoleIds() {
        UserContext context = CONTEXT.get();
        return context != null ? context.getRoleIds() : Collections.emptyList();
    }
}
```

---

## 三、模块骨架

### 3.1 hrms-common（公共基础模块）

#### 职责描述

公共基础模块，**只做能力，不做业务**，负责沉淀全项目复用的纯技术能力：
- 通用返回体
- 枚举与错误码
- 全局异常处理
- 基础实体（公共字段）
- 安全上下文（用户信息传递）
- 工具类与基础常量
- 通用配置基座

> **禁止事项**：hrms-common 不包含 Controller、不涉及数据表、不承载业务逻辑。

#### 目录结构

```
hrms-common/
└── src/main/java/com/hrms/common/
    ├── model/                    # 统一返回体
    │   ├── Result.java
    │   └── PageResult.java
    ├── exception/                # 全局异常处理
    │   ├── GlobalException.java
    │   ├── ErrorCode.java
    │   └── GlobalExceptionHandler.java
    ├── entity/                   # 基础实体
    │   └── BaseEntity.java       # 公共字段基类
    ├── enums/                    # 枚举类（13个）
    │   ├── BaseEnum.java
    │   ├── EmployeeStatusEnum.java
    │   ├── GenderEnum.java
    │   ├── HireTypeEnum.java
    │   ├── ContractTypeEnum.java
    │   ├── LeaveTypeEnum.java
    │   ├── ApprovalStatusEnum.java
    │   ├── TaskStatusEnum.java
    │   ├── ApproveResultEnum.java
    │   ├── DataScopeEnum.java
    │   ├── MenuTypeEnum.java
    │   ├── SalaryBatchStatusEnum.java
    │   ├── AttendanceStatusEnum.java
    │   └── BizTypeEnum.java
    ├── security/                 # 安全相关
    │   ├── SecurityContextHolder.java    # 用户上下文持有者
    │   └── UserContext.java              # 用户上下文对象
    ├── annotation/               # 自定义注解
    ├── aop/                      # AOP切面
    ├── config/                   # 公共配置
    ├── constant/                 # 常量定义
    ├── handler/                  # MyBatis-Plus 自动填充
    │   └── MyMetaObjectHandler.java
    └── util/                     # 工具类
```

#### BaseEntity 公共字段约定

所有业务表（sys_* 和 hr_*）必须包含以下公共字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `id` | BIGINT UNSIGNED | 主键 |
| `create_by` | BIGINT UNSIGNED | 创建人 |
| `create_time` | DATETIME | 创建时间 |
| `update_by` | BIGINT UNSIGNED | 更新人 |
| `update_time` | DATETIME | 更新时间 |
| `is_deleted` | TINYINT(1) | 逻辑删除标记，默认 0 |

**BaseEntity.java**：
```java
/**
 * 基础实体类
 * 所有业务表实体必须继承此类
 */
public class BaseEntity {

    private Long id;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
```

---

### 3.2 hrms-system（系统管理域）

#### 3.2.1 auth（权限体系）

#### 职责描述

负责系统权限体系与认证能力：
- 用户管理（CRUD、状态控制）
- 角色管理（CRUD、权限分配）
- 菜单管理（树形结构、权限标识）
- 登录认证（Token 生成与校验）

#### 数据表

| 表名 | 说明 |
|------|------|
| sys_user | 系统用户表 |
| sys_role | 角色表 |
| sys_menu | 菜单表 |
| sys_user_role | 用户角色关联表 |
| sys_role_menu | 角色菜单关联表 |

#### 目录结构

```
auth/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
├── vo/
├── convert/
└── enums/
```

#### 接口清单（声明）

| 接口名 | 路径 | 请求方式 | 说明 |
|--------|------|----------|------|
| 用户登录 | /auth/login | POST | - |
| 获取当前用户 | /auth/current-user | GET | - |
| 用户CRUD | /users | GET/POST/PUT/DELETE | - |
| 角色CRUD | /roles | GET/POST/PUT/DELETE | - |
| 菜单CRUD | /menus | GET/POST/PUT/DELETE | - |
| 获取用户菜单树 | /menus/tree | GET | - |
| 获取字段权限 | /permissions/field | GET | **跨模块接口** |
| 获取数据权限范围 | /permissions/data-scope | GET | **跨模块接口** |

> **说明**：以上接口为声明，具体实现由开发者在系分文档中填充。

---

#### 3.2.2 organization（组织架构管理）

#### 职责描述

负责组织架构与基础主数据管理：
- 部门管理（树形结构、层级控制）
- 职位管理（CRUD、职级范围）
- 字典管理（类型与数据）
- 部门树查询（跨模块接口）

#### 数据表

| 表名 | 说明 |
|------|------|
| sys_dept | 部门表 |
| sys_post | 职位表 |
| sys_dict_type | 字典类型表 |
| sys_dict_data | 字典数据表 |

#### 目录结构

```
organization/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
├── vo/
├── convert/
└── enums/
```

#### 接口清单（声明）

| 接口名 | 路径 | 请求方式 | 说明 |
|--------|------|----------|------|
| 部门CRUD | /departments | GET/POST/PUT/DELETE | - |
| 获取部门树 | /departments/tree | GET | **跨模块接口** |
| 职位CRUD | /posts | GET/POST/PUT/DELETE | - |
| 字典类型CRUD | /dict-types | GET/POST/PUT/DELETE | - |
| 字典数据CRUD | /dict-data | GET/POST/PUT/DELETE | - |
| 按类型获取字典 | /dict-data/type/{dictType} | GET | - |

> **说明**：以上接口为声明，具体实现由开发者在系分文档中填充。

---

#### 3.2.3 file（附件管理）

#### 职责描述

负责附件文件管理：
- 文件上传（支持多种存储方式）
- 文件下载
- 文件预览
- 文件元数据管理

#### 数据表

| 表名 | 说明 |
|------|------|
| sys_file | 文件表 |

#### 目录结构

```
file/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
├── vo/
└── convert/
```

#### 接口清单（声明）

| 接口名 | 路径 | 请求方式 | 说明 |
|--------|------|----------|------|
| 文件上传 | /files/upload | POST | - |
| 文件下载 | /files/{id}/download | GET | - |
| 文件删除 | /files/{id} | DELETE | - |
| 文件元数据查询 | /files/{id} | GET | - |

> **说明**：以上接口为声明，具体实现由地基搭建者实现。

---

#### 3.2.4 log（日志管理）

#### 职责描述

负责系统日志管理：
- 操作日志（自动记录、查询）
- 登录日志（自动记录、查询）

#### 数据表

| 表名 | 说明 |
|------|------|
| sys_operate_log | 操作日志表 |
| sys_login_log | 登录日志表 |

#### 目录结构

```
log/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
├── vo/
└── enums/
```

#### 接口清单（声明）

| 接口名 | 路径 | 请求方式 | 说明 |
|--------|------|----------|------|
| 操作日志查询 | /logs/operate | GET | - |
| 登录日志查询 | /logs/login | GET | - |

> **说明**：以上接口为声明，具体实现由地基搭建者实现。日志记录通过 AOP 切面自动完成，无需业务代码手动调用。

---

### 3.3 hrms-business（业务域）

#### 3.3.1 employee（员工档案管理）

#### 职责描述

负责员工档案全生命周期管理：
- 员工主档（CRUD、状态流转）
- 工号生成（规则：4位年份+2位部门编码+3位流水序号）
- 员工合同管理
- 调岗记录管理
- 变更历史追溯

#### 数据表

| 表名 | 说明 |
|------|------|
| hr_employee | 员工主档表 |
| hr_employee_contract | 员工合同表 |
| hr_employee_transfer_record | 员工调岗记录表 |

#### 目录结构

```
employee/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
├── vo/
├── convert/
└── enums/
```

#### 接口清单（声明）

| 接口名 | 路径 | 请求方式 | 说明 |
|--------|------|----------|------|
| 员工CRUD | /employees | GET/POST/PUT/DELETE | - |
| 获取员工简要信息 | /employees/brief/{id} | GET | **跨模块接口** |
| 生成工号 | /employees/gen-no | POST | **跨模块接口** |
| 获取员工完整档案 | /employees/full/{id} | GET | **跨模块接口** |
| 按部门获取员工列表 | /employees/by-department/{departmentId} | GET | **跨模块接口** |
| 员工合同管理 | /employees/{id}/contracts | GET/POST/PUT/DELETE | - |
| 调岗记录查询 | /employees/{id}/transfers | GET | - |

> **说明**：以上接口为声明，具体实现由开发者在系分文档中填充。

---

#### 3.3.2 personnel（入转调离流程）

#### 职责描述

负责员工入职、转正、调岗、离职全流程管理：
- 入职管理（申请、审批、入职确认）
- 转正管理（试用期到期提醒、转正审批）
- 调岗管理（申请、审批、生效）
- 离职管理（申请、审批、离职确认）

#### 数据表

| 表名 | 说明 |
|------|------|
| hr_entry_application | 入职申请表（待建） |
| hr_regular_application | 转正申请表（待建） |
| hr_transfer_application | 调岗申请表（待建） |
| hr_leave_application | 离职申请表（待建） |

> **注意**：以上业务表需开发者在系分阶段设计并补充 SQL，且必须包含审批状态字段（见 5.4 待建表设计约束）。

#### 目录结构

```
personnel/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
├── vo/
├── convert/
└── enums/
```

#### 接口清单（声明）

| 接口名 | 路径 | 请求方式 | 说明 |
|--------|------|----------|------|
| 入职申请CRUD | /entry-applications | GET/POST/PUT/DELETE | - |
| 转正申请CRUD | /regular-applications | GET/POST/PUT/DELETE | - |
| 调岗申请CRUD | /transfer-applications | GET/POST/PUT/DELETE | - |
| 离职申请CRUD | /leave-applications | GET/POST/PUT/DELETE | - |

> **说明**：以上接口为声明，具体实现由开发者在系分文档中填充。

---

#### 3.3.3 attendance（考勤管理）

#### 职责描述

负责员工考勤管理：
- 打卡记录（上下班打卡）
- 请假管理（请假申请、审批）
- 考勤统计（月度汇总）
- 异常处理（迟到、早退、旷工）

#### 数据表

| 表名 | 说明 |
|------|------|
| hr_attendance_record | 考勤打卡记录表（待建） |
| hr_leave_request | 请假申请表（待建） |
| hr_attendance_summary | 考勤汇总表（待建） |

> **注意**：以上业务表需开发者在系分阶段设计并补充 SQL。其中 hr_leave_request 必须包含审批状态字段（见 5.4 待建表设计约束）。

#### 目录结构

```
attendance/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
├── vo/
├── convert/
└── enums/
```

#### 接口清单（声明）

| 接口名 | 路径 | 请求方式 | 说明 |
|--------|------|----------|------|
| 打卡记录 | /attendance/records | GET/POST | - |
| 请假申请CRUD | /leave-requests | GET/POST/PUT/DELETE | - |
| 获取员工考勤汇总 | /attendance/summary/{employeeId}/{yearMonth} | GET | **跨模块接口** |
| 考勤统计查询 | /attendance/statistics | GET | - |

> **说明**：以上接口为声明，具体实现由开发者在系分文档中填充。

---

#### 3.3.4 salary（薪资管理）

#### 职责描述

负责员工薪资管理：
- 薪资账套管理（账套定义、项目配置）
- 薪资核算（月度计算、批次管理）
- 工资条（生成、查看）
- 发放记录

#### 数据表

| 表名 | 说明 |
|------|------|
| hr_salary_account | 薪资账套表（待建） |
| hr_salary_item | 薪资项目表（待建） |
| hr_salary_batch | 薪资批次表（待建） |
| hr_salary_detail | 薪资明细表（待建） |

> **注意**：以上业务表需开发者在系分阶段设计并补充 SQL。

#### 目录结构

```
salary/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
├── vo/
├── convert/
└── enums/
```

#### 接口清单（声明）

| 接口名 | 路径 | 请求方式 | 说明 |
|--------|------|----------|------|
| 薪资账套CRUD | /salary-accounts | GET/POST/PUT/DELETE | - |
| 获取员工薪资档案 | /salary/account/{employeeId} | GET | **跨模块接口** |
| 薪资核算 | /salary/calculate | POST | - |
| 工资条查询 | /salary/payslips | GET | - |

> **说明**：以上接口为声明，具体实现由开发者在系分文档中填充。

---

#### 3.3.5 approval（审批中心）

#### 职责描述

负责统一审批流程管理：
- 审批引擎（流程定义、节点配置）
- 待办列表（当前用户待处理任务）
- 已办列表（已处理任务）
- 审批委托（委托他人处理）
- 审批回调（业务模块回调处理）

#### 数据表

| 表名 | 说明 |
|------|------|
| hr_approval_instance | 审批实例表 |
| hr_approval_task | 审批任务表 |

#### 目录结构

```
approval/
├── controller/
├── service/
├── service/impl/
├── mapper/
├── entity/
├── dto/
├── vo/
├── convert/
├── enums/
└── handler/                     # 审批回调处理器
    └── ApprovalCallbackHandler.java
```

#### 接口清单（声明）

| 接口名 | 路径 | 请求方式 | 说明 |
|--------|------|----------|------|
| 发起审批任务 | /approval/start | POST | **跨模块接口** |
| 获取待审批数量 | /approval/pending-count | GET | **跨模块接口** |
| 待办列表 | /approval/pending | GET | - |
| 已办列表 | /approval/done | GET | - |
| 审批处理 | /approval/tasks/{taskId}/approve | POST | - |

> **说明**：以上接口为声明，具体实现由开发者在系分文档中填充。

---

#### 3.3.6 mycenter（个人中心）

#### 职责描述

负责员工个人事务管理：
- 我的档案（个人信息查看）
- 我的考勤（考勤记录、请假记录）
- 我的薪资（工资条查看）
- 我的申请（申请记录查询）
- 账号安全（修改密码、个人信息维护）

#### 数据表

| 表名 | 说明 |
|------|------|
| 无独立表 | 聚合展示各模块数据 |

#### 目录结构

```
mycenter/
├── controller/
├── service/
├── service/impl/
├── dto/
├── vo/
└── enums/
```

#### 接口清单（声明）

| 接口名 | 路径 | 请求方式 | 说明 |
|--------|------|----------|------|
| 我的档案 | /my/profile | GET | - |
| 我的考勤 | /my/attendance | GET | - |
| 我的薪资 | /my/salary | GET | - |
| 我的申请 | /my/applications | GET | - |
| 修改密码 | /my/password | PUT | - |

> **说明**：以上接口为声明，具体实现由开发者在系分文档中填充。mycenter 模块主要聚合展示其他模块数据，不独立管理数据表。

---

### 3.4 hrms-server（启动聚合模块）

#### 职责描述

启动与装配模块，仅负责：
- Spring Boot 应用启动
- 配置装配
- 模块扫描与依赖聚合

#### 目录结构

```
hrms-server/
└── src/main/
    ├── java/com/hrms/server/
    │   ├── HrmsServerApplication.java    # 启动类
    │   └── config/                       # 启动配置
    └── resources/
        ├── application.yaml              # 主配置
        ├── application-dev.yaml          # 开发环境配置
        └── application-prod.yaml         # 生产环境配置
```

#### 启动类配置

```java
@SpringBootApplication(scanBasePackages = {
    "com.hrms.common",
    "com.hrms.system",
    "com.hrms.business"
})
public class HrmsServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrmsServerApplication.class, args);
    }
}
```

> **约束**：`hrms-server` 不承载具体业务逻辑、Mapper、Service 实现和领域实体。

---

## 四、核心跨模块接口

> 以下接口为模块间协作的最小契约，各模块开发时必须遵守。具体实现由开发者在系分文档中填充。

### 4.1 接口清单

| 序号 | 接口名 | 路径 | 请求方式 | 最小必填入参 | 最小必有返回字段 | 提供方 | 调用方 |
|------|--------|------|----------|---------------|------------------|--------|--------|
| 1 | 获取员工简要信息 | /employees/brief/{id} | GET | id (Path) | id, name, employeeNo, departmentId, departmentName, employmentStatus | employee | attendance, salary, approval, mycenter |
| 2 | 生成工号 | /employees/gen-no | POST | departmentId | employeeNo | employee | personnel |
| 3 | 获取部门树 | /departments/tree | GET | 无（基于 Token 自动过滤） | [{id, name, parentId, children}] | organization | employee, personnel, attendance, salary, approval |
| 4 | 发起审批任务 | /approval/start | POST | bizType, bizId, applicantId | taskId | approval | personnel |
| 5 | 审批回调 | /approval/callback | POST | taskId, result, remark | 无（只需返回成功状态） | 各业务模块 | approval |
| 6 | 获取员工完整档案 | /employees/full/{id} | GET | id (Path) | id, name, employeeNo, departmentId, departmentName, positionId, positionName, jobLevel, leaderId, leaderName, hireDate, employmentStatus, baseSalary | employee | personnel, salary, mycenter |
| 7 | 获取员工考勤汇总 | /attendance/summary/{employeeId}/{yearMonth} | GET | employeeId, yearMonth | employeeId, yearMonth, workDays, actualWorkDays, leaveDays, lateCount, earlyLeaveCount, absentDays, overtimeHours | attendance | salary |
| 8 | 获取员工薪资档案 | /salary/account/{employeeId} | GET | employeeId | employeeId, salaryAccountId, salaryAccountName, baseSalary, probationSalaryRatio | salary | employee, personnel, mycenter |
| 9 | 按部门获取员工列表 | /employees/by-department/{departmentId} | GET | departmentId | departmentId, departmentName, employees[{id, name, employeeNo, positionName, employmentStatus}] | employee | attendance, salary |
| 10 | 获取待审批数量 | /approval/pending-count | GET | 无（基于Token自动识别当前用户） | count, details[{bizType, count}] | approval | mycenter |
| 11 | 获取字段权限 | /permissions/field | GET | 无（基于 Token） | viewableFields, editableFields, flowRequiredFields | auth | 所有模块 |
| 12 | 获取数据权限范围 | /permissions/data-scope | GET | 无（基于 Token） | scopeType, departmentIds | auth | 所有模块 |

> **说明**：序号 1-4 为原 01 文档定义接口，序号 5-12 为本次新增。

### 4.2 审批回调处理器设计

审批完成后，审批中心调用各业务模块的回调接口。建议采用统一抽象模式：

#### 接口定义

```java
/**
 * 审批回调处理器接口
 * 各业务模块实现此接口处理审批结果
 */
public interface ApprovalCallbackHandler {

    /**
     * 获取处理器支持的业务类型
     */
    String getBizType();

    /**
     * 处理审批回调
     *
     * @param taskId    审批任务ID
     * @param bizId     业务主键ID
     * @param result    审批结果：1-通过，2-驳回
     * @param remark    审批意见
     */
    void handle(Long taskId, Long bizId, Integer result, String remark);
}
```

#### 业务模块实现示例

```java
/**
 * 入职审批回调处理器
 */
@Component
public class EntryApprovalHandler implements ApprovalCallbackHandler {

    @Override
    public String getBizType() {
        return "ENTRY";
    }

    @Override
    public void handle(Long taskId, Long bizId, Integer result, String remark) {
        if (result == 1) {
            // 入职审批通过，更新入职申请状态
            // 调用 employee 模块生成工号、创建员工档案
        } else {
            // 入职审批驳回，更新申请状态
        }
    }
}
```

#### 调用流程

```
approval 模块
    ↓ 审批完成，调用 /approval/callback
各业务模块的 Handler 实现
    ↓ 根据 bizType 路由到对应 Handler
具体业务处理逻辑
```

---

## 五、数据库表清单

### 5.1 hrms-system 表

| 模块 | 表名 | 说明 |
|------|------|------|
| auth | sys_user | 系统用户表 |
| auth | sys_role | 角色表 |
| auth | sys_menu | 菜单表 |
| auth | sys_user_role | 用户角色关联表 |
| auth | sys_role_menu | 角色菜单关联表 |
| organization | sys_dept | 部门表 |
| organization | sys_post | 职位表 |
| organization | sys_dict_type | 字典类型表 |
| organization | sys_dict_data | 字典数据表 |
| file | sys_file | 文件表 |
| log | sys_operate_log | 操作日志表 |
| log | sys_login_log | 登录日志表 |

### 5.2 hrms-business 表

| 模块 | 表名 | 说明 |
|------|------|------|
| employee | hr_employee | 员工主档表 |
| employee | hr_employee_contract | 员工合同表 |
| employee | hr_employee_transfer_record | 员工调岗记录表 |
| personnel | hr_entry_application | 入职申请表（待建） |
| personnel | hr_regular_application | 转正申请表（待建） |
| personnel | hr_transfer_application | 调岗申请表（待建） |
| personnel | hr_leave_application | 离职申请表（待建） |
| attendance | hr_attendance_record | 考勤打卡记录表（待建） |
| attendance | hr_leave_request | 请假申请表（待建） |
| attendance | hr_attendance_summary | 考勤汇总表（待建） |
| salary | hr_salary_account | 薪资账套表（待建） |
| salary | hr_salary_item | 薪资项目表（待建） |
| salary | hr_salary_batch | 薪资批次表（待建） |
| salary | hr_salary_detail | 薪资明细表（待建） |
| approval | hr_approval_instance | 审批实例表 |
| approval | hr_approval_task | 审批任务表 |
| mycenter | - | 无独立表 |

### 5.3 公共字段约定

所有业务表必须包含以下公共字段（继承 BaseEntity）：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `id` | BIGINT UNSIGNED | 主键 |
| `create_by` | BIGINT UNSIGNED | 创建人 |
| `create_time` | DATETIME | 创建时间 |
| `update_by` | BIGINT UNSIGNED | 更新人 |
| `update_time` | DATETIME | 更新时间 |
| `is_deleted` | TINYINT(1) | 逻辑删除标记，默认 0 |

### 5.4 待建表设计约束

以下待建表必须遵守额外约束：

#### 申请类表（personnel 模块）

| 表名 | 必须包含字段 |
|------|--------------|
| hr_entry_application | approval_status, approval_instance_id |
| hr_regular_application | approval_status, approval_instance_id |
| hr_transfer_application | approval_status, approval_instance_id |
| hr_leave_application | approval_status, approval_instance_id |

#### 请假申请表（attendance 模块）

| 表名 | 必须包含字段 |
|------|--------------|
| hr_leave_request | approval_status, approval_instance_id |

#### 字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `approval_status` | TINYINT | 审批状态，使用 ApprovalStatusEnum 枚举值（0-草稿、1-审批中、2-已通过、3-已驳回、4-已撤回） |
| `approval_instance_id` | BIGINT | 关联审批实例ID，关联 hr_approval_instance.id |

> **说明**：
> - 已有 SQL 文件保持不变，详见 `backend/sql/schema/` 目录
> - 标注"待建"的表，由开发者在系分阶段设计并补充 SQL
> - 待建表必须遵守上述设计约束

---

## 六、附录

### 6.1 地基搭建前置任务

> 以下任务由地基搭建者**优先实现**，其他成员开发业务前必须确认已完成。

#### 6.1.1 hrms-common 必须实现的配置类

| 类名 | 路径 | 作用 | 状态 |
|------|------|------|------|
| MyMetaObjectHandler.java | com.hrms.common.handler | MyBatis-Plus 自动填充，填充 createBy/createTime/updateBy/updateTime | 必须 |
| MybatisPlusConfig.java | com.hrms.common.config | MyBatis-Plus 配置，启用逻辑删除（is_deleted = 0） | 必须 |
| CorsConfig.java | com.hrms.server.config | 跨域配置，允许前端调用后端接口 | 必须 |

#### 6.1.2 hrms-system.auth 必须实现的工具类

| 类名 | 路径 | 作用 | 负责人 | 状态 |
|------|------|------|--------|------|
| JwtUtils.java | com.hrms.system.auth.util | JWT 生成与解析，提供 generateToken() 和 parseToken() 方法 | A 同学 | 必须 |

> **协作约定**：
> - A 同学实现 JwtUtils 后，在群内通知其他成员
> - 其他成员解析 Token 时，统一调用 `JwtUtils.parseToken()` 或 `AuthService.parseToken()`
> - 禁止各模块单独实现 JWT 工具类，避免 Token 互相不认

#### 6.1.3 启动前检查清单

业务开发开始前，确认以下项：

- [ ] hrms-common 的 MyMetaObjectHandler 已实现，insert 自动填充生效
- [ ] hrms-common 的 MybatisPlusConfig 已配置，逻辑删除生效（deleteById 变 UPDATE）
- [ ] hrms-server 的 CorsConfig 已配置，前端可正常调用后端
- [ ] hrms-system.auth 的 JwtUtils 已实现，Token 生成与解析可用

---

### 6.2 hrms-common 现有文件清单（保留不变）

**统一返回体**
- Result.java
- PageResult.java

**全局异常处理**
- GlobalException.java
- ErrorCode.java
- GlobalExceptionHandler.java

**枚举类（13个）**
- BaseEnum.java
- EmployeeStatusEnum.java
- GenderEnum.java
- HireTypeEnum.java
- ContractTypeEnum.java
- LeaveTypeEnum.java
- ApprovalStatusEnum.java
- TaskStatusEnum.java
- ApproveResultEnum.java
- DataScopeEnum.java
- MenuTypeEnum.java
- SalaryBatchStatusEnum.java
- AttendanceStatusEnum.java
- BizTypeEnum.java

**其他基础类**
- annotation/ - 自定义注解
- aop/ - AOP切面
- config/ - 公共配置
- constant/ - 常量定义
- handler/ - MyBatis-Plus 自动填充
- security/ - 安全相关
- util/ - 工具类

### 6.3 配置文件（保留不变）

**应用配置**
- application.yaml - 主配置（Redis、RabbitMQ、MyBatis-Plus）
- application-dev.yaml - 开发环境配置
- application-prod.yaml - 生产环境配置

**启动类**
- HrmsServerApplication.java

---

## 八、权限矩阵

> 本节定义各模块的菜单级、页面级、数据范围级、字段级权限规则，作为后端数据权限控制和前端菜单显示的统一依据。

### 8.1 角色定义

| 角色标识 | 角色名称 | 数据范围 | 核心职责 |
|----------|----------|----------|----------|
| `ADMIN` | 系统管理员 | 全平台 | 系统配置、角色管理、数据备份 |
| `HR` | HR 专员 | 全部员工 | 员工管理、薪资核算、考勤管理、审批管理 |
| `MANAGER` | 部门主管 | 本部门及下属 | 本部门员工查看、下属审批 |
| `FINANCE` | 财务专员 | 薪资相关 | 薪资审核、成本报表查看 |
| `EMPLOYEE` | 普通员工 | 仅本人 | 个人信息、请假申请、工资条查看 |

> **说明**：角色数据已在 `init_admin.sql` 中初始化，`data_scope` 字段值：1-本人、2-本部门、3-本部门及子部门、4-全部。

### 8.2 侧边栏菜单可见性

| 菜单项 | 系统管理员 | HR 专员 | 部门主管 | 财务专员 | 普通员工 |
|--------|:---:|:---:|:---:|:---:|:---:|
| 🏠 首页 | ✓ | ✓ | ✓ | ✓ | ✓ |
| 🔧 系统管理 | ✓ | ✗ | ✗ | ✗ | ✗ |
| 👥 员工档案 | ✓ | ✓ | ✓(本部门) | ✗ | ✗ |
| 📋 入转调离 | ✓ | ✓ | ✓ | ✗ | ✗ |
| ⏰ 考勤管理 | ✓ | ✓ | ✓(本部门) | ✗ | ✗ |
| 💰 薪资管理 | ✓ | ✓ | ✗ | ✓ | ✗ |
| ✅ 审批中心 | ✓ | ✓ | ✓ | ✓ | ✗ |
| 👤 个人中心 | ✓ | ✓ | ✓ | ✓ | ✓(唯一入口) |

> **注**：普通员工侧边栏只显示 **首页** 和 **个人中心**，所有业务操作通过个人中心进入。

### 8.3 业务模块权限矩阵

#### 8.3.1 模块权限总览

| 业务模块 | 系统管理员 | HR 专员 | 部门主管 | 财务专员 | 普通员工 |
|----------|:---:|:---:|:---:|:---:|:---:|
| **权限体系(auth)** | ✅ 全功能 | ❌ | ❌ | ❌ | ❌ |
| **组织架构(organization)** | ✅ 全功能 | ✅ 只读 | ❌ | ❌ | ❌ |
| **员工档案(employee)** | ✅ 全功能 | ✅ 全功能 | ✅ 本部门 | ❌ | ❌ |
| **入转调离(personnel)** | ✅ 全功能 | ✅ 全功能 | ✅ 审批+调岗 | ❌ | ❌ |
| **考勤管理(attendance)** | ✅ 全功能 | ✅ 全功能 | ✅ 本部门 | ❌ | ✅ 仅自己(个人中心) |
| **薪资管理(salary)** | ✅ 全功能 | ✅ 全功能 | ❌ | ✅ 全功能 | ✅ 仅自己(个人中心) |
| **审批中心(approval)** | ✅ 全功能 | ✅ 全功能 | ✅ 本部门 | ✅ 薪资审批 | ❌ |
| **个人中心(mycenter)** | ✅ 全功能 | ✅ 全功能 | ✅ 全功能 | ✅ 全功能 | ✅ 全功能 |

#### 8.3.2 权限体系（auth）

| 维度 | 系统管理员 | HR 专员 | 部门主管 | 财务专员 | 普通员工 |
|------|:---:|:---:|:---:|:---:|:---:|
| **菜单入口可见** | ✓ | ✗ | ✗ | ✗ | ✗ |
| **页面可访问** | ✓ | ✗ | ✗ | ✗ | ✗ |
| **数据范围** | 全平台 | — | — | — | — |
| **权限标识** | `system:user:*`<br>`system:role:*`<br>`system:menu:*` | — | — | — | — |

> **特殊说明**：仅系统管理员可管理用户、角色、菜单。

#### 8.3.3 组织架构（organization）

| 维度 | 系统管理员 | HR 专员 | 部门主管 | 财务专员 | 普通员工 |
|------|:---:|:---:|:---:|:---:|:---:|
| **菜单入口可见** | ✓ | ✓ | ✗ | ✗ | ✗ |
| **页面可访问** | ✓ | ✓(只读) | ✗ | ✗ | ✗ |
| **可编辑** | ✓ | ✗ | ✗ | ✗ | ✗ |
| **数据范围** | 全部 | 全部(只读) | — | — | — |
| **权限标识** | `organization:dept:*`<br>`organization:post:*`<br>`organization:dict:*` | `organization:dept:list`<br>`organization:post:list` | — | — | — |

> **特殊说明**：HR 专员需要查看部门/职位信息用于员工档案录入，但无增删改权限。部门树接口供其他模块调用，不受菜单权限限制。

#### 8.3.4 员工档案（employee）

| 维度 | 系统管理员 | HR 专员 | 部门主管 | 财务专员 | 普通员工 |
|------|:---:|:---:|:---:|:---:|:---:|
| **菜单入口可见** | ✓ | ✓ | ✓ | ✗ | ✗ |
| **页面可访问** | ✓ | ✓ | ✓ | ✗ | ✗ |
| **数据范围** | 全部员工 | 全部员工 | 本部门及下属 | — | — |
| **可编辑** | ✓ | ✓ | ✗ | ✗ | ✗ |
| **可删除** | ✓ | ✓(逻辑删除) | ✗ | ✗ | ✗ |
| **权限标识** | `employee:*` | `employee:list`<br>`employee:create`<br>`employee:edit`<br>`employee:delete` | `employee:list`<br>`employee:query` | — | — |

**字段级权限**：

| 字段 | HR 专员 | 部门主管 | 普通员工(本人) |
|------|:---:|:---:|:---:|
| 姓名、工号、部门、职位、手机、邮箱 | 可见 | 可见 | 可见 |
| **身份证号** | **可见** | **不可见** | **仅自己可见** |
| **薪资信息** | **可见** | **不可见** | **仅自己可见** |
| **紧急联系人** | **可见** | **不可见** | **仅自己可见** |
| 合同信息 | 可见 | 不可见 | 不可见 |

#### 8.3.5 入转调离（personnel）

| 维度 | 系统管理员 | HR 专员 | 部门主管 | 财务专员 | 普通员工 |
|------|:---:|:---:|:---:|:---:|:---:|
| **菜单入口可见** | ✓ | ✓ | ✓ | ✗ | ✗ |
| **页面可访问** | ✓ | ✓ | ✓ | ✗ | ✗ |
| **发起入职** | ✓ | ✓ | ✗ | ✗ | ✗ |
| **发起转正** | ✓ | ✓ | ✗ | ✗ | ✗ |
| **发起调岗** | ✓ | ✓ | ✓(下属) | ✗ | ✗ |
| **发起离职** | ✓ | ✓ | ✗ | ✗ | ✗ |
| **审批入职/转正/离职** | ✓ | ✓ | ✓(下属) | ✗ | ✗ |
| **数据范围** | 全部 | 全部 | 本部门及下属 | — | — |
| **权限标识** | `personnel:*` | `personnel:entry:create`<br>`personnel:regular:create`<br>`personnel:leave:create`<br>`personnel:*:approve` | `personnel:transfer:create`<br>`personnel:*:approve` | — | — |

> **特殊说明**：部门主管可发起调岗申请（将下属调至其他部门），但不能发起入职/离职/转正（由 HR 发起）。

#### 8.3.6 考勤管理（attendance）

| 维度 | 系统管理员 | HR 专员 | 部门主管 | 财务专员 | 普通员工 |
|------|:---:|:---:|:---:|:---:|:---:|
| **菜单入口可见** | ✓ | ✓ | ✓ | ✗ | ✗ |
| **页面可访问** | ✓ | ✓ | ✓ | ✗ | ✗(个人中心) |
| **数据范围** | 全部 | 全部 | 本部门 | — | 仅本人 |
| **打卡记录查看** | 全部 | 全部 | 本部门 | — | 仅本人 |
| **考勤统计查看** | 全部 | 全部 | 本部门 | — | 仅本人 |
| **请假审批** | ✓ | ✓ | ✓(下属) | ✗ | ✗ |
| **考勤规则配置** | ✓ | ✓ | ✗ | ✗ | ✗ |
| **权限标识** | `attendance:*` | `attendance:record:list`<br>`attendance:leave:approve`<br>`attendance:summary:list` | `attendance:record:list`<br>`attendance:leave:approve` | — | `attendance:leave:create`(本人) |

> **特殊说明**：普通员工的考勤数据通过「个人中心→我的考勤」查看，不进入考勤管理模块。

#### 8.3.7 薪资管理（salary）

| 维度 | 系统管理员 | HR 专员 | 部门主管 | 财务专员 | 普通员工 |
|------|:---:|:---:|:---:|:---:|:---:|
| **菜单入口可见** | ✓ | ✓ | ✗ | ✓ | ✗ |
| **页面可访问** | ✓ | ✓ | ✗ | ✓ | ✗(个人中心) |
| **数据范围** | 全部 | 全部 | — | 全部 | 仅本人 |
| **薪资账套管理** | ✓ | ✓ | ✗ | ✓ | ✗ |
| **薪资核算** | ✓ | ✓(发起) | ✗ | ✓(审核) | ✗ |
| **工资条管理** | ✓ | ✓(发放) | ✗ | ✓(审核) | ✗(仅查看自己) |
| **成本报表** | ✓ | ✓ | ✗ | ✓ | ✗ |
| **权限标识** | `salary:*` | `salary:accounts:*`<br>`salary:calculate:execute`<br>`salary:payslip:list` | — | `salary:accounts:list`<br>`salary:calculate:execute`<br>`salary:payslip:list` | `salary:payslip:view`(本人) |

**薪资数据可见性**：

| 数据 | 系统管理员 | HR 专员 | 财务专员 | 部门主管 | 普通员工 |
|------|:---:|:---:|:---:|:---:|:---:|
| 全员薪资明细 | — | ✓ | ✓ | ✗ | ✗ |
| 本人薪资 | ✓ | ✓ | ✓ | ✓ | ✓ |

> **特殊说明**：普通员工通过「个人中心→我的薪资」查看自己的工资条，需二次验证。

#### 8.3.8 审批中心（approval）

| 维度 | 系统管理员 | HR 专员 | 部门主管 | 财务专员 | 普通员工 |
|------|:---:|:---:|:---:|:---:|:---:|
| **菜单入口可见** | ✓ | ✓ | ✓ | ✓ | ✗ |
| **页面可访问** | ✓ | ✓ | ✓ | ✓ | ✗(个人中心) |
| **待办列表** | 全部待办 | 全部待办 | 本部门待办 | 薪资批次待办 | — |
| **已办列表** | 全部已办 | 全部已办 | 本部门已办 | 薪资批次已办 | — |
| **审批操作** | ✓ | ✓ | ✓(下属) | ✓(薪资批次) | ✗ |
| **委托审批** | ✓ | ✓ | ✓ | ✓ | ✗ |
| **权限标识** | `approval:*` | `approval:task:list`<br>`approval:task:approve` | `approval:task:list`<br>`approval:task:approve` | `approval:task:list`<br>`approval:task:approve` | — |

**各角色审批类型**：

| 角色 | 可审批类型 |
|------|------------|
| HR 专员 | 入职审批、转正审批、调岗审批、离职审批 |
| 部门主管 | 下属请假审批、下属转正审批、下属调岗审批、下属离职审批 |
| 财务专员 | 薪资批次审批 |
| 系统管理员 | 全部(兜底) |

> **特殊说明**：普通员工通过「个人中心→我的申请」查看自己的申请进度，不进入审批中心模块。

#### 8.3.9 个人中心（mycenter）

| 页面 | 系统管理员 | HR 专员 | 部门主管 | 财务专员 | 普通员工 |
|------|:---:|:---:|:---:|:---:|:---:|
| **我的档案** | ✓(本人) | ✓(本人) | ✓(本人) | ✓(本人) | ✓(本人) |
| **我的考勤** | ✓ | ✓ | ✓ | ✓ | ✓ |
| **我的薪资** | ✓ | ✓ | ✓ | ✓ | ✓(需二次验证) |
| **我的申请** | ✓ | ✓ | ✓ | ✓ | ✓ |
| **账号安全** | ✓ | ✓ | ✓ | ✓ | ✓ |
| **权限标识** | `mycenter:*` | `mycenter:*` | `mycenter:*` | `mycenter:*` | `mycenter:profile:view`<br>`mycenter:salary:view`<br>`mycenter:attendance:view` |

> **特殊说明**：个人中心是所有角色的统一入口，普通员工的核心操作区域。普通员工在「我的档案」中查看的敏感字段（身份证、薪资等）仅对自己可见。

### 8.4 权限标识规范

#### 8.4.1 标识符命名规范

遵循 **`{模块}:{资源}:{操作}`** 三段式格式：

| 段落 | 说明 | 示例 |
|------|------|------|
| 模块 | 对应后端模块名 | `system`、`employee`、`salary` |
| 资源 | 对应具体业务实体 | `user`、`employee`、`salary` |
| 操作 | 对应操作类型 | `list`、`create`、`edit`、`delete`、`approve` |

#### 8.4.2 完整权限标识清单

| 模块 | 权限标识 | 说明 | 分配角色 |
|------|----------|------|----------|
| **auth** | `system:user:list` | 查看用户列表 | ADMIN |
| | `system:user:create` | 创建用户 | ADMIN |
| | `system:user:edit` | 编辑用户 | ADMIN |
| | `system:user:delete` | 删除用户 | ADMIN |
| | `system:role:list` | 查看角色列表 | ADMIN |
| | `system:role:create` | 创建角色 | ADMIN |
| | `system:role:edit` | 编辑角色 | ADMIN |
| | `system:role:delete` | 删除角色 | ADMIN |
| | `system:menu:list` | 查看菜单列表 | ADMIN |
| | `system:menu:create` | 创建菜单 | ADMIN |
| | `system:menu:edit` | 编辑菜单 | ADMIN |
| | `system:menu:delete` | 删除菜单 | ADMIN |
| **organization** | `organization:dept:list` | 查看部门列表 | ADMIN、HR |
| | `organization:dept:create` | 创建部门 | ADMIN |
| | `organization:dept:edit` | 编辑部门 | ADMIN |
| | `organization:dept:delete` | 删除部门 | ADMIN |
| | `organization:post:list` | 查看职位列表 | ADMIN、HR |
| | `organization:dict:list` | 查看字典列表 | ADMIN、HR |
| **employee** | `employee:list` | 查看员工列表 | ADMIN、HR、MANAGER |
| | `employee:query` | 查看员工详情 | ADMIN、HR、MANAGER、EMPLOYEE(本人) |
| | `employee:create` | 创建员工 | ADMIN、HR |
| | `employee:edit` | 编辑员工 | ADMIN、HR |
| | `employee:delete` | 删除员工 | ADMIN、HR |
| | `employee:contract:list` | 查看合同 | ADMIN、HR |
| | `employee:transfer:list` | 查看调岗记录 | ADMIN、HR、MANAGER |
| **personnel** | `personnel:entry:create` | 发起入职 | ADMIN、HR |
| | `personnel:entry:approve` | 审批入职 | ADMIN、HR、MANAGER |
| | `personnel:regular:create` | 发起转正 | ADMIN、HR |
| | `personnel:transfer:create` | 发起调岗 | ADMIN、HR、MANAGER |
| | `personnel:leave:create` | 发起离职 | ADMIN、HR |
| **attendance** | `attendance:record:list` | 查看打卡记录 | ADMIN、HR、MANAGER |
| | `attendance:record:checkin` | 执行打卡 | ALL |
| | `attendance:leave:create` | 发起请假 | ALL |
| | `attendance:leave:approve` | 审批请假 | ADMIN、HR、MANAGER |
| | `attendance:summary:list` | 查看考勤统计 | ADMIN、HR、MANAGER |
| **salary** | `salary:accounts:list` | 查看薪资账套 | ADMIN、HR、FINANCE |
| | `salary:accounts:create` | 创建账套 | ADMIN、HR |
| | `salary:calculate:execute` | 执行核算 | ADMIN、HR、FINANCE |
| | `salary:payslip:list` | 查看工资条 | ADMIN、HR、FINANCE、EMPLOYEE(本人) |
| **approval** | `approval:task:list` | 查看待办列表 | ADMIN、HR、MANAGER、FINANCE |
| | `approval:task:approve` | 审批操作 | ADMIN、HR、MANAGER、FINANCE |
| | `approval:task:delegate` | 委托审批 | ADMIN、HR、MANAGER、FINANCE |
| **mycenter** | `mycenter:profile:view` | 查看个人档案 | ALL |
| | `mycenter:salary:view` | 查看个人薪资 | ALL(EMPLOYEE需二次验证) |
| | `mycenter:attendance:view` | 查看个人考勤 | ALL |
| | `mycenter:password:edit` | 修改密码 | ALL |

### 8.5 数据权限范围

| data_scope 值 | 含义 | 适用角色 |
|---------------|------|----------|
| 1 | 仅本人 | EMPLOYEE、FINANCE |
| 2 | 本部门 | — |
| 3 | 本部门及子部门 | MANAGER |
| 4 | 全部数据 | ADMIN、HR |

### 8.6 菜单初始化数据

> 菜单数据需在 `backend/sql/data/` 目录下新增 `init_menu.sql` 文件初始化。

#### 8.6.1 一级菜单目录

| ID | 菜单名称 | 菜单类型 | 路径 | 权限标识 | 排序 |
|----|----------|----------|------|----------|------|
| 1 | 首页 | 1(目录) | /home | - | 1 |
| 2 | 系统管理 | 1(目录) | /system | system | 2 |
| 3 | 员工档案 | 1(目录) | /employee | employee | 3 |
| 4 | 入转调离 | 1(目录) | /process | process | 5 |
| 5 | 考勤管理 | 1(目录) | /attendance | attendance | 6 |
| 6 | 薪资管理 | 1(目录) | /salary | salary | 7 |
| 7 | 审批中心 | 1(目录) | /approval | approval | 8 |
| 8 | 个人中心 | 1(目录) | /profile | - | 9 |

#### 8.6.2 二级菜单页面（示例）

**系统管理（parent_id=2）**

| ID | 菜单名称 | 菜单类型 | 路径 | 权限标识 |
|----|----------|----------|------|----------|
| 21 | 用户管理 | 2(菜单) | /system/user | system:user |
| 22 | 角色管理 | 2(菜单) | /system/role | system:role |
| 23 | 菜单管理 | 2(菜单) | /system/menu | system:menu |

**员工档案（parent_id=3）**

| ID | 菜单名称 | 菜单类型 | 路径 | 权限标识 |
|----|----------|----------|------|----------|
| 31 | 员工列表 | 2(菜单) | /employee/list | employee:list |
| 32 | 合同管理 | 2(菜单) | /employee/contract | employee:contract |

**入转调离（parent_id=4）**

| ID | 菜单名称 | 菜单类型 | 路径 | 权限标识 |
|----|----------|----------|------|----------|
| 41 | 入职管理 | 2(菜单) | /process/entry | personnel:entry |
| 42 | 转正管理 | 2(菜单) | /process/regular | personnel:regular |
| 43 | 调岗管理 | 2(菜单) | /process/transfer | personnel:transfer |
| 44 | 离职管理 | 2(菜单) | /process/leave | personnel:leave |

**考勤管理（parent_id=5）**

| ID | 菜单名称 | 菜单类型 | 路径 | 权限标识 |
|----|----------|----------|------|----------|
| 51 | 打卡记录 | 2(菜单) | /attendance/record | attendance:record |
| 52 | 请假管理 | 2(菜单) | /attendance/leave | attendance:leave |
| 53 | 考勤统计 | 2(菜单) | /attendance/summary | attendance:summary |

**薪资管理（parent_id=6）**

| ID | 菜单名称 | 菜单类型 | 路径 | 权限标识 |
|----|----------|----------|------|----------|
| 61 | 薪资账套 | 2(菜单) | /salary/account | salary:account |
| 62 | 薪资核算 | 2(菜单) | /salary/calculate | salary:calculate |
| 63 | 工资条 | 2(菜单) | /salary/payslip | salary:payslip |

**审批中心（parent_id=7）**

| ID | 菜单名称 | 菜单类型 | 路径 | 权限标识 |
|----|----------|----------|------|----------|
| 71 | 待办任务 | 2(菜单) | /approval/pending | approval:task |
| 72 | 已办任务 | 2(菜单) | /approval/done | approval:task |

**个人中心（parent_id=8）**

| ID | 菜单名称 | 菜单类型 | 路径 | 权限标识 |
|----|----------|----------|------|----------|
| 81 | 我的档案 | 2(菜单) | /profile/info | mycenter:profile |
| 82 | 我的考勤 | 2(菜单) | /profile/attendance | mycenter:attendance |
| 83 | 我的薪资 | 2(菜单) | /profile/salary | mycenter:salary |
| 84 | 我的申请 | 2(菜单) | /profile/application | mycenter:application |

#### 8.6.3 按钮级权限（示例）

**用户管理（parent_id=21）**

| ID | 菜单名称 | 菜单类型 | 权限标识 |
|----|----------|----------|----------|
| 211 | 查询 | 3(按钮) | system:user:list |
| 212 | 新增 | 3(按钮) | system:user:create |
| 213 | 编辑 | 3(按钮) | system:user:edit |
| 214 | 删除 | 3(按钮) | system:user:delete |

> **说明**：完整的菜单和按钮权限初始化数据，需在开发阶段根据实际情况补充到 `init_menu.sql` 文件中。

---

## 九、版本记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2026-07-09 | 整合 01、02 文档，重新组织为八业务模块架构 |
| v1.1 | 2026-07-09 | 修复缺陷：file/log 移至 hrms-system，补充 BaseEntity、枚举规范、异常规范、认证上下文规范、待建表约束、权限接口 |
| v1.2 | 2026-07-09 | 修复缺陷：补充 MyBatis-Plus 自动填充规范、file/log 专属错误码段 |
| v1.3 | 2026-07-09 | 补充地基搭建前置任务：MyMetaObjectHandler、MybatisPlusConfig、CorsConfig、JwtUtils |
| v1.4 | 2026-07-10 | 新增第八章权限矩阵：角色定义、菜单可见性、模块权限矩阵、权限标识清单、数据权限范围、菜单初始化数据（与前端规范对齐） |

# HRMS 全局地基搭建规范

> **文档状态**：正式版
> **生效日期**：2026-07-09
> **适用范围**：HRMS 后端开发团队
> **文档定位**：独立执行手册，成员按此文档搭建地基并进行后续开发
> > **冲突处理说明**：当 `01-HRMS全局开发规范` 与 `02-HRMS全局地基搭建规范` 存在冲突时，
> **以02规范为准**。

---

## 1. 文档目标与适用范围

### 1.1 文档目标

本文档用于指导：
- **Claude**：搭建全局地基（目录结构、公共组件、配置文件、SQL文件）
- **成员**：了解Claude搭了什么、后续如何开发

### 1.2 适用范围

- 后端所有模块：`hrms-common`、`hrms-system`、`hrms-business`、`hrms-server`
- 前后端接口联调
- 数据库建设

### 1.3 地基搭建完成标准

成员可以进行自己模块的开发，包括：
- 知道代码放哪个包
- 知道接口怎么写
- 知道跨模块调用规范
- 知道需要的全局常量

---

## 2. 目录结构规范

### 2.1 Maven模块结构

```
backend
├── pom.xml                    # 父工程
├── hrms-common/               # 公共模块
├── hrms-system/               # 系统管理域
├── hrms-business/             # 业务域
└── hrms-server/               # 启动聚合模块
```

**模块职责**：

| 模块 | 职责 | 禁止事项 |
|------|------|----------|
| `hrms-common` | 公共基础能力（返回体、异常、枚举、工具类） | 禁止反向依赖业务模块 |
| `hrms-system` | 系统底座（用户、角色、部门、员工档案） | 禁止依赖 business |
| `hrms-business` | 核心业务（入转调离、考勤、薪资、审批） | - |
| `hrms-server` | 启动装配，不承载业务逻辑 | 禁止写 Controller/Service/Mapper |

**依赖关系**：
```
hrms-server → hrms-business → hrms-system → hrms-common
```

### 2.2 各模块包结构骨架

Claude 搭建骨架，成员填充具体类。

#### hrms-common 包结构

```
com.hrms.common
├── web/           # 统一返回体
├── exception/     # 全局异常
├── enums/         # 公共枚举
├── util/          # 工具类
├── config/        # 公共配置
└── constant/      # 常量
```

#### hrms-system 包结构

```
com.hrms.system
├── controller/    # 接口层
├── service/       # 业务层接口
│   └── impl/      # 业务层实现
├── mapper/        # 数据访问层
├── entity/        # 数据库实体
├── dto/           # 入参DTO
├── vo/            # 出参VO
├── convert/       # 对象转换
└── config/        # 模块配置
```

#### hrms-business 包结构

```
com.hrms.business
├── controller/
├── service/
│   └── impl/
├── mapper/
├── entity/
├── dto/
├── vo/
├── convert/
└── config/
```

#### hrms-server 包结构

```
com.hrms.server
└── config/        # 启动配置
```

### 2.3 包命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | 大驼峰 | `EmployeeController`、`EmployeeService` |
| 方法名 | 小驼峰 | `getUserById`、`createEmployee` |
| 变量名 | 小驼峰 | `employeeList`、`totalCount` |
| 常量 | 全大写下划线 | `MAX_RETRY_COUNT` |
| 包名 | 全小写 | `com.hrms.system.controller` |

---

## 3. 公共组件清单

### 3.1 统一返回体 Result

**位置**：`com.hrms.common.web.Result`

**字段**：
- `code`：int，0=成功，非0=异常
- `message`：String，响应消息
- `data`：T，业务数据

**静态方法**：
```java
Result.success()                    // 成功，无数据
Result.success(T data)              // 成功，返回数据
Result.failure(ErrorCode errorCode) // 失败，使用错误码
Result.failure(int code, String message) // 失败，自定义
```

### 3.2 全局异常处理

**位置**：
- `com.hrms.common.exception.GlobalException`
- `com.hrms.common.handler.GlobalExceptionHandler`

**使用方式**：
```java
// 抛出业务异常
if (employee == null) {
    throw new GlobalException(ErrorCode.EMPLOYEE_NOT_FOUND);
}

// 全局异常处理器自动捕获并转换
// 返回格式：{"code": 60001, "message": "员工不存在", "data": null}
```

### 3.3 错误码枚举

**位置**：`com.hrms.common.exception.ErrorCode`

**码段划分**：

| 码段 | 类型 | 说明 |
|------|------|------|
| `0` | 成功 | - |
| `40001-40099` | 参数错误 | 参数缺失、格式错误 |
| `40100-40199` | 认证授权错误 | 未登录、无权限 |
| `50001-50099` | 系统内部错误 | 数据库异常、缓存异常 |
| `60001-60999` | 业务逻辑错误 | 各模块业务异常 |

**业务码段分配**：
- `60001-60099`：档案模块
- `60100-60199`：组织模块
- `60200-60299`：入离职模块
- `60300-60399`：考勤模块
- `60400-60499`：薪资模块
- `60500-60599`：审批模块

### 3.4 业务枚举

**位置**：`com.hrms.common.enums`

所有枚举均实现 `BaseEnum` 接口，使用数字编码（对应数据库 TINYINT 类型）。

**已有枚举**：

| 枚举类 | 说明 | 编码值 |
|--------|------|--------|
| `EmployeeStatusEnum` | 员工状态 | 1-试用期(PROBATION), 2-正式(FORMAL), 3-待离职(PENDING_LEAVE), 4-已离职(LEFT) |
| `GenderEnum` | 性别 | 1-男(MALE), 2-女(FEMALE) |
| `HireTypeEnum` | 入职类型 | 1-全职(FULL_TIME), 2-兼职(PART_TIME), 3-实习(INTERN) |
| `ContractTypeEnum` | 合同类型 | 1-固定期限(FIXED_TERM), 2-无固定期限(NON_FIXED_TERM), 3-劳务合同(LABOR) |
| `LeaveTypeEnum` | 请假类型 | 1-年假, 2-病假, 3-事假, 4-婚假, 5-产假, 6-丧假, 7-调休 |
| `ApprovalStatusEnum` | 审批状态 | 0-草稿(DRAFT), 1-审批中(APPROVING), 2-已通过(APPROVED), 3-已驳回(REJECTED), 4-已撤回(WITHDRAWN) |
| `TaskStatusEnum` | 审批任务状态 | 0-待处理(PENDING), 1-已处理(PROCESSED), 2-已转交(TRANSFERRED) |
| `ApproveResultEnum` | 审批结果 | 1-通过(APPROVED), 2-驳回(REJECTED), 3-转交(TRANSFERRED) |
| `DataScopeEnum` | 数据权限范围 | 1-本人(SELF), 2-本部门(DEPT), 3-本部门及子部门(DEPT_AND_CHILD), 4-全部(ALL) |
| `MenuTypeEnum` | 菜单类型 | 1-目录(DIR), 2-菜单(MENU), 3-按钮(BUTTON) |
| `SalaryBatchStatusEnum` | 薪资批次状态 | 0-草稿, 1-计算中, 2-待确认, 3-已通过, 4-已发放, 5-已驳回 |
| `AttendanceStatusEnum` | 考勤状态 | 0-正常, 1-迟到, 2-早退, 3-旷工, 4-缺卡, 5-请假 |
| `BizTypeEnum` | 审批业务类型 | 字符串编码：ONBOARDING, TRANSFER, DIMISSION, LEAVE, ATTENDANCE_RECTIFY, SALARY_APPROVAL |

**数据库字段类型约定**：

所有使用数字编码枚举的数据库字段，必须定义为 `TINYINT` 类型，而非 `VARCHAR`。

| 表名 | 字段 | 数据库类型 | 对应枚举 |
|------|------|------------|----------|
| `hr_employee` | `employment_status` | TINYINT | EmployeeStatusEnum |
| `hr_employee` | `gender` | TINYINT | GenderEnum |
| `hr_employee` | `hire_type` | TINYINT | HireTypeEnum |
| `hr_employee` | `contract_type` | TINYINT | ContractTypeEnum |
| `hr_approval_instance` | `approval_status` | TINYINT | ApprovalStatusEnum |
| `hr_approval_task` | `task_status` | TINYINT | TaskStatusEnum |
| `hr_approval_task` | `approve_result` | TINYINT | ApproveResultEnum |
| `sys_role` | `data_scope` | TINYINT | DataScopeEnum |

### 3.5 使用示例

```java
// Controller 返回示例
@GetMapping("/employees/{id}")
public Result<EmployeeVO> getEmployee(@PathVariable Long id) {
    EmployeeVO vo = employeeService.getById(id);
    return Result.success(vo);
}

// 异常抛出示例
if (employee == null) {
    throw new GlobalException(ErrorCode.EMPLOYEE_NOT_FOUND);
}
```

---

## 4. 配置文件规范

### 4.1 父模块配置

**位置**：`hrms-server/src/main/resources/application.yaml`

**已配置项**：

```yaml
spring:
  application:
    name: hrms-server
  
  # Redis 配置（已配置好，成员无需修改）
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 3000ms
  
  # RabbitMQ 配置（已配置好，成员无需修改）
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: ${RABBITMQ_VHOST:/}

# MyBatis-Plus 配置（已配置好，成员无需修改）
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.hrms
  configuration:
    map-underscore-to-camel-case: true  # 下划线转驼峰
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 4.2 数据源配置模板

**说明**：数据源连接由成员自己配置。

**模板**（在 `application-dev.yaml` 或环境变量中配置）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DB:hrms}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 4.3 配置文件位置

| 文件 | 用途 | 由谁配置 |
|------|------|----------|
| `application.yaml` | 公共配置（Redis、RabbitMQ、MyBatis-Plus） | Claude 已配好 |
| `application-dev.yaml` | 开发环境配置 | 成员配置数据源 |
| `application-prod.yaml` | 生产环境配置 | 部署时配置 |

---

## 5. 接口规范

### 5.1 RESTful接口规范

**HTTP方法语义**：

| 方法 | 用途 | 示例 |
|------|------|------|
| `GET` | 查询 | `GET /employees/{id}` |
| `POST` | 新增、复杂查询 | `POST /employees` |
| `PUT` | 全量更新 | `PUT /employees/{id}` |
| `PATCH` | 部分更新、状态变更 | `PATCH /employees/{id}/status` |
| `DELETE` | 逻辑删除 | `DELETE /employees/{id}` |

**URL规范**：
- 使用名词复数：`/employees`、`/departments`
- 路径参数：`/employees/{id}`
- 查询参数：`/employees?name=张三&status=1`

### 5.2 返回体格式

**成功响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1001,
    "name": "张三"
  }
}
```

**错误响应**：
```json
{
  "code": 40001,
  "message": "参数不能为空",
  "data": null
}
```

### 5.3 分页规范

**请求参数**：
- `pageNum`：页码（从1开始）
- `pageSize`：每页大小

**响应格式**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 5.4 接口示例

```java
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @GetMapping("/{id}")
    public Result<EmployeeVO> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }
    
    @PostMapping
    public Result<Long> create(@RequestBody @Valid EmployeeCreateDTO dto) {
        return Result.success(employeeService.create(dto));
    }
    
    @GetMapping
    public Result<PageResult<EmployeeVO>> page(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        EmployeeQueryDTO query
    ) {
        return Result.success(employeeService.page(pageNum, pageSize, query));
    }
}
```

---

## 6. 测试规范

### 6.1 测试类型要求

| 测试类型 | 覆盖层 | 要求 |
|----------|--------|------|
| 单元测试 | Service 层 | 必须，目标覆盖率 80%+ |
| 集成测试 | Controller 层 | 必须 |

### 6.2 测试命名规范

**测试类命名**：`XxxTest` 或 `XxxIT`（集成测试）

**测试方法命名**：`methodName_scenario_expectedBehavior()`

**示例**：
- `getById_existingEmployee_returnsEmployee()`
- `create_invalidParam_throwsException()`

### 6.3 测试示例

```java
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    
    @Mock
    private EmployeeMapper employeeMapper;
    
    private EmployeeService employeeService;
    
    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeMapper);
    }
    
    @Test
    @DisplayName("根据ID查询员工-存在-返回员工信息")
    void getById_existingEmployee_returnsEmployee() {
        // given
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("张三");
        when(employeeMapper.selectById(1L)).thenReturn(employee);
        
        // when
        EmployeeVO result = employeeService.getById(1L);
        
        // then
        assertThat(result.getName()).isEqualTo("张三");
    }
}
```

---

## 7. 提交规范

### 7.1 分支管理

| 分支 | 用途 | 说明 |
|------|------|------|
| `master` | 公共主分支 | 稳定代码，合并需代码审查 |
| `LiShiHao` | 个人开发分支 | - |
| `GaoSiJun` | 个人开发分支 | - |

### 7.2 Commit Message格式

采用 Conventional Commits 格式：

```
<type>(<scope>): <subject>

<body>
```

**类型（type）**：

| 类型 | 用途 | 是否影响版本号 |
|------|------|----------------|
| `feat` | 新增功能 | 是 |
| `fix` | 修复Bug | 是 |
| `refactor` | 重构 | 否 |
| `docs` | 文档更新 | 否 |
| `test` | 测试相关 | 否 |
| `chore` | 构建/工具变动 | 否 |

### 7.3 提交示例

```
feat(employee): 新增员工档案创建与详情查询能力

- 新增员工主档实体与查询接口
- 接入部门、职位与汇报人关联校验
- 补充员工工号生成规则与数据校验
```

---

## 8. SQL文件规范

### 8.1 SQL文件位置

```
backend/sql/
├── schema/              # 建表SQL
│   ├── sys_*.sql        # 系统模块表
│   └── hr_*.sql         # 业务模块表
└── data/                # 初始化数据
    └── init_*.sql
```

### 8.2 表命名规范

| 模块 | 表前缀 | 示例表 |
|------|--------|--------|
| hrms-common | 无 | 无（不建表） |
| hrms-system | `sys_` | `sys_user`, `sys_role`, `sys_dept` |
| hrms-business | `hr_` | `hr_employee`, `hr_approval_instance` |

### 8.3 表归属模块说明

**hrms-system 模块表**：
- `sys_user` - 系统用户表
- `sys_role` - 角色表
- `sys_user_role` - 用户角色关联表
- `sys_menu` - 菜单表
- `sys_role_menu` - 角色菜单关联表
- `sys_dept` - 部门表
- `sys_post` - 职位表
- `sys_dict_type` - 字典类型表
- `sys_dict_data` - 字典数据表
- `sys_file` - 文件表
- `sys_operate_log` - 操作日志表
- `sys_login_log` - 登录日志表

**hrms-business 模块表**：
- `hr_employee` - 员工主档表
- `hr_employee_contract` - 员工合同表
- `hr_employee_transfer_record` - 员工调岗记录表
- `hr_approval_instance` - 审批实例表
- `hr_approval_task` - 审批任务表

### 8.4 跨模块契约字段

**关键外键关系**：

| 外键字段 | 关联表 | 说明 |
|----------|--------|------|
| `hr_employee.user_id` | `sys_user.id` | 员工关联系统用户 |
| `hr_employee.dept_id` | `sys_dept.id` | 员工关联部门 |
| `hr_employee.post_id` | `sys_post.id` | 员工关联职位 |
| `hr_approval_instance.applicant_user_id` | `sys_user.id` | 审批申请人 |
| `hr_approval_instance.applicant_employee_id` | `hr_employee.id` | 审批申请人员工 |

**说明**：
- 跨模块表关联通过外键实现
- 修改契约字段需同步更新相关模块
- 契约字段变更需通知全员

---

## 9. 附录

### 9.1 依赖清单

项目已引入以下依赖，成员无需额外配置：

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.16 | 基础框架 |
| MyBatis-Plus | 3.5.12 | ORM框架 |
| Redis | - | 缓存 |
| RabbitMQ | - | 消息队列 |
| MySQL Connector | - | 数据库驱动 |
| Lombok | - | 代码简化 |

### 9.2 快速开始

**成员开发第一步**：
1. 拉取最新代码
2. 配置数据源（见 4.2 节）
3. 启动 `HrmsServerApplication`
4. 在对应模块包下创建类

**验证地基搭建成功**：
1. 项目能正常启动
2. 访问 `http://localhost:8080/actuator/health` 返回 UP
3. 能正常连接数据库

---

## 10. 版本记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2026-07-09 | 初版，基于头脑风暴会议确定的核心需求编写 |
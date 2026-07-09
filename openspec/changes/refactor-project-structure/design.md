## 上下文

### 背景

HRMS 项目需要从当前的占位结构重构为符合 `03-HRMS模块划分规范.md` 的标准架构。当前状态：
- hrms-common：缺少核心基础设施（BaseEntity、SecurityContextHolder、MyMetaObjectHandler）
- hrms-system：只有 3 个占位文件，未按子模块组织
- hrms-business：只有 3 个占位文件，未按子模块组织
- hrms-server：缺少跨域配置

### 当前状态

| 模块 | 现状 | 目标 |
|------|------|------|
| hrms-common | 18 个文件（返回体、异常、枚举） | 新增 5 个基础设施文件 |
| hrms-system | 3 个占位文件 | 4 个子模块目录结构 |
| hrms-business | 3 个占位文件 + 空子模块目录 | 6 个子模块目录结构 |
| hrms-server | 启动类 + 配置文件 | 新增 CorsConfig |

### 约束

- 不修改现有代码（Result、PageResult、异常处理、枚举类）
- 不修改 SQL 文件
- 不修改 pom.xml 和 application.yaml
- 必须符合四人团队协作分工

### 利益相关者

- 地基搭建者：负责 hrms-common + file + log 实现
- 成员 A：依赖 auth + organization 目录结构
- 成员 B：依赖 employee + personnel 目录结构
- 成员 C：依赖 attendance + salary 目录结构
- 成员 D：依赖 approval + mycenter 目录结构

## 目标 / 非目标

### 目标

1. **提供公共基础设施**：BaseEntity、SecurityContextHolder、MyMetaObjectHandler、MybatisPlusConfig
2. **建立模块目录规范**：10 个子模块的标准分层结构
3. **支持前后端分离开发**：提供 CORS 配置
4. **启用自动化审计**：公共字段自动填充、逻辑删除

### 非目标

1. 不实现具体业务逻辑（由各成员后续开发）
2. 不修改数据库结构（SQL 文件保持不变）
3. 不修改现有代码（Result、PageResult、异常处理、枚举类）
4. 不实现 JWT 工具类（由成员 A 负责）

## 决策

### 决策 1：BaseEntity 继承模式

**选择**：所有业务实体继承 BaseEntity

**理由**：
- 统一公共字段管理
- 支持 MyBatis-Plus 自动填充
- 避免重复定义公共字段

**替代方案**：
- 使用 MyBatis-Plus 注解单独标注每个实体 → 字段重复，维护困难

### 决策 2：SecurityContextHolder 基于 ThreadLocal

**选择**：使用 ThreadLocal 存储用户上下文

**理由**：
- 线程隔离，避免并发问题
- 与 Spring Security 机制兼容
- 简单易用，无需额外依赖

**替代方案**：
- 使用 Spring Security Context → 引入额外依赖，增加复杂度
- 使用 RequestAttribute → 需要每次传递 HttpServletRequest

### 决策 3：MyMetaObjectHandler 放置位置

**选择**：放在 hrms-common.handler 包

**理由**：
- 公共能力，所有模块共用
- 避免 Bean 冲突（全局只有一个 MetaObjectHandler）
- 与 SecurityContextHolder 配合，自动获取当前用户

**替代方案**：
- 放在各业务模块 → Bean 冲突，需要条件装配
- 放在 hrms-server → 违反"server 不承载逻辑"原则

### 决策 4：目录结构创建方式

**选择**：创建空目录结构，不生成占位类

**理由**：
- 避免生成无用代码
- 各成员根据需要创建类
- 保持目录结构清晰

**替代方案**：
- 生成占位接口 → 增加维护负担，无实际价值

## 风险 / 权衡

### 风险 1：ThreadLocal 内存泄漏

**风险**：如果请求结束后未清理 ThreadLocal，可能导致内存泄漏

**缓解措施**：
- 在拦截器的 afterCompletion 中清理 ThreadLocal
- 使用 try-finally 确保 finally 块中清理

### 风险 2：自动填充时用户未登录

**风险**：定时任务或异步线程中调用 insert，无法获取当前用户

**缓解措施**：
- SecurityContextHolder.getUserId() 返回 null 时，createBy 设为 null 或系统默认值
- 定时任务使用系统账号登录上下文

### 风险 3：CORS 配置过于宽松

**风险**：允许所有来源可能导致安全问题

**缓解措施**：
- 开发环境允许所有来源
- 生产环境通过配置文件限制允许的来源

### 权衡：目录结构 vs 占位类

**权衡**：创建空目录结构 vs 生成占位接口

**选择理由**：
- 空目录结构更清晰
- 避免无用代码
- 各成员按需创建

## 迁移计划

### 步骤 1：删除占位文件

```bash
# hrms-system
rm -rf hrms-system/src/main/java/com/hrms/system/controller
rm -rf hrms-system/src/main/java/com/hrms/system/service
rm -rf hrms-system/src/main/java/com/hrms/system/convert
rm -rf hrms-system/src/main/java/com/hrms/system/domain
rm -rf hrms-system/src/main/java/com/hrms/system/dto
rm -rf hrms-system/src/main/java/com/hrms/system/entity
rm -rf hrms-system/src/main/java/com/hrms/system/enums
rm -rf hrms-system/src/main/java/com/hrms/system/mapper
rm -rf hrms-system/src/main/java/com/hrms/system/vo

# hrms-business
rm -rf hrms-business/src/main/java/com/hrms/business/controller
rm -rf hrms-business/src/main/java/com/hrms/business/service
rm -rf hrms-business/src/main/java/com/hrms/business/convert
rm -rf hrms-business/src/main/java/com/hrms/business/dto
rm -rf hrms-business/src/main/java/com/hrms/business/entity
rm -rf hrms-business/src/main/java/com/hrms/business/mapper
rm -rf hrms-business/src/main/java/com/hrms/business/vo
rm -rf hrms-business/src/main/java/com/hrms/business/approval
rm -rf hrms-business/src/main/java/com/hrms/business/attendance
rm -rf hrms-business/src/main/java/com/hrms/business/config
rm -rf hrms-business/src/main/java/com/hrms/business/process
rm -rf hrms-business/src/main/java/com/hrms/business/profile
rm -rf hrms-business/src/main/java/com/hrms/business/salary
```

### 步骤 2：创建 hrms-common 新文件

| 文件 | 操作 |
|------|------|
| entity/BaseEntity.java | 新增 |
| security/SecurityContextHolder.java | 新增 |
| security/UserContext.java | 新增 |
| handler/MyMetaObjectHandler.java | 新增 |
| config/MybatisPlusConfig.java | 新增 |

### 步骤 3：创建 hrms-system 子模块目录

```
hrms-system/src/main/java/com/hrms/system/
├── auth/
│   ├── controller/
│   ├── service/
│   ├── service/impl/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   ├── vo/
│   ├── convert/
│   └── enums/
├── organization/
│   ├── controller/
│   ├── service/
│   ├── service/impl/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   ├── vo/
│   ├── convert/
│   └── enums/
├── file/
│   ├── controller/
│   ├── service/
│   ├── service/impl/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   ├── vo/
│   └── convert/
└── log/
    ├── controller/
    ├── service/
    ├── service/impl/
    ├── mapper/
    ├── entity/
    ├── dto/
    ├── vo/
    └── enums/
```

### 步骤 4：创建 hrms-business 子模块目录

```
hrms-business/src/main/java/com/hrms/business/
├── employee/
│   ├── controller/
│   ├── service/
│   ├── service/impl/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   ├── vo/
│   ├── convert/
│   └── enums/
├── personnel/
│   ├── controller/
│   ├── service/
│   ├── service/impl/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   ├── vo/
│   ├── convert/
│   └── enums/
├── attendance/
│   ├── controller/
│   ├── service/
│   ├── service/impl/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   ├── vo/
│   ├── convert/
│   └── enums/
├── salary/
│   ├── controller/
│   ├── service/
│   ├── service/impl/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   ├── vo/
│   ├── convert/
│   └── enums/
├── approval/
│   ├── controller/
│   ├── service/
│   ├── service/impl/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   ├── vo/
│   ├── convert/
│   ├── enums/
│   └── handler/
└── mycenter/
    ├── controller/
    ├── service/
    ├── service/impl/
    ├── dto/
    ├── vo/
    └── enums/
```

### 步骤 5：创建 hrms-server 配置

| 文件 | 操作 |
|------|------|
| config/CorsConfig.java | 新增 |

### 回滚策略

如果变更出现问题，可通过 Git 回滚：
```bash
git checkout -- .
git clean -fd
```

## 待解决问题

无（此变更为基础设施搭建，无待定决策）
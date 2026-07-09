## ADDED Requirements

### 需求:SecurityContextHolder 必须基于 ThreadLocal 实现

用户上下文持有者 `SecurityContextHolder` 必须使用 ThreadLocal 存储当前登录用户信息，确保线程隔离。

#### 场景:获取当前用户 ID

- **当** 业务代码调用 `SecurityContextHolder.getUserId()`
- **那么** 返回当前请求关联的用户 ID（Long 类型）

#### 场景:获取当前部门 ID

- **当** 业务代码调用 `SecurityContextHolder.getDeptId()`
- **那么** 返回当前用户所属部门 ID（Long 类型）

#### 场景:获取当前角色列表

- **当** 业务代码调用 `SecurityContextHolder.getRoleIds()`
- **那么** 返回当前用户拥有的角色 ID 列表（List<Long> 类型）

### 需求:UserContext 必须包含用户上下文信息

UserContext 类必须包含以下用户上下文信息：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户 ID |
| deptId | Long | 部门 ID |
| roleIds | List<Long> | 角色 ID 列表 |

#### 场景:UserContext 完整性

- **当** 查阅 UserContext 类定义
- **那么** 必须包含 userId、deptId、roleIds 字段

### 需求:必须统一使用 SecurityContextHolder

所有模块必须通过 `SecurityContextHolder` 获取当前用户信息，禁止自行解析 Token 或从其他途径获取用户信息。

#### 场景:禁止自行解析 Token

- **当** 业务代码需要获取当前用户 ID
- **那么** 必须调用 `SecurityContextHolder.getUserId()`，禁止自行解析 JWT Token

#### 场景:未登录时返回 null

- **当** 请求未携带有效 Token（如定时任务场景）
- **那么** `SecurityContextHolder.getUserId()` 返回 null，不抛出异常

### 需求:拦截器必须设置和清理用户上下文

登录拦截器必须在请求开始时设置用户上下文，在请求结束时清理用户上下文，避免 ThreadLocal 内存泄漏。

#### 场景:请求开始时设置上下文

- **当** 请求进入拦截器
- **那么** 解析 Token 并调用 `SecurityContextHolder.setContext(userContext)` 设置用户上下文

#### 场景:请求结束时清理上下文

- **当** 请求处理完成
- **那么** 调用 `SecurityContextHolder.clear()` 清理 ThreadLocal，避免内存泄漏
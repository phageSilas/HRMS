# HRMS 后端 API 接口文档

> 更新日期：2026-07-10
> 版本：v1.0
> 基础路径：`http://localhost:8080`
> 状态：✅ 已验证可用

## 重要说明

### 实体类映射配置

**所有实体类必须添加 `@TableName` 注解**，否则 MyBatis-Plus 会使用错误的表名。

| 实体类 | 表名 | 说明 |
|--------|------|------|
| UserDO | sys_user | ✅ 已配置 |
| RoleDO | sys_role | ✅ 已配置 |
| MenuDO | sys_menu | ✅ 已配置 |
| UserRoleDO | sys_user_role | ✅ 已配置 |
| RoleMenuDO | sys_role_menu | ✅ 已配置 |
| DeptDO | sys_dept | ✅ 已配置 |
| PostDO | sys_post | ✅ 已配置 |
| DictTypeDO | sys_dict_type | ✅ 已配置 |
| DictDataDO | sys_dict_data | ✅ 已配置 |
| FileDO | sys_file | ✅ 已配置 |
| OperateLogDO | sys_operate_log | ✅ 已配置 |
| LoginLogDO | sys_login_log | ✅ 已配置 |

### Spring Boot 3.x 注意事项

本项目使用 Spring Boot 3.5.16，使用 **Jakarta EE** 命名空间：
- ✅ 正确：`jakarta.servlet.http.HttpServletResponse`
- ❌ 错误：`javax.servlet.http.HttpServletResponse`

---

## 统一响应格式

所有接口返回统一的 `Result<T>` 格式：

```json
{
  "code": 0,
  "message": "操作成功",
  "data": { ... }
}
```

### 错误码定义

| 错误码范围 | 类型 | 说明 |
|-----------|------|------|
| 0 | 成功 | 操作成功 |
| 40001-40099 | 参数错误 | 参数缺失、格式错误 |
| 40100-40199 | 认证授权错误 | 未登录、无权限、Token 无效/过期 |
| 50001-50009 | 系统内部错误 | 数据库异常、缓存异常 |
| 50010-50019 | 附件模块错误 | 文件过大、格式不支持 |
| 50020-50029 | 日志模块错误 | 日志导出失败 |
| 60001-60999 | 业务逻辑异常 | 各业务模块错误 |

---

## 1. 认证接口 (AuthController)

**路径前缀**: `/auth`

### 1.1 用户登录

- **路径**: `POST /auth/login`
- **需要认证**: 否
- **请求体**:
```json
{
  "username": "admin",
  "password": "123456"
}
```
- **响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "admin",
    "realName": "管理员",
    "avatarUrl": "https://...",
    "roleIds": [1, 2],
    "permissions": ["system:user:list", "system:dept:list"]
  }
}
```

### 1.2 获取当前用户信息

- **路径**: `GET /auth/current-user`
- **需要认证**: 是
- **请求头**: `Authorization: Bearer <token>`
- **响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "admin",
    "realName": "管理员",
    "avatarUrl": "https://...",
    "deptId": 1,
    "deptName": "总部",
    "roleIds": [1, 2],
    "roleNames": ["管理员", "HR"],
    "permissions": ["system:user:list", "system:dept:list"]
  }
}
```

### 1.3 退出登录

- **路径**: `POST /auth/logout`
- **需要认证**: 是
- **响应**: `{ "code": 0, "message": "操作成功", "data": null }`

---

## 2. 部门接口 (DeptController)

**路径前缀**: `/departments`

### 2.1 查询部门树

- **路径**: `GET /departments/tree`
- **需要认证**: 是
- **响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "parentId": null,
      "deptName": "总公司",
      "deptCode": "HQ",
      "leaderUserId": 1,
      "deptLevel": 1,
      "path": "1",
      "sortNo": 1,
      "status": 1,
      "children": [
        {
          "id": 2,
          "parentId": 1,
          "deptName": "技术部",
          "deptCode": "TECH",
          "deptLevel": 2,
          "path": "1/2",
          "sortNo": 1,
          "status": 1,
          "children": []
        }
      ]
    }
  ]
}
```

### 2.2 创建部门

- **路径**: `POST /departments`
- **需要认证**: 是
- **请求体**:
```json
{
  "parentId": null,
  "deptName": "技术部",
  "deptCode": "TECH",
  "leaderUserId": 1,
  "sortNo": 1,
  "description": "技术部门",
  "status": 1
}
```
- **响应**: `{ "code": 0, "message": "操作成功", "data": 1 }` (返回部门 ID)

### 2.3 更新部门

- **路径**: `PUT /departments/{id}`
- **需要认证**: 是
- **请求体**: 同创建部门

### 2.4 删除部门

- **路径**: `DELETE /departments/{id}`
- **需要认证**: 是

### 2.5 查询部门详情

- **路径**: `GET /departments/{id}`
- **需要认证**: 是
- **响应**: 单个部门对象（无 children）

---

## 3. 职位接口 (PostController)

**路径前缀**: `/posts`

### 3.1 查询职位列表

- **路径**: `GET /posts`
- **需要认证**: 是
- **查询参数**:
  - `postName` - 职位名称（模糊查询，可选）
  - `postCode` - 职位编码（模糊查询，可选）
  - `status` - 状态：1启用 0禁用（可选）
- **响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "postName": "高级工程师",
      "postCode": "SENIOR_ENG",
      "sortNo": 1,
      "status": 1,
      "remark": "备注",
      "createTime": "2024-01-01T10:00:00"
    }
  ]
}
```

### 3.2 创建职位

- **路径**: `POST /posts`
- **需要认证**: 是
- **请求体**:
```json
{
  "postName": "高级工程师",
  "postCode": "SENIOR_ENG",
  "sortNo": 1,
  "status": 1,
  "remark": "备注"
}
```
- **响应**: `{ "code": 0, "message": "操作成功", "data": 1 }` (返回职位 ID)

### 3.3 更新职位

- **路径**: `PUT /posts/{id}`
- **需要认证**: 是
- **请求体**: 同创建职位

### 3.4 删除职位

- **路径**: `DELETE /posts/{id}`
- **需要认证**: 是

### 3.5 查询职位详情

- **路径**: `GET /posts/{id}`
- **需要认证**: 是

---

## 4. 字典数据接口 (DictDataController)

**路径前缀**: `/dict-data`

### 4.1 按类型编码查询字典数据（重要）

- **路径**: `GET /dict-data/type/{typeCode}`
- **需要认证**: 是
- **描述**: 根据字典类型编码获取所有字典数据（前端下拉框常用）
- **示例**: `GET /dict-data/type/GENDER`
- **响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "typeCode": "GENDER",
      "dictLabel": "男",
      "dictValue": "1",
      "sortNo": 1,
      "status": 1,
      "createTime": "2024-01-01T10:00:00"
    },
    {
      "id": 2,
      "typeCode": "GENDER",
      "dictLabel": "女",
      "dictValue": "2",
      "sortNo": 2,
      "status": 1,
      "createTime": "2024-01-01T10:00:00"
    }
  ]
}
```

### 4.2 查询字典数据列表

- **路径**: `GET /dict-data`
- **需要认证**: 是
- **查询参数**:
  - `typeCode` - 字典类型编码（可选）
  - `dictLabel` - 字典标签（可选）
  - `status` - 状态（可选）

### 4.3 创建字典数据

- **路径**: `POST /dict-data`
- **需要认证**: 是
- **请求体**:
```json
{
  "typeCode": "GENDER",
  "dictLabel": "男",
  "dictValue": "1",
  "sortNo": 1,
  "status": 1,
  "remark": "备注"
}
```

### 4.4 更新字典数据

- **路径**: `PUT /dict-data/{id}`
- **需要认证**: 是

### 4.5 删除字典数据

- **路径**: `DELETE /dict-data/{id}`
- **需要认证**: 是

---

## 5. 文件接口 (FileController)

**路径前缀**: `/files`

### 5.1 文件上传

- **路径**: `POST /files/upload`
- **Content-Type**: `multipart/form-data`
- **需要认证**: 是
- **请求参数**:
  - `file` - 文件（必填）
  - `bizType` - 业务类型（可选）
  - `bizId` - 业务 ID（可选）
- **响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 1,
    "fileName": "document.pdf",
    "filePath": "/uploads/2024/01/xxx.pdf",
    "fileSize": 102400,
    "fileType": "application/pdf",
    "uploadTime": "2024-01-01T10:00:00"
  }
}
```

### 5.2 文件下载

- **路径**: `GET /files/{id}/download`
- **需要认证**: 是
- **响应**: 文件流（`Content-Disposition: attachment`）

### 5.3 文件预览

- **路径**: `GET /files/{id}/preview`
- **需要认证**: 是
- **描述**: 支持图片、PDF 等可直接预览的文件类型
- **响应**: 文件字节流（`Content-Disposition: inline`）

### 5.4 查询文件详情

- **路径**: `GET /files/{id}`
- **需要认证**: 是

### 5.5 删除文件

- **路径**: `DELETE /files/{id}`
- **需要认证**: 是

---

## 6. 其他 Controller

项目中还包含以下 Controller（未在上述列表中）：

| Controller | 路径前缀 | 功能 |
|------------|---------|------|
| `UserController` | `/users` | 用户管理 |
| `RoleController` | `/roles` | 角色管理 |
| `MenuController` | `/menus` | 菜单管理 |
| `PermissionController` | `/permissions` | 权限管理 |
| `DictTypeController` | `/dict-types` | 字典类型管理 |
| `OperateLogController` | `/operate-logs` | 操作日志 |
| `LoginLogController` | `/login-logs` | 登录日志 |

---

## 认证机制

### 认证方式

- **JWT Token 认证**
- 请求头格式: `Authorization: Bearer <token>`
- Token 在登录时由 `/auth/login` 接口返回

### 拦截器配置

**排除路径**（无需认证）:
- `/auth/login` - 登录接口
- `/error` - 错误页面
- `/actuator/**` - 监控端点
- `/swagger-ui/**` - Swagger UI
- `/v3/api-docs/**` - OpenAPI 文档

**其他所有路径都需要认证**

---

## 使用示例

### cURL 示例

```bash
# 登录
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 获取当前用户（需要 Token）
curl -X GET http://localhost:8080/auth/current-user \
  -H "Authorization: Bearer <token>"

# 获取部门树
curl -X GET http://localhost:8080/departments/tree \
  -H "Authorization: Bearer <token>"

# 获取职位列表
curl -X GET http://localhost:8080/posts \
  -H "Authorization: Bearer <token>"

# 获取字典数据
curl -X GET http://localhost:8080/dict-data/type/GENDER \
  -H "Authorization: Bearer <token>"
```

### Postman Collection

建议使用 Postman 导入以下配置：

```json
{
  "info": {
    "name": "HRMS API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "认证",
      "item": [
        {
          "name": "登录",
          "request": {
            "method": "POST",
            "url": "http://localhost:8080/auth/login",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
              "mode": "raw",
              "raw": "{\"username\":\"admin\",\"password\":\"123456\"}"
            }
          }
        }
      ]
    }
  ]
}
```

---

## 联系方式

如有疑问，请联系：
- 后端负责人：成员 A
- 技术文档：`docs/03-HRMS模块划分规范.md`
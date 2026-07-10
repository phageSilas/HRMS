# HRMS 前端开发指南

## 目录

1. [Mock 数据使用](#mock-数据使用)
2. [角色切换开发](#角色切换开发)
3. [权限控制开发](#权限控制开发)
4. [新增页面开发流程](#新增页面开发流程)
5. [常见问题解答](#常见问题解答)

---

## Mock 数据使用

### Mock 数据位置

所有 Mock 数据文件位于 `mock/` 目录：

```
mock/
├── auth.ts    # 登录认证 Mock
├── user.ts    # 用户信息 Mock
└── home.ts    # 首页统计 Mock
```

### Mock 数据格式

Mock 数据遵循统一的返回体格式：

```typescript
// 成功响应
{
  code: 0,
  message: 'success',
  data: { ... }
}

// 失败响应
{
  code: 40101,
  message: '用户名或密码错误',
  data: null
}
```

### 如何新增 Mock 接口

1. 在对应的 Mock 文件中添加接口定义：

```typescript
// mock/employee.ts
export default {
  // 获取员工列表
  'GET /employees': (req, res) => {
    const { page = 1, size = 10 } = req.query;
    
    res.json({
      code: 0,
      message: 'success',
      data: {
        records: [...],  // 员工列表
        total: 100,      // 总数
        page: Number(page),
        size: Number(size),
      }
    });
  },
  
  // 获取员工详情
  'GET /employees/:id': (req, res) => {
    const { id } = req.params;
    
    res.json({
      code: 0,
      message: 'success',
      data: {
        id: Number(id),
        name: '张三',
        // ...
      }
    });
  },
};
```

2. 重启开发服务器（Mock 文件修改需要重启）

### Mock 数据开发技巧

#### 动态数据

使用变量存储状态，实现动态 Mock：

```typescript
let currentId = 1;
const employees = [];

export default {
  'POST /employees': (req, res) => {
    const employee = {
      id: currentId++,
      ...req.body,
      createTime: new Date().toISOString(),
    };
    employees.push(employee);
    
    res.json({
      code: 0,
      message: 'success',
      data: employee,
    });
  },
};
```

#### 模拟延迟

使用 `setTimeout` 模拟真实请求延迟：

```typescript
'GET /employees': (req, res) => {
  setTimeout(() => {
    res.json({
      code: 0,
      message: 'success',
      data: [...]
    });
  }, 300); // 延迟 300ms
},
```

#### 错误模拟

模拟接口错误，测试错误处理：

```typescript
'POST /employees': (req, res) => {
  // 模拟参数错误
  if (!req.body.name) {
    return res.json({
      code: 40001,
      message: '员工姓名不能为空',
      data: null,
    });
  }
  
  // 模拟业务错误
  if (req.body.phone && !/^1[3-9]\d{9}$/.test(req.body.phone)) {
    return res.json({
      code: 60001,
      message: '手机号格式不正确',
      data: null,
    });
  }
  
  res.json({
    code: 0,
    message: 'success',
    data: { id: 1, ...req.body },
  });
},
```

---

## 角色切换开发

### 角色权限映射

在 `mock/auth.ts` 中定义了角色权限映射：

```typescript
const rolePermissions: Record<string, string[]> = {
  ADMIN: ['system', 'employee', 'process', 'attendance', 'salary', 'approval', 'mycenter'],
  HR: ['employee', 'process', 'attendance', 'salary', 'approval', 'mycenter'],
  MANAGER: ['employee', 'process', 'attendance', 'approval', 'mycenter'],
  FINANCE: ['salary', 'approval', 'mycenter'],
  EMPLOYEE: ['mycenter'],
};
```

### 如何测试不同角色

#### 方法 1：登录页角色选择（推荐）

1. 启动开发服务器：`pnpm dev`
2. 访问 http://localhost:8000/login
3. 在角色下拉框中选择目标角色
4. 点击登录，系统会自动应用该角色的权限

#### 方法 2：手动修改 localStorage

```javascript
// 在浏览器控制台执行
localStorage.clear();
localStorage.setItem('token', 'mock-token-ADMIN');
localStorage.setItem('userInfo', JSON.stringify({
  userId: 1,
  username: 'admin',
  nickname: '管理员',
  roleCode: 'ADMIN',
  permissions: ['system', 'employee', 'process', 'attendance', 'salary', 'approval', 'mycenter']
}));
// 刷新页面
window.location.reload();
```

#### 方法 3：代码中模拟角色

在 `mock/auth.ts` 中修改默认角色：

```typescript
// 修改默认角色
let currentRole = 'HR'; // 改为 HR
```

### 角色切换注意事项

1. **开发环境专用**：角色选择功能仅在开发环境可见（`process.env.NODE_ENV === 'development'`）

2. **生产环境隐藏**：生产环境构建时，角色选择自动隐藏

3. **权限同步**：确保前端权限标识与后端 `sys_menu.permission` 字段一致

---

## 权限控制开发

### 权限控制层级

#### 1. 路由级权限

在 `.umirc.ts` 中通过 `access` 属性控制：

```typescript
{
  path: '/system',
  name: '系统管理',
  access: 'system', // 仅 'system' 权限可见
  routes: [
    { path: '/system/user', name: '用户管理', component: '@/pages/system/user' },
    // ...
  ],
}
```

#### 2. 菜单级权限

在 `src/constants/menu.ts` 中定义菜单权限：

```typescript
export const menuConfig: MenuItem[] = [
  {
    key: 'system',
    name: '系统管理',
    path: '/system',
    access: 'system', // 权限标识
    children: [...]
  },
];
```

#### 3. 组件级权限

在组件中使用 `useAccess` 钩子：

```typescript
import { useAccess } from '@umijs/max';

function MyComponent() {
  const access = useAccess();
  
  return (
    <div>
      {access.system && <Button>系统管理</Button>}
      {access.hasPermission('employee') && <Button>员工管理</Button>}
    </div>
  );
}
```

#### 4. 按钮级权限

封装 `Access` 组件：

```typescript
import { Access, useAccess } from '@umijs/max';

function MyPage() {
  const access = useAccess();
  
  return (
    <div>
      <Access accessible={access.system}>
        <Button>删除用户</Button>
      </Access>
    </div>
  );
}
```

### 如何新增权限

#### 步骤 1：后端添加权限标识

在后端数据库 `sys_menu` 表中添加菜单记录，设置 `permission` 字段：

```sql
INSERT INTO sys_menu (name, permission, ...)
VALUES ('报表管理', 'report', ...);
```

#### 步骤 2：前端定义权限常量

在 `src/constants/permissions.ts` 中添加：

```typescript
export const PERMISSIONS = {
  // ...现有权限
  REPORT: 'report',
} as const;
```

#### 步骤 3：更新权限规则

在 `src/access.ts` 中添加权限判断：

```typescript
export default function access(initialState: { currentUser?: UserInfo }) {
  const permissions = currentUser?.permissions || [];
  
  return {
    // ...现有权限
    report: permissions.includes('report'),
  };
}
```

#### 步骤 4：路由配置权限

在 `.umirc.ts` 中添加路由并设置权限：

```typescript
{
  path: '/report',
  name: '报表管理',
  icon: 'bar-chart',
  access: 'report', // 新增权限
  routes: [
    { path: '/report', redirect: '/report/employee' },
    { path: '/report/employee', name: '员工报表', component: '@/pages/report/employee' },
  ],
},
```

#### 步骤 5：更新 Mock 数据

在 `mock/auth.ts` 中更新角色权限：

```typescript
const rolePermissions: Record<string, string[]> = {
  ADMIN: ['system', 'employee', ..., 'report'], // 添加 report
  // ...
};
```

---

## 新增页面开发流程

### 1. 确定页面所属模块

根据团队分工确定页面所属模块：

| 成员 | 负责模块 |
|------|----------|
| 成员 A | 系统管理 |
| 成员 B | 员工档案、入转调离 |
| 成员 C | 考勤管理、薪资管理 |
| 成员 D | 审批中心、个人中心 |

### 2. 创建页面文件

在对应模块目录下创建页面：

```bash
# 示例：新增员工详情页
touch src/pages/employee/detail/index.tsx
```

### 3. 编写页面代码

```typescript
// src/pages/employee/detail/index.tsx
import React, { useEffect, useState } from 'react';
import { useParams } from '@umijs/max';
import { Card, Descriptions } from 'antd';
import { getEmployeeDetail } from '@/services/employee';
import type { Employee } from '@/types/employee';

const EmployeeDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [employee, setEmployee] = useState<Employee>();

  useEffect(() => {
    loadEmployee();
  }, [id]);

  const loadEmployee = async () => {
    const data = await getEmployeeDetail(Number(id));
    setEmployee(data);
  };

  return (
    <Card title="员工详情">
      <Descriptions>
        <Descriptions.Item label="姓名">{employee?.name}</Descriptions.Item>
        <Descriptions.Item label="工号">{employee?.employeeNo}</Descriptions.Item>
        {/* ... */}
      </Descriptions>
    </Card>
  );
};

export default EmployeeDetail;
```

### 4. 添加路由配置

在 `.umirc.ts` 中添加路由：

```typescript
{
  path: '/employee/detail/:id',
  name: '员工详情',
  component: '@/pages/employee/detail',
  hideInMenu: true, // 不在菜单显示
},
```

### 5. 添加接口服务

```typescript
// src/services/employee/index.ts
import request from '@/utils/request';

export async function getEmployeeDetail(id: number): Promise<Employee> {
  return request.get(`/employees/${id}`);
}
```

### 6. 添加 Mock 数据

```typescript
// mock/employee.ts
export default {
  'GET /employees/:id': (req, res) => {
    const { id } = req.params;
    
    res.json({
      code: 0,
      message: 'success',
      data: {
        id: Number(id),
        name: '张三',
        employeeNo: 'EMP001',
        // ...
      },
    });
  },
};
```

### 7. 测试验证

1. 启动开发服务器：`pnpm dev`
2. 访问页面：http://localhost:8000/employee/detail/1
3. 验证功能是否正常
4. 验证权限控制是否生效

---

## 常见问题解答

### 1. Mock 数据不生效

**原因**：
- Mock 文件语法错误
- Mock 文件未正确导出
- 开发服务器未重启

**解决方案**：
```bash
# 检查 Mock 文件语法
npx tsc mock/*.ts --noEmit

# 重启开发服务器
pnpm dev
```

### 2. 权限控制不生效

**原因**：
- 权限标识拼写错误
- 权限未在 `access.ts` 中定义
- 用户信息未正确初始化

**解决方案**：
```typescript
// 检查权限标识是否一致
// .umirc.ts
{ access: 'system' }

// access.ts
return { system: permissions.includes('system') };

// mock/auth.ts
permissions: ['system', ...]
```

### 3. 菜单未显示

**原因**：
- 路由 `access` 属性与权限不匹配
- 用户无对应权限
- 菜单配置错误

**解决方案**：
```typescript
// 检查菜单配置
// src/constants/menu.ts
{
  key: 'system',
  name: '系统管理',
  access: 'system', // 必须与 access.ts 中定义一致
}

// 检查用户权限
console.log(currentUser.permissions); // 应包含 'system'
```

### 4. 请求 404 错误

**原因**：
- Mock 接口路径不正确
- Mock 文件未加载
- 代理配置错误

**解决方案**：
```typescript
// 检查 Mock 接口路径
// mock/auth.ts
'POST /auth/login': ...  // 注意没有前导 /

// 检查请求路径
// services/auth/index.ts
request.post('/auth/login')  // 必须有前导 /
```

### 5. 页面空白

**原因**：
- 组件渲染错误
- 数据未加载完成
- 路由配置错误

**解决方案**：
```typescript
// 添加加载状态
const [loading, setLoading] = useState(true);

if (loading) return <Spin />;

// 添加错误处理
useEffect(() => {
  loadData().catch(error => {
    message.error('加载失败');
    console.error(error);
  });
}, []);
```

### 6. Token 丢失

**原因**：
- localStorage 被清除
- Token 过期
- 登录状态未正确保存

**解决方案**：
```typescript
// 检查 localStorage
console.log(localStorage.getItem('token'));
console.log(localStorage.getItem('userInfo'));

// 重新登录
localStorage.clear();
window.location.href = '/login';
```

---

## 开发规范速查

### 文件命名

- 组件文件：`PascalCase.tsx`（如 `EmployeeList.tsx`）
- 工具文件：`camelCase.ts`（如 `formatDate.ts`）
- 常量文件：`UPPER_SNAKE_CASE.ts`（如 `API_ENDPOINTS.ts`）

### 导入顺序

```typescript
// 1. React 相关
import React, { useState, useEffect } from 'react';

// 2. 第三方库
import { Card, Button } from 'antd';

// 3. Umi 相关
import { history, useModel, useAccess } from '@umijs/max';

// 4. 项目内部模块
import { getEmployeeList } from '@/services/employee';
import type { Employee } from '@/types/employee';

// 5. 样式文件
import styles from './index.less';
```

### 组件结构

```typescript
/**
 * 组件说明
 */

import React from 'react';

// 类型定义
interface Props {
  // ...
}

// 组件定义
const MyComponent: React.FC<Props> = (props) => {
  // Hooks
  const [state, setState] = useState();
  
  // 副作用
  useEffect(() => {
    // ...
  }, []);
  
  // 事件处理
  const handleClick = () => {
    // ...
  };
  
  // 渲染
  return (
    <div>
      {/* ... */}
    </div>
  );
};

export default MyComponent;
```

---

## 相关资源

- [Umi Max 文档](https://umijs.org/)
- [Ant Design 文档](https://ant.design/)
- [项目模块划分规范](../../../docs/03-HRMS模块划分规范.md)
- [前端版本信息与开发文档](../../../docs/前端版本信息与开发文档.md)
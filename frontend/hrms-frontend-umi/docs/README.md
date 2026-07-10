# HRMS 前端项目

## 技术栈

- **框架**: Umi Max 4.6.73
- **UI 库**: Ant Design 5.4.0
- **语言**: TypeScript 5
- **状态管理**: Umi Model
- **权限控制**: Umi Access
- **HTTP 客户端**: Axios
- **包管理**: pnpm

## 项目结构

```
src/
├── pages/                 # 页面组件
│   ├── login/            # 登录页
│   ├── home/             # 首页工作台
│   ├── system/           # 系统管理（成员 A）
│   ├── employee/         # 员工档案（成员 B）
│   ├── process/          # 入转调离（成员 B）
│   ├── attendance/       # 考勤管理（成员 C）
│   ├── salary/           # 薪资管理（成员 C）
│   ├── approval/         # 审批中心（成员 D）
│   └── profile/          # 个人中心（成员 D）
├── components/           # 公共组件
├── services/            # 接口服务
├── models/              # 状态模型
├── utils/               # 工具函数
│   └── request/         # Axios 封装
├── constants/           # 常量配置
│   ├── menu.ts          # 菜单配置
│   ├── home.ts          # 首页配置
│   └── permissions.ts   # 权限标识
├── types/               # TypeScript 类型
│   ├── api.ts           # API 响应类型
│   ├── user.ts          # 用户类型
│   └── menu.ts          # 菜单类型
├── access.ts            # 权限规则定义
└── app.ts               # 运行时配置
```

## 快速开始

### 安装依赖

```bash
pnpm install
```

### 启动开发服务器

```bash
pnpm dev
```

访问 http://localhost:8000

### 构建生产版本

```bash
pnpm build
```

## Mock 数据

### 开发模式

开发环境下，项目会自动加载 `mock/` 目录下的 Mock 数据，支持前后端分离开发。

### Mock 文件说明

| 文件 | 说明 |
|------|------|
| `mock/auth.ts` | 登录认证 Mock |
| `mock/user.ts` | 用户信息 Mock |
| `mock/home.ts` | 首页统计 Mock |

### 角色切换（开发环境）

登录页面提供角色选择下拉框（仅开发环境可见），可选择不同角色测试权限控制：

| 角色 | 可见菜单 |
|------|----------|
| 系统管理员 | 所有菜单 |
| HR 专员 | 员工档案、入转调离、考勤管理、薪资管理、审批中心、个人中心 |
| 部门主管 | 员工档案、入转调离、考勤管理、审批中心、个人中心 |
| 财务专员 | 薪资管理、审批中心、个人中心 |
| 普通员工 | 个人中心 |

### 关闭 Mock

生产环境构建时，Mock 数据会自动排除。

如需在开发环境关闭 Mock，在 `.umirc.ts` 中注释掉 `mock: {}` 配置。

## 后端集成

### 代理配置

`.umirc.ts` 中已配置后端代理：

```typescript
proxy: {
  '/auth': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
  // ...
}
```

### 切换到真实后端

1. 启动后端服务（默认端口 8080）
2. 注释或删除 `mock/` 目录下的文件
3. 重启前端开发服务器

## 权限控制

### 路由权限

在 `.umirc.ts` 中通过 `access` 属性控制：

```typescript
{
  path: '/system',
  name: '系统管理',
  access: 'system', // 仅 'system' 权限可见
  routes: [...]
}
```

### 权限定义

在 `src/access.ts` 中定义权限规则：

```typescript
export default function access(initialState: { currentUser?: UserInfo }) {
  const permissions = currentUser?.permissions || [];
  
  return {
    system: permissions.includes('system'),
    employee: permissions.includes('employee'),
    // ...
  };
}
```

### 权限标识

权限标识对应后端 `sys_menu.permission` 字段：

| 权限标识 | 说明 |
|----------|------|
| `system` | 系统管理 |
| `employee` | 员工档案 |
| `process` | 入转调离 |
| `attendance` | 考勤管理 |
| `salary` | 薪资管理 |
| `approval` | 审批中心 |
| `mycenter` | 个人中心 |

## 请求封装

### 使用示例

```typescript
import request from '@/utils/request';

// GET 请求
const data = await request.get('/api/users');

// POST 请求
const result = await request.post('/api/login', {
  username: 'admin',
  password: '123456'
});
```

### 自动处理

- **Token 注入**: 自动在请求头添加 `Authorization: Bearer <token>`
- **响应解析**: 自动解析 `Result<T>` 格式，返回 `data` 部分
- **错误处理**: 自动处理认证失败（401）跳转登录
- **统一错误提示**: 自动显示错误消息

## 开发规范

### 命名规范

- **文件名**: 小写 kebab-case（如 `user-info.tsx`）
- **组件名**: PascalCase（如 `UserInfo`）
- **函数名**: camelCase（如 `getUserInfo`）
- **常量**: UPPER_SNAKE_CASE（如 `MAX_PAGE_SIZE`）

### Git 提交规范

```
<type>(<scope>): <subject>

<body>
```

**类型**：
- `feat`: 新功能
- `fix`: 修复 Bug
- `refactor`: 重构
- `docs`: 文档更新
- `test`: 测试相关
- `chore`: 构建/工具变动

**示例**：
```
feat(login): 添加角色选择功能

- 开发环境支持角色下拉选择
- 不同角色显示不同菜单
- 生产环境自动隐藏角色选择
```

## 测试

### 运行验收测试

```bash
# 安装 Playwright
pnpm add -D @playwright/test
npx playwright install

# 运行测试
npx playwright test e2e/acceptance.spec.ts
```

### 测试覆盖

- 登录流程
- 角色切换和菜单过滤
- 权限控制
- 首页统计卡片显示
- 待办列表/申请进度显示
- 退出登录流程
- 响应式布局

## 常见问题

### 1. 登录后菜单未显示

检查权限配置：
- Mock 数据中 `permissions` 数组是否包含对应权限
- `.umirc.ts` 路由 `access` 属性是否与权限标识匹配

### 2. 请求 401 错误

检查 Token：
- localStorage 中是否存在 `token`
- Token 是否过期
- 请求拦截器是否正确注入 Token

### 3. Mock 数据未生效

检查配置：
- `.umirc.ts` 中是否启用了 `mock: {}`
- `mock/` 目录下是否存在对应的 Mock 文件
- Mock 文件是否正确导出

## 相关文档

- [Umi Max 文档](https://umijs.org/)
- [Ant Design 文档](https://ant.design/)
- [项目模块划分规范](../../../docs/03-HRMS模块划分规范.md)

## 团队协作

| 成员 | 负责模块 | 包路径 |
|------|----------|--------|
| 成员 A | 系统管理 | src/pages/system |
| 成员 B | 员工档案、入转调离 | src/pages/employee, src/pages/process |
| 成员 C | 考勤管理、薪资管理 | src/pages/attendance, src/pages/salary |
| 成员 D | 审批中心、个人中心 | src/pages/approval, src/pages/profile |

## License

MIT

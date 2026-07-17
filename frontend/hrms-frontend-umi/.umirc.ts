import { defineConfig } from '@umijs/max';

export default defineConfig({
  antd: {},
  access: {},
  model: {},
  initialState: {},
  request: {},
  layout: {
    title: 'HRMS 人资管理系统',
  },
  // Mock 配置（开发环境自动加载）
  // mock: {},  // 已切换到真实后端（2026-07-10）
  // 代理配置（全量转发至后端 /api/v1/* 和其他模块前缀）
  proxy: {
    '/api/v1': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
    '/auth': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
    '/employees': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
    '/departments': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
    '/leave-requests': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
    '/approval': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
    '/salary': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
    '/my': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
  routes: [
    // 登录页（无需权限）
    {
      path: '/login',
      layout: false,
      component: '@/pages/login',
    },

    // 首页工作台（所有人可见）
    {
      path: '/home',
      name: '首页',
      icon: 'home',
      component: '@/pages/Home',
    },

    // 系统管理（成员 A）- 仅管理员可见
    {
      path: '/system',
      name: '权限体系',
      icon: 'setting',
      access: 'system',
      routes: [
        { path: '/system', redirect: '/system/user' },
        {
          path: '/system/user',
          name: '用户管理',
          component: '@/pages/system/user',
        },
        {
          path: '/system/role',
          name: '角色管理',
          component: '@/pages/system/role',
        },
        {
          path: '/system/menu',
          name: '菜单管理',
          component: '@/pages/system/menu',
        },
        {
          path: '/system/dept',
          name: '部门管理',
          component: '@/pages/system/dept',
        },
        {
          path: '/system/post',
          name: '职位管理',
          component: '@/pages/system/post',
        },
        {
          path: '/system/dict',
          name: '字典管理',
          component: '@/pages/system/dict',
        },
      ],
    },

    // 组织架构（九大模块之一）
    {
      path: '/organization',
      name: '组织架构',
      icon: 'apartment',
      access: 'organization',
      component: '@/pages/organization',
    },

    // 员工档案（成员 B）- ADMIN, HR, MANAGER 可见
    {
      path: '/employee',
      name: '员工档案',
      icon: 'team',
      access: 'employee',
      routes: [
        { path: '/employee', redirect: '/employee/list' },
        {
          path: '/employee/list',
          name: '员工列表',
          component: '@/pages/employee',
        },
        {
          path: '/employee/create',
          name: '新增员工',
          component: '@/pages/employee/edit',
          hideInMenu: true,
        },
        {
          path: '/employee/:id/edit',
          name: '编辑员工',
          component: '@/pages/employee/edit',
          hideInMenu: true,
        },
        {
          path: '/employee/detail/:id',
          name: '员工详情',
          component: '@/pages/employee/detail',
          hideInMenu: true,
        },
        {
          path: '/employee/contract',
          name: '合同管理',
          component: '@/pages/employee/contract',
        },
      ],
    },

    // 入转调离（成员 B）- ADMIN, HR, MANAGER 可见
    {
      path: '/process',
      name: '入转调离',
      icon: 'swap',
      access: 'process',
      routes: [
        { path: '/process', redirect: '/process/entry' },
        {
          path: '/process/entry',
          name: '入职申请',
          component: '@/pages/process/entry',
        },
        {
          path: '/process/regular',
          name: '转正申请',
          component: '@/pages/process/regular',
        },
        {
          path: '/process/transfer',
          name: '调岗申请',
          component: '@/pages/process/transfer',
        },
        {
          path: '/process/leave',
          name: '离职申请',
          component: '@/pages/process/leave',
        },
      ],
    },

    // 考勤管理（成员 C）- 打卡所有已登录用户可见，管理类功能 ADMIN, HR, MANAGER 可见
    {
      path: '/attendance',
      name: '考勤管理',
      icon: 'clock-circle',
      routes: [
        { path: '/attendance', redirect: '/attendance/punch' },
        {
          path: '/attendance/punch',
          name: '员工打卡',
          access: 'attendancePunch',
          component: '@/pages/attendance/punch',
        },
        {
          path: '/attendance/record',
          name: '考勤记录',
          access: 'attendanceManage',
          component: '@/pages/attendance/record',
        },
        {
          path: '/attendance/groups',
          name: '考勤配置',
          access: 'attendanceManage',
          component: '@/pages/attendance/groups',
        },
        {
          path: '/attendance/leave',
          name: '请假申请',
          access: 'attendanceManage',
          component: '@/pages/attendance/leave',
        },
        {
          path: '/attendance/summary',
          name: '考勤统计',
          access: 'attendanceManage',
          component: '@/pages/attendance/summary',
        },
      ],
    },

    // 薪资管理（成员 C）- ADMIN, HR, FINANCE 可见
    {
      path: '/salary',
      name: '薪资管理',
      icon: 'pay-circle',
      access: 'salary',
      routes: [
        { path: '/salary', redirect: '/salary/account' },
        {
          path: '/salary/account',
          name: '薪资账套',
          component: '@/pages/salary/account',
        },
        {
          path: '/salary/batch',
          name: '薪资核算',
          component: '@/pages/salary/batch',
        },
        {
          path: '/salary/payslip',
          name: '工资条',
          component: '@/pages/salary/payslip',
        },
      ],
    },

    // 审批中心（成员 D）- ADMIN, HR, MANAGER, FINANCE 可见
    {
      path: '/approval',
      name: '审批中心',
      icon: 'check-circle',
      access: 'approval',
      routes: [
        { path: '/approval', redirect: '/approval/workspace' },
        {
          path: '/approval/workspace',
          name: '审批工作台',
          component: '@/pages/approval/workspace',
        },
        {
          path: '/approval/delegation',
          name: '审批配置',
          component: '@/pages/approval/delegation',
        },
        {
          path: '/approval/detail/:id',
          name: '审批详情',
          component: '@/pages/approval/detail',
          hideInMenu: true,
        },
      ],
    },

    // 个人中心（成员 D）- 所有人可见
    {
      path: '/profile',
      name: '个人中心',
      icon: 'user',
      routes: [
        { path: '/profile', redirect: '/profile/index' },
        {
          path: '/profile/index',
          name: '我的首页',
          component: '@/pages/profile/index',
        },
        {
          path: '/profile/archive',
          name: '我的档案',
          component: '@/pages/profile/archive',
        },
        {
          path: '/profile/attendance',
          name: '我的考勤',
          component: '@/pages/profile/attendance',
        },
        {
          path: '/profile/leave',
          name: '我的请假',
          component: '@/pages/profile/leave',
        },
        {
          path: '/profile/salary',
          name: '我的薪资',
          component: '@/pages/profile/salary',
        },
        {
          path: '/profile/security',
          name: '账号安全',
          component: '@/pages/profile/security',
        },
      ],
    },

    // AI 智能助手（尝试性生成基础入口）
    {
      path: '/ai',
      name: 'AI 智能助手',
      icon: 'robot',
      access: 'ai',
      component: '@/pages/ai',
    },

    // 首页重定向（带权限控制，未登录时会被路由守卫拦截）
    { path: '/', redirect: '/home' },

    // 403 无权限页面
    {
      path: '/403',
      layout: false,
      component: '@/pages/403',
    },
  ],
  npmClient: 'pnpm',
  utoopack: {},
});

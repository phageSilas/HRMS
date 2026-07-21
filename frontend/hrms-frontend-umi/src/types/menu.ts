/**
 * 菜单相关类型定义
 */

/**
 * 菜单项（用于侧边栏渲染）
 * ProLayout 菜单数据格式
 */
export interface MenuItem {
  /** 菜单ID */
  key?: string;
  /** 菜单名称（用于显示） */
  name: string;
  /** 路由路径 */
  path?: string;
  /** 图标 */
  icon?: string;
  /** 子菜单列表 */
  children?: MenuItem[];
  /** 隐藏菜单 */
  hideInMenu?: boolean;
  /** 父级菜单路径（用于 ProLayout） */
  parentPath?: string;
  /** 重定向 */
  redirect?: string;
}

/**
 * 将后端菜单数据转换为 ProLayout 需要的格式
 * 后端返回字段：id, name, title, path, icon, children
 */
export function transformMenus(menus: any[], parentPath?: string): MenuItem[] {
  if (!menus || !Array.isArray(menus)) {
    return [];
  }

  const transformedMenus = menus.map(menu => {
    const currentPath = menu.path || '';

    const item: MenuItem = {
      key: String(menu.id),
      name: menu.title || menu.name,
      path: currentPath,
      icon: menu.icon,
      parentPath,
    };

    // 递归处理子菜单
    if (menu.children && menu.children.length > 0) {
      item.children = transformMenus(menu.children, currentPath);
    }

    return item;
  });

  if (!parentPath) {
    return ensureSalaryProfileMenu(transformedMenus);
  }
  return transformedMenus;
}

/**
 * 为薪资管理菜单兜底注入“薪资档案”入口，避免后端菜单未同步时页面无入口。
 */
export function ensureSalaryProfileMenu(menus: MenuItem[]): MenuItem[] {
  return menus.map((menu) => {
    if (menu.path !== '/salary') {
      return menu;
    }
    const children = menu.children || [];
    if (children.some((child) => child.path === '/salary/profile')) {
      return menu;
    }
    const profileMenu: MenuItem = {
      key: 'salary-profile',
      name: '薪资档案',
      path: '/salary/profile',
      parentPath: '/salary',
    };
    const accountIndex = children.findIndex((child) => child.path === '/salary/account');
    if (accountIndex < 0) {
      return { ...menu, children: [...children, profileMenu] };
    }
    return {
      ...menu,
      children: [
        ...children.slice(0, accountIndex + 1),
        profileMenu,
        ...children.slice(accountIndex + 1),
      ],
    };
  });
}

/**
 * 菜单过滤结果（旧版兼容）
 */
export interface MenuFilterResult {
  visibleMenus: MenuItem[];
  hasPermission: (access: string) => boolean;
}

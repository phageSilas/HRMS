/**
 * 菜单配置类型定义
 */

/**
 * 菜单项配置
 */
export interface MenuItem {
  key: string;          // 唯一标识
  name: string;         // 显示名称
  icon: string;         // 图标名称
  path: string;         // 路由路径
  access?: string;      // 权限标识（可选，无则所有人可见）
  children?: MenuItem[]; // 子菜单
  hideInMenu?: boolean; // 是否在菜单中隐藏
}

/**
 * 菜单过滤结果
 */
export interface MenuFilterResult {
  visibleMenus: MenuItem[];
  hasPermission: (access: string) => boolean;
}
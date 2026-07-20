/**
 * React 类型增强
 *
 * 修复 @ant-design/icons@5.0.1 与 @types/react@18 的类型兼容性问题。
 *
 * 根因分析：
 * @ant-design/icons 的 AntdIconProps 通过类型链
 * (AntdIconProps → IconBaseProps → HTMLProps<HTMLSpanElement> →
 *  AllHTMLAttributes → HTMLAttributes → DOMAttributes)
 * 继承自 React 的 DOMAttributes 接口。其类型声明中通过 Pick 选中了
 * onPointerEnterCapture 和 onPointerLeaveCapture 属性，但 React 18
 * 的 DOMAttributes 接口中未定义这两个属性（React 19 才引入），
 * 导致 TypeScript 推导出 onPointerEnterCapture: any 作为必需属性。
 *
 * 当 JSX 渲染 <UserOutlined />（等价于 React.createElement(UserOutlined, {})）
 * 时，{} 类型缺少这两个必需属性，触发 TS2739 错误。
 *
 * 解决方案：
 * 通过在 React.DOMAttributes 中声明这两个属性为可选，
 * 使 Pick 结果中的属性保留 ? 标记，从而 {} 类型可满足属性约束。
 *
 * @module Types
 */

import 'react';

declare module 'react' {
  interface DOMAttributes<T> {
    /** 指针进入捕获事件（兼容 @ant-design/icons v5 的 Pick 引用） */
    onPointerEnterCapture?: PointerEventHandler<T>;
    /** 指针离开捕获事件（兼容 @ant-design/icons v5 的 Pick 引用） */
    onPointerLeaveCapture?: PointerEventHandler<T>;
  }
}

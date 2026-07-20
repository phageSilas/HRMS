/**
 * 错误信息提取工具
 *
 * 在 catch 子句中统一提取可读的错误消息，替代 catch (err: any) + err?.message 的模式。
 *
 * @example
 * ```tsx
 * catch (err: unknown) {
 *   message.error(getErrorMessage(err));
 * }
 * ```
 */

/**
 * 从 unknown 类型错误中提取可读的错误消息
 *
 * @param error    捕获的错误对象
 * @param fallback 无法提取时的默认文案
 * @returns 可读的错误消息字符串
 */
export function getErrorMessage(error: unknown, fallback = '操作失败'): string {
  if (error instanceof Error) {
    return error.message;
  }

  if (typeof error === 'string') {
    return error;
  }

  if (error && typeof error === 'object' && 'message' in error) {
    const msg = (error as Record<string, unknown>).message;
    if (typeof msg === 'string') return msg;
  }

  return fallback;
}

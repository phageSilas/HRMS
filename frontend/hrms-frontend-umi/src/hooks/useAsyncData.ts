/**
 * 通用异步数据加载 Hook
 *
 * 替代每个页面中重复的 useState(loading) + useState(error) + useState(data) + useCallback + useEffect 模式。
 *
 * @example
 * ```tsx
 * const { data: profile, loading, error, refresh } = useAsyncData(() => getProfile());
 * ```
 */
import { useCallback, useEffect, useRef, useState } from 'react';

/** Hook 返回的状态 */
export interface AsyncState<T> {
  /** 加载成功后的数据 */
  data: T | null;
  /** 是否正在加载 */
  loading: boolean;
  /** 错误信息，加载成功时为 null */
  error: string | null;
  /** 手动刷新（重新调用 fetcher） */
  refresh: () => void;
}

/**
 * useAsyncData — 通用异步数据加载
 *
 * @param fetcher  返回 Promise<T> 的异步函数
 * @param deps     当 deps 变化时重新调用 fetcher（默认 []）
 * @param options  可选配置
 * @returns { data, loading, error, refresh }
 */
export function useAsyncData<T>(
  fetcher: () => Promise<T>,
  deps: any[] = [],
  options?: {
    /** 是否在组件卸载时忽略已发出的请求结果（防内存泄漏），默认 true */
    cancelOnUnmount?: boolean;
  },
): AsyncState<T> {
  const { cancelOnUnmount = true } = options || {};
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /** 标记组件是否仍挂载，用于防止卸载后 setState */
  const mountedRef = useRef(true);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await fetcher();
      if (mountedRef.current) {
        setData(result);
      }
    } catch (err: unknown) {
      if (mountedRef.current) {
        const message =
          err instanceof Error
            ? err.message
            : typeof err === 'string'
              ? err
              : '加载失败';
        setError(message);
      }
    } finally {
      if (mountedRef.current) {
        setLoading(false);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => {
    mountedRef.current = true;
    fetchData();
    return () => {
      mountedRef.current = cancelOnUnmount ? false : true;
    };
  }, [fetchData, cancelOnUnmount]);

  const refresh = useCallback(() => {
    fetchData();
  }, [fetchData]);

  return { data, loading, error, refresh };
}

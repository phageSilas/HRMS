import { useLocation } from '@umijs/max';
import { useEffect, useRef } from 'react';

interface UsePageAutoRefreshOptions {
  cooldownMs?: number;
}

/**
 * 页面自动刷新 Hook。
 * 在路由重新进入、标签页回到前台或窗口重新聚焦时触发刷新。
 */
export function usePageAutoRefresh(
  onRefresh: () => void,
  options?: UsePageAutoRefreshOptions,
) {
  const location = useLocation();
  const refreshRef = useRef(onRefresh);
  const mountedRef = useRef(false);
  const lastTriggerAtRef = useRef(0);
  const cooldownMs = options?.cooldownMs ?? 500;

  refreshRef.current = onRefresh;

  const triggerRefresh = () => {
    const now = Date.now();
    if (now - lastTriggerAtRef.current < cooldownMs) {
      return;
    }
    lastTriggerAtRef.current = now;
    refreshRef.current();
  };

  useEffect(() => {
    if (!mountedRef.current) {
      mountedRef.current = true;
      return;
    }
    triggerRefresh();
  }, [location.key]);

  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        triggerRefresh();
      }
    };

    const handleFocus = () => {
      triggerRefresh();
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    window.addEventListener('focus', handleFocus);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      window.removeEventListener('focus', handleFocus);
    };
  }, []);
}

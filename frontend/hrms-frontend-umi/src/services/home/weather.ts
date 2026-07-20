/**
 * 首页天气接口请求
 */

import { transformUapiWeatherToHomeInfo } from '@/utils/weather';
import type { HomeWeatherInfo, UapiWeatherResponse } from '@/types/weather';

const WEATHER_API_URL = 'https://uapis.cn/api/v1/misc/weather?city=郑州';
const WEATHER_REQUEST_TIMEOUT = 8000;

/**
 * 获取郑州实时天气
 */
export async function fetchZhengzhouWeather(): Promise<HomeWeatherInfo | null> {
  if (typeof window === 'undefined') {
    return null;
  }

  const controller = new AbortController();
  const timeoutId = window.setTimeout(
    () => controller.abort(),
    WEATHER_REQUEST_TIMEOUT,
  );

  try {
    const response = await fetch(WEATHER_API_URL, {
      method: 'GET',
      signal: controller.signal,
    });

    if (!response.ok) {
      return null;
    }

    const payload = (await response.json()) as UapiWeatherResponse;
    return transformUapiWeatherToHomeInfo(payload);
  } catch (error) {
    console.warn('[HomeWeather] 获取真实天气失败', error);
    return null;
  } finally {
    window.clearTimeout(timeoutId);
  }
}

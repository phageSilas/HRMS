/**
 * 首页天气展示类型定义
 */

export type HomeWeatherType = '晴天' | '多云' | '阴天' | '雨天';

export interface HomeWeatherInfo {
  type: HomeWeatherType;
  temperature: string;
  tip: string;
  weatherCode?: string;
  rawWeatherText?: string;
}

export interface UapiWeatherRecord {
  [key: string]: unknown;
}

export interface UapiWeatherResponse {
  code?: number;
  message?: string;
  msg?: string;
  data?: UapiWeatherRecord;
  result?: UapiWeatherRecord;
  [key: string]: unknown;
}

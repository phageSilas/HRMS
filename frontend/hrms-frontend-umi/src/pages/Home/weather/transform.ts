/**
 * 首页天气数据转换逻辑
 */

import type {
  HomeWeatherInfo,
  HomeWeatherType,
  UapiWeatherRecord,
  UapiWeatherResponse,
} from './types';

export const DEFAULT_HOME_WEATHER_INFO: HomeWeatherInfo = {
  type: '多云',
  temperature: '15°C',
  tip: '带好常备轻薄外套',
  weatherCode: 'default-cloudy',
  rawWeatherText: '多云',
};

const SUNNY_CODE_SET = new Set(['0', '00', '100', '900']);
const CLOUDY_CODE_SET = new Set(['1', '01', '101', '102', '103']);
const OVERCAST_CODE_SET = new Set(['2', '02', '104']);
const RAINY_CODE_SET = new Set([
  '3',
  '03',
  '300',
  '301',
  '302',
  '303',
  '304',
  '305',
  '306',
  '307',
  '308',
  '309',
  '310',
  '311',
  '312',
  '313',
  '314',
  '315',
  '316',
  '317',
  '318',
]);

function isRecord(value: unknown): value is UapiWeatherRecord {
  return typeof value === 'object' && value !== null;
}

function pickString(record: UapiWeatherRecord, keys: string[]) {
  for (const key of keys) {
    const value = record[key];
    if (typeof value === 'string' && value.trim()) {
      return value.trim();
    }
    if (typeof value === 'number' && Number.isFinite(value)) {
      return String(value);
    }
  }
  return undefined;
}

function unwrapWeatherRecord(
  payload: UapiWeatherResponse | UapiWeatherRecord | null | undefined,
) {
  if (!payload || !isRecord(payload)) {
    return undefined;
  }

  if (isRecord(payload.data)) {
    return payload.data;
  }

  if (isRecord(payload.result)) {
    return payload.result;
  }

  return payload;
}

function formatTemperature(value?: string) {
  if (!value) {
    return DEFAULT_HOME_WEATHER_INFO.temperature;
  }

  if (value.includes('°')) {
    return value;
  }

  return `${value}°C`;
}

function resolveWeatherTypeByCode(code?: string): HomeWeatherType | undefined {
  if (!code) {
    return undefined;
  }

  const normalizedCode = code.trim().toLowerCase();

  if (SUNNY_CODE_SET.has(normalizedCode) || normalizedCode.includes('sun')) {
    return '晴天';
  }

  if (CLOUDY_CODE_SET.has(normalizedCode) || normalizedCode.includes('cloud')) {
    return '多云';
  }

  if (
    OVERCAST_CODE_SET.has(normalizedCode) ||
    normalizedCode.includes('overcast')
  ) {
    return '阴天';
  }

  if (RAINY_CODE_SET.has(normalizedCode) || normalizedCode.includes('rain')) {
    return '雨天';
  }

  return undefined;
}

function resolveWeatherTypeByText(text?: string): HomeWeatherType {
  if (!text) {
    return '阴天';
  }

  if (text.includes('晴')) {
    return '晴天';
  }

  if (
    text.includes('多云') ||
    text.includes('少云') ||
    text.includes('晴间多云')
  ) {
    return '多云';
  }

  if (
    text.includes('雨') ||
    text.includes('雪') ||
    text.includes('雷') ||
    text.includes('冰')
  ) {
    return '雨天';
  }

  if (
    text.includes('阴') ||
    text.includes('雾') ||
    text.includes('霾') ||
    text.includes('沙') ||
    text.includes('浮尘')
  ) {
    return '阴天';
  }

  return '阴天';
}

function buildTip(record: UapiWeatherRecord) {
  const directTip = pickString(record, [
    'tip',
    'tips',
    'advice',
    'prompt',
    'notice',
    'suggestion',
  ]);
  if (directTip) {
    return directTip;
  }

  const windDirection = pickString(record, [
    'wind_direction',
    'windDirection',
    'wd',
  ]);
  const windPower = pickString(record, [
    'wind_power',
    'windPower',
    'wind_scale',
    'windScale',
    'ws',
  ]);

  if (windDirection && windPower) {
    return `${windDirection}${windPower}`;
  }

  if (windDirection) {
    return `${windDirection}，注意出行体感变化`;
  }

  return DEFAULT_HOME_WEATHER_INFO.tip;
}

export function transformUapiWeatherToHomeInfo(
  payload: UapiWeatherResponse | UapiWeatherRecord | null | undefined,
) {
  const record = unwrapWeatherRecord(payload);
  if (!record) {
    return null;
  }

  const weatherCode = pickString(record, [
    'weather_code',
    'weatherCode',
    'code',
  ]);
  const rawWeatherText = pickString(record, [
    'weather',
    'weather_name',
    'weatherName',
    'text',
    'condition',
  ]);
  const temperature = pickString(record, [
    'temperature',
    'temp',
    'current_temperature',
    'currentTemperature',
  ]);

  const resolvedType =
    resolveWeatherTypeByCode(weatherCode) ||
    resolveWeatherTypeByText(rawWeatherText);

  return {
    type: resolvedType,
    temperature: formatTemperature(temperature),
    tip: buildTip(record),
    weatherCode,
    rawWeatherText,
  } satisfies HomeWeatherInfo;
}

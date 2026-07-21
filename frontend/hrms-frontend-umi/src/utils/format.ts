import dayjs from 'dayjs';

/**
 * 格式化入转调离模块的日期时间字段。
 */
export function formatProcessDateTime(value?: string | number[] | null) {
  if (!value) {
    return '--';
  }
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value;
    if (!year || !month || !day) {
      return '--';
    }
    const date = dayjs(new Date(year, month - 1, day, hour, minute, second));
    return date.isValid() ? date.format('YYYY-MM-DD HH:mm') : value.join('-');
  }
  const date = dayjs(value);
  if (!date.isValid()) {
    return value;
  }
  return date.format('YYYY-MM-DD HH:mm');
}

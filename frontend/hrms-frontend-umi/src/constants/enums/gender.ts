/**
 * 性别枚举
 * 对应后端 GenderEnum
 */

export const Gender = {
  MALE: 1,    // 男
  FEMALE: 2,  // 女
} as const;

export type GenderType = typeof Gender[keyof typeof Gender];

/**
 * 性别标签映射
 */
export const GenderLabel: Record<number, string> = {
  [Gender.MALE]: '男',
  [Gender.FEMALE]: '女',
};
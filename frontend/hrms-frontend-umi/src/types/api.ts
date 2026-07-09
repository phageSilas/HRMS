/**
 * 统一返回体类型定义
 * 对应后端 Result<T>
 */

/**
 * 统一返回体
 */
export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

/**
 * 分页返回体
 */
export interface PageResult<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

/**
 * 分页查询参数
 */
export interface PageQuery {
  pageNum: number;
  pageSize: number;
}
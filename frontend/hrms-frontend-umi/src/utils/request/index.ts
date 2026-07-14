import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { message } from 'antd';

/**
 * HRMS 请求工具封装
 * 基于 axios 封装，统一处理 Token 注入、响应拦截、错误处理
 *
 * 注意：响应拦截器已将后端 Result<T> 解包，返回 T 而非 AxiosResponse<T>。
 * 下方 http 对象重新声明了类型签名，使 TypeScript 类型与运行时行为一致。
 */

// 创建 axios 实例
const instance: AxiosInstance = axios.create({
  // 开发环境走 Umi 代理/Mock，生产环境可配置 API_BASE_URL
  baseURL: process.env.API_BASE_URL || '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器：注入 Token
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器：处理 Result<T> 格式
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message: msg, data } = response.data;

    // 成功
    if (code === 0) {
      return data;
    }

    // 认证失败：40100-40199
    if (code >= 40100 && code <= 40199) {
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      message.error(msg || '认证失败，请重新登录');
      window.location.href = '/login';
      return Promise.reject(new Error(msg || '认证失败，请重新登录'));
    }

    // 参数错误：40001-40099
    if (code >= 40001 && code <= 40099) {
      message.error(msg || '参数错误');
      return Promise.reject(new Error(msg || '参数错误'));
    }

    // 系统错误：50001-50099
    if (code >= 50001 && code <= 50099) {
      message.error('系统繁忙，请稍后重试');
      return Promise.reject(new Error('系统繁忙，请稍后重试'));
    }

    // 业务错误：60001-60599
    if (code >= 60001 && code <= 60599) {
      message.error(msg || '操作失败');
      return Promise.reject(new Error(msg || '操作失败'));
    }

    // 其他错误
    message.error(msg || '请求失败');
    return Promise.reject(new Error(msg || '请求失败'));
  },
  (error) => {
    // 网络错误或超时
    if (error.code === 'ECONNABORTED') {
      message.error('请求超时，请稍后重试');
      return Promise.reject(new Error('请求超时，请稍后重试'));
    }
    if (!window.navigator.onLine) {
      message.error('网络断开，请检查网络连接');
      return Promise.reject(new Error('网络断开，请检查网络连接'));
    }
    // HTTP 状态码错误
    if (error.response) {
      const status = error.response.status;
      if (status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        message.error('登录已过期，请重新登录');
        window.location.href = '/login';
        return Promise.reject(new Error('登录已过期，请重新登录'));
      }
      if (status === 403) {
        message.error('没有权限访问该资源');
        return Promise.reject(new Error('没有权限访问该资源'));
      }
      if (status === 404) {
        message.error('请求的资源不存在');
        return Promise.reject(new Error('请求的资源不存在'));
      }
      if (status >= 500) {
        message.error('服务器繁忙，请稍后重试');
        return Promise.reject(new Error('服务器繁忙，请稍后重试'));
      }
    }
    message.error('网络错误，请稍后重试');
    return Promise.reject(new Error('网络错误，请稍后重试'));
  }
);

/**
 * 类型安全的请求方法集合
 * 与 axios 实例共享相同的拦截器，但重新声明了类型签名，使
 * 返回类型为 T（而非 AxiosResponse<T>），与拦截器解包行为一致。
 *
 * 用法示例：
 *   request.get<PageResult<ApprovalTask>>('/api/v1/approval/tasks/pending')
 *   // 返回 Promise<PageResult<ApprovalTask>>
 */
const request = {
  get: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    instance.get<any, T>(url, config),

  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    instance.post<any, T>(url, data, config),

  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    instance.put<any, T>(url, data, config),

  delete: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    instance.delete<any, T>(url, config),

  patch: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> =>
    instance.patch<any, T>(url, data, config),
};

export default request;
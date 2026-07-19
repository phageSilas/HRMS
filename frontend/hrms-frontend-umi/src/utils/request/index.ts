import axios, {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
} from 'axios';
import { message } from 'antd';

/**
 * HRMS 请求工具封装
 * 基于 axios 封装，统一处理 Token 注入、响应拦截、错误处理
 *
 * 重要：响应拦截器已自动解包后端 Result<T> 格式，
 * 成功时直接返回 data 字段（T），失败时抛出异常。
 * 因此 service 中的泛型参数应为实际数据类型，无需再包裹 Result<>。
 */

// 创建 axios 实例
// 走 Umi 代理（开发环境 proxy 配置见 .umirc.ts），不设 baseURL 直连后端
const instance: AxiosInstance = axios.create({
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
  },
);

// 响应拦截器：处理 Result<T> 格式
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message: msg, data } = response.data || {};

    // 成功：后端统一成功码为 20000，兼容早期前端 mock 的 0。
    if (code === 20000 || code === 0) {
      return data;
    }

    // 认证失败：40100-40199
    if (code >= 40100 && code <= 40199) {
      // 账号锁定/禁用等不跳转，只提示
      if (code === 40110 || code === 40111 || code === 40112) {
        message.error(msg || '账号状态异常，请联系管理员');
        return Promise.reject(new Error(msg || '账号状态异常'));
      }
      // Token 过期/无效/未登录 → 跳转登录页
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      // 防止重复跳转：5 秒内只跳一次
      const lastRedirect = sessionStorage.getItem('redirectToLogin');
      const now = Date.now();
      if (lastRedirect && now - Number(lastRedirect) < 5000) {
        return Promise.reject(new Error(msg || '认证失败'));
      }
      sessionStorage.setItem('redirectToLogin', String(now));
      message.error(msg || '登录已过期，请重新登录');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
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
    // 网络错误或超时：保留原始 AxiosError 的 code，让上层能判断网络状态
    if (error.code === 'ECONNABORTED') {
      message.error('请求超时，请稍后重试');
      return Promise.reject(error);
    }
    if (!window.navigator.onLine) {
      message.error('网络断开，请检查网络连接');
      return Promise.reject(error);
    }
    // HTTP 状态码错误
    if (error.response) {
      const status = error.response.status;
      if (status === 401) {
        // 检查业务错误码，账号锁定类不跳转
        const bizCode = error.response.data?.code;
        if (bizCode === 40110 || bizCode === 40111 || bizCode === 40112) {
          message.error(error.response.data?.message || '账号状态异常，请联系管理员');
          return Promise.reject(new Error('账号状态异常'));
        }
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        // 防止重复跳转：5 秒内只跳一次
        const lastRedirect = sessionStorage.getItem('redirectToLogin');
        const now = Date.now();
        if (lastRedirect && now - Number(lastRedirect) < 5000) {
          return Promise.reject(new Error('认证失败'));
        }
        sessionStorage.setItem('redirectToLogin', String(now));
        message.error('登录已过期，请重新登录');
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
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
    // 其他网络错误（连接失败等），保留原始错误
    message.error('网络错误，请稍后重试');
    return Promise.reject(error);
  },
);

/**
 * 类型安全的请求方法包装
 * 响应拦截器已解包 Result<T> -> T，此处泛型 T 即为业务数据类型
 */
const request = {
  get: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    instance.get(url, config) as unknown as Promise<T>,

  post: <T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig,
  ): Promise<T> =>
    instance.post(url, data, config) as unknown as Promise<T>,

  put: <T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig,
  ): Promise<T> =>
    instance.put(url, data, config) as unknown as Promise<T>,

  delete: <T = any>(
    url: string,
    config?: AxiosRequestConfig,
  ): Promise<T> =>
    instance.delete(url, config) as unknown as Promise<T>,
};

export default request;

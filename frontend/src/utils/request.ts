// src/utils/request.ts
import axios, { type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';

// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000,
});

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
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

// 响应拦截器 - 修正类型处理
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 直接返回 response.data，这样调用 API 时就能直接得到数据
    return response.data;
  },
  (error) => {
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录');
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      router.push('/login');
    } else if (error.response?.status === 403) {
      ElMessage.error('没有权限访问');
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误');
    }
    return Promise.reject(error);
  }
);

export default service;
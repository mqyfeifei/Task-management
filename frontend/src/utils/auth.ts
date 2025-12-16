//@/utils/auth.ts
// 用户认证相关工具函数
import {jwtDecode} from 'jwt-decode';
import type { UserInfo } from '@/types';


// 获取 token
export const getToken = (): string | null => {
  return localStorage.getItem('token') || 
    sessionStorage.getItem('token');
};


// 从 token 中提取用户信息
export const getUserIdFromToken = (token: string): number | null => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.userId || null;
  } catch {
    return null;
  }
};

// 检查登录状态
export const isLoggedIn = (): boolean => {
  const token = getToken();
  if (!token) return false;
  
  // 检查 token 是否过期
  try {
    const decoded: any = jwtDecode(token);
    const currentTime = Date.now() / 1000;
    
    // 检查 token 是否过期
    if (decoded.exp && decoded.exp < currentTime) {
      clearUserInfo();
      return false;
    }
    
    return true;
  } catch (error) {
    console.error('检查登录状态失败:', error);
    return false;
  }
};


// 保存用户信息
export const setUserInfo = (token: string): void => {
  localStorage.setItem('token', token);
  const userId = getUserIdFromToken(token);
  if (userId) {
    localStorage.setItem('userId', userId.toString());
  }
};

// 清除用户信息
export const clearUserInfo = (): void => {
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  sessionStorage.removeItem('token');
  sessionStorage.removeItem('userInfo');
};

// 获取当前用户信息
export const getCurrentUser = (): UserInfo | null => {
  const token = getToken();
  if (!token) return null;
  
  try {
    const decoded: any = jwtDecode(token);
    return {
      userId: decoded.userId || decoded.sub,
      username: decoded.username,
      email: decoded.email,
      avatar: decoded.avatar
    };
  } catch (error) {
    console.error('解析用户信息失败:', error);
    return null;
  }
};

// 获取当前用户ID
export const getCurrentUserId = (): string | null => {
  return localStorage.getItem('userId');
};

// 获取用户信息（扩展版）
export const getUserInfo = (): UserInfo | null => {
  // 尝试从 localStorage 获取完整用户信息
  const userStr = localStorage.getItem('userInfo');
  if (userStr) {
    try {
      return JSON.parse(userStr);
    } catch (error) {
      console.error('解析用户信息失败:', error);
    }
  }
  // 回退到从 token 解析
  return getCurrentUser();
};



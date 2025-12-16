// @/router/guards.ts
import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router';
import { isLoggedIn } from '@/utils/auth';

/**
 * 路由守卫 - 检查登录状态
 * 用于需要认证的路由
 */
export const requireAuth = (
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext
): void => {
  // 如果路由不需要认证，直接放行
  if (!to.meta?.requiresAuth) {
    next();
    return;
  }

  try {
    const loggedIn = isLoggedIn();
    
    if (loggedIn) {
      next();
    } else {
      // 保存当前路径，登录后可以跳转回来
      const redirectPath = to.fullPath !== '/login' ? to.fullPath : undefined;
      next({
        path: '/login',
        query: redirectPath ? { redirect: redirectPath } : undefined
      });
    }
  } catch (error) {
    console.error('登录状态检查失败:', error);
    // 发生错误时，安全起见要求重新登录
    next({
      path: '/login',
      query: { error: 'auth_check_failed' }
    });
  }
};

/**
 * 已登录用户访问登录/注册页时重定向到任务页
 * 防止已登录用户重复登录
 */
export const redirectIfLoggedIn = (
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext
): void => {
  // 只处理登录和注册页面
  const isAuthPage = to.path === '/login' || to.path === '/register';
  if (!isAuthPage) {
    next();
    return;
  }

  try {
    const loggedIn = isLoggedIn();
    
    if (loggedIn) {
      // 已登录用户访问登录/注册页，重定向到任务页
      next('/tasks');
    } else {
      next();
    }
  } catch (error) {
    console.error('登录状态检查失败:', error);
    // 发生错误时，允许继续访问登录页
    next();
  }
};

/**
 * 全局前置守卫
 * 结合多个守卫逻辑
 */
export const globalGuard = (
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext
): void => {
  try {
    const loggedIn = isLoggedIn();
    
    // 1. 访问登录/注册页
    if (to.path === '/login' || to.path === '/register') {
      if (loggedIn) {
        // 已登录，重定向到任务页
        next('/tasks');
      } else {
        // 未登录，允许访问
        next();
      }
      return; // 重要：这里要 return
    }
    
    // 2. 访问需要认证的页面
    if (to.meta?.requiresAuth) {
      if (loggedIn) {
        // 已登录，允许访问
        next();
      } else {
        // 未登录，重定向到登录页
        const redirectPath = to.fullPath !== '/login' ? to.fullPath : undefined;
        next({
          path: '/login',
          query: redirectPath ? { redirect: redirectPath } : undefined
        });
      }
      return; // 重要：这里要 return
    }
    
    // 3. 其他页面直接放行
    next();
    
  } catch (error) {
    console.error('路由守卫错误:', error);
    next('/login');
  }
};

/**
 * 初始化路由守卫
 */
export const setupRouterGuards = (router: any) => {
  // 使用全局前置守卫
  router.beforeEach(globalGuard);
};
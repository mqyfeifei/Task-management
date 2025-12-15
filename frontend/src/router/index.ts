import { createRouter, createWebHistory } from 'vue-router';
import { requireAuth, redirectIfLoggedIn } from './authGuard';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { 
      path: '/', 
      redirect: '/login' 
    },

    { 
      path: '/login', 
      component: () => import('../views/LoginView.vue') ,
      beforeEnter: redirectIfLoggedIn
    },
    { 
      path: '/register', 
      component: () => import('../views/RegisterView.vue') 
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    { 
      path: '/tasks', 
      component: () => import('../views/TaskView.vue'),
      meta: { requireAuth: true },
      beforeEnter: requireAuth
    },
    { 
      path: '/data', 
      name: 'Data',
      component: () => import('../views/DataView.vue'),
      meta: { requireAuth: true } 
    },  
    {
      path: '/settings',
      name: 'Settings',
      component: () => import('@/views/SettingsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/OperationRecord',
      name: 'OperationRecord',
      component: () => import('@/views/OperationRecord.vue'),
      meta: { 
        requiresAuth: true,
        title: '数据管理',
        icon: 'data-line'  // 根据你的图标库调整
      }
}
  ]
});

// 路由守卫：未登录拦截
router.beforeEach((to, from, next) => {
  requireAuth(to, from, next);
  const token = localStorage.getItem('token'); // 实际项目用token
  if (to.meta.requireAuth && !token) {
    next('/login');
  } else {
    next();
  }
});

export default router;
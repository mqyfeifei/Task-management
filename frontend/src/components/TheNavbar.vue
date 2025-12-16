<template>
  <div class="navbar">
    <div class="navbar-content">
      <div class="navbar-brand">
        <el-icon :size="24" color="#fff"><Calendar /></el-icon>
        <span class="brand-text">任务管理系统</span>
      </div>
      
      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        class="navbar-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
        <el-menu-item index="/data">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
      </el-menu>

      <div class="navbar-user">
        <el-dropdown @command="handleCommand" trigger="click">
          <div class="user-trigger">
            <el-avatar :size="32" class="user-avatar">
              {{ username.charAt(0).toUpperCase() }}
            </el-avatar>
            <span class="username">{{ username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                系统设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Calendar,
  List,
  DataAnalysis,
  User,
  Setting,
  SwitchButton,
  ArrowDown
} from '@element-plus/icons-vue';
import { clearUserInfo, getCurrentUser,getCurrentUserId  } from '@/utils/auth';
import { userAPI } from '@/api';

const router = useRouter();
const route = useRoute();


const username = ref('用户');
const activeMenu = ref('/tasks');

const initUserInfo = async () => {
  const user = getCurrentUser();
  if (user?.username) {
    username.value = user.username;
  }
  
  // 获取用户ID
  const userId = getCurrentUserId();

  if (!userId) {
    ElMessage.warning('用户ID不存在');
    return;
  }

  try {
    const res = await userAPI.getUserInfo(userId);
    if (res.success && res.data) {
      username.value = res.data.username || '用户';
    } else {
      ElMessage.warning(res.message || '获取用户信息失败');
    }
  } catch (error) {
    console.error('加载用户信息失败:', error);
    ElMessage.error('加载用户信息失败');
  }
};


// 更新激活菜单
const updateActiveMenu = () => {
  const path = route.path;
  if (path.startsWith('/tasks')) {
    activeMenu.value = '/tasks';
  } else if (path.startsWith('/data')) {
    activeMenu.value = '/data';
  }
};

// 监听路由变化
watch(() => route.path, () => {
  updateActiveMenu();
}, { immediate: true });

// 菜单选择处理
const handleMenuSelect = (index: string) => {
  router.push(index);
};

// 命令处理
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile');
      break;
    case 'settings':
      router.push('/settings');
      break;
    case 'logout':
      handleLogout();
      break;
  }
};

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    clearUserInfo();
    ElMessage.success('已退出登录');
    router.push('/login');
  } catch {
    // 用户取消
  }
};

// 初始化
initUserInfo();
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-right: 40px;
  cursor: pointer;
}

.brand-text {
  font-size: 18px;
  font-weight: 600;
  color: white;
  white-space: nowrap;
}

.navbar-menu {
  flex: 1;
  background: transparent !important;
  border: none !important;
}

.navbar-menu :deep(.el-menu-item) {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.8) !important;
  border: none !important;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 20px;
}

.navbar-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}

.navbar-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.2) !important;
  color: white !important;
  font-weight: 600;
}

.navbar-user {
  margin-left: 20px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  cursor: pointer;
  color: white;
  border-radius: 20px;
  transition: background 0.3s;
}

.user-trigger:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: 2px solid white;
  font-size: 14px;
  font-weight: bold;
}

.username {
  font-size: 14px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-content {
    padding: 0 12px;
  }
  
  .brand-text {
    display: none;
  }
  
  .navbar-brand {
    margin-right: 20px;
  }
  
  .navbar-menu :deep(.el-menu-item span) {
    display: none;
  }
  
  .username {
    display: none;
  }
}
</style>
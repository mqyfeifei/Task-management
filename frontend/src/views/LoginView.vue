<template>
  <div class="auth-page">
    <div class="background-overlay"></div>
    
    <div class="auth-container">
      <div class="auth-card">
        <div class="card-header">
          <div class="logo-container">
            <el-icon :size="48" color="#667eea"><Checked /></el-icon>
          </div>
          <h2>任务管理系统</h2>
          <p class="subtitle">欢迎回来，请登录您的账户</p>
        </div>

        <el-form 
          :model="loginForm" 
          :rules="loginRules"
          ref="loginFormRef"
          class="auth-form"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
              clearable
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleLogin"
              :loading="loading"
              size="large"
              class="submit-btn"
            >
              <el-icon v-if="!loading"><Right /></el-icon>
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="card-footer">
          <el-divider>还没有账户？</el-divider>
          <el-button 
            text 
            @click="goRegister"
            class="link-btn"
          >
            立即注册 →
          </el-button>
        </div>
      </div>

      <div class="features">
        <div class="feature-item">
          <el-icon :size="24" color="#667eea"><Document /></el-icon>
          <span>任务管理</span>
        </div>
        <div class="feature-item">
          <el-icon :size="24" color="#67c23a"><TrendCharts /></el-icon>
          <span>数据分析</span>
        </div>
        <div class="feature-item">
          <el-icon :size="24" color="#e6a23c"><User /></el-icon>
          <span>个人中心</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { User, Lock, Right, Checked, Document, TrendCharts } from '@element-plus/icons-vue';
import { userAPI } from '@/api';
import { setUserInfo } from '@/utils/auth';
import type { ApiResponse } from '@/types';

const router = useRouter();
const loginFormRef = ref<FormInstance>();
const loading = ref(false);

const loginForm = ref({
  username: '',
  password: ''
});

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  // 验证表单
  const valid = await loginFormRef.value.validate().catch(() => false);
  if (!valid) return;
  
  loading.value = true;
  
  try {
    const res: ApiResponse<string> = await userAPI.login(loginForm.value);
    
    if (res.success && res.data) {
      ElMessage.success(res.message || '登录成功');
      setUserInfo(res.data);
      
      // 延迟跳转以显示成功消息
      setTimeout(() => {
        router.push('/tasks');
      }, 500);
    } else {
      ElMessage.error(res.message || '登录失败');
    }
  } catch (error) {
    console.error('登录请求失败：', error);
    ElMessage.error('网络请求失败，请检查连接');
  } finally {
    loading.value = false;
  }
};

const goRegister = () => {
  router.push('/register');
};
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.auth-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
}

.auth-card {
  background: white;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-container {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
}

.card-header h2 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 12px 0;
  font-weight: 600;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.auth-form {
  margin-bottom: 24px;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.auth-form :deep(.el-input__wrapper) {
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
}

.auth-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #667eea inset;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.4);
}

.submit-btn:active {
  transform: translateY(0);
}

.card-footer {
  text-align: center;
  margin-top: 32px;
}

.card-footer :deep(.el-divider__text) {
  background: white;
  color: #909399;
  font-size: 14px;
}

.link-btn {
  font-size: 15px;
  color: #667eea;
  font-weight: 500;
  margin-top: 12px;
  transition: all 0.3s;
}

.link-btn:hover {
  color: #764ba2;
  transform: translateX(4px);
}

.features {
  display: flex;
  justify-content: space-around;
  margin-top: 40px;
  padding: 0 20px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: white;
  font-size: 14px;
  opacity: 0.9;
  transition: all 0.3s;
}

.feature-item:hover {
  opacity: 1;
  transform: translateY(-4px);
}

@media (max-width: 480px) {
  .auth-card {
    padding: 32px 24px;
  }
  
  .card-header h2 {
    font-size: 24px;
  }
  
  .features {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
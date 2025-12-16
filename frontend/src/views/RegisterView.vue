<template>
  <div class="auth-page">
    <div class="background-overlay"></div>
    
    <div class="auth-container">
      <div class="auth-card">
        <div class="card-header">
          <div class="logo-container">
            <el-icon :size="48" color="#667eea"><UserFilled /></el-icon>
          </div>
          <h2>创建新账户</h2>
          <p class="subtitle">加入任务管理系统，开启高效工作</p>
        </div>

        <el-form 
          :model="registerForm" 
          :rules="registerRules"
          ref="registerFormRef"
          class="auth-form"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="registerForm.username" 
              placeholder="请输入用户名 (3-20个字符)"
              :prefix-icon="User"
              size="large"
              clearable
              maxlength="20"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="请输入密码 (至少6位)"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleRegister"
            />
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleRegister"
              :loading="loading"
              size="large"
              class="submit-btn"
            >
              <el-icon v-if="!loading"><CirclePlus /></el-icon>
              {{ loading ? '注册中...' : '注册账户' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="card-footer">
          <el-divider>已有账户？</el-divider>
          <el-button 
            text 
            @click="goLogin"
            class="link-btn"
          >
            ← 返回登录
          </el-button>
        </div>
      </div>

      <div class="features">
        <div class="feature-item">
          <el-icon :size="24" color="#667eea"><Box /></el-icon>
          <span>树形任务</span>
        </div>
        <div class="feature-item">
          <el-icon :size="24" color="#67c23a"><DataAnalysis /></el-icon>
          <span>可视化统计</span>
        </div>
        <div class="feature-item">
          <el-icon :size="24" color="#e6a23c"><Timer /></el-icon>
          <span>时间管理</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { 
  User, 
  Lock, 
  UserFilled, 
  CirclePlus, 
  Box, 
  DataAnalysis, 
  Timer 
} from '@element-plus/icons-vue';
import { userAPI } from '@/api';
import type { ApiResponse } from '@/types';

const router = useRouter();
const registerFormRef = ref<FormInstance>();
const loading = ref(false);

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
});

// 表单验证规则
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { 
      pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, 
      message: '用户名只能包含字母、数字、下划线和中文', 
      trigger: 'blur' 
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
    { 
      pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, 
      message: '密码必须包含字母和数字', 
      trigger: 'blur' 
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

const handleRegister = async () => {
  if (!registerFormRef.value) return;
  
  // 验证表单
  const valid = await registerFormRef.value.validate().catch(() => false);
  if (!valid) return;
  
  loading.value = true;
  
  try {
    // 提交注册数据（不包含 confirmPassword）
    const res: ApiResponse = await userAPI.register({
      username: registerForm.value.username,
      password: registerForm.value.password
    });
    
    if (res.success) {
      ElMessage.success(res.message || '注册成功，即将跳转到登录页');
      
      // 延迟跳转到登录页
      setTimeout(() => {
        router.push('/login');
      }, 1500);
    } else {
      ElMessage.error(res.message || '注册失败');
    }
  } catch (error) {
    console.error('注册请求失败：', error);
    ElMessage.error('网络请求失败，请检查连接');
  } finally {
    loading.value = false;
  }
};

const goLogin = () => {
  router.push('/login');
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
  transform: translateX(-4px);
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
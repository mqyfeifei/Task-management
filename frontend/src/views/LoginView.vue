<template>
  <div class="login-container">
    <el-card title="用户登录">
      <el-form :model="loginForm" label-width="80px" class="login-form">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <el-button @click="goRegister">注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userAPI } from '@/api';
import { setUserInfo } from '@/utils/auth';
import type { ApiResponse } from '@/types';

const loginForm = ref({
  username: '',
  password: ''
});
const router = useRouter();

const handleLogin = async () => {
  try {
    const res: ApiResponse<string> = await userAPI.login(loginForm.value);
    
    console.log("登录响应：", res);

    if (res.success && res.data) {  // 添加 res.data 检查
      ElMessage.success(res.message || '登录成功');
      setUserInfo(res.data);  // 现在 res.data 确定是 string
      router.push('/tasks');
    } else if (!res.success) {
      ElMessage.error(res.message || '登录失败');
    } else {
      // res.success 为 true 但 res.data 为 undefined 的情况
      ElMessage.error('登录成功但未获取到Token');
    }
  } catch (error) {
    console.error('登录请求失败：', error);
    ElMessage.error('网络请求失败，请检查连接');
  }
};

const goRegister = () => {
  router.push('/register');
};
</script>

<style scoped>
.login-container {
  width: 400px;
  margin: 100px auto;
}
.login-form {
  margin-top: 20px;
}
</style>
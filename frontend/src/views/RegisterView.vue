<template>
  <div class="register-container">
    <el-card title="用户注册">
      <el-form :model="registerForm" label-width="80px" class="register-form">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input v-model="registerForm.signature" placeholder="可选" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister">注册</el-button>
          <el-button @click="goLogin">返回登录</el-button>
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
import type { ApiResponse } from '@/types';

const registerForm = ref({
  username: '',
  password: '',
  signature: ''
});
const router = useRouter();

const handleRegister = async () => {
  try {
    const res: ApiResponse = await userAPI.register(registerForm.value);
    console.log("注册响应mqy：", res);
    if (res.success) {
      ElMessage.success(res.message || '注册成功');
      router.push('/login');
    } else {
      console.error('注册失败：', res);
      ElMessage.error(res.message || '注册失败');
    }
  } catch (error) {
    console.error('注册请求失败：', error);
  }
};

const goLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.register-container {
  width: 400px;
  margin: 100px auto;
}
.register-form {
  margin-top: 20px;
}
</style>
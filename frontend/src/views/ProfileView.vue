<template>
  <div class="profile-page">
    <TheNavbar />
    
    <div class="container">
      <div class="header">
        <h2>个人中心</h2>
      </div>

      <div class="profile-card" v-loading="loading">
        <div class="profile-header">
          <el-avatar :size="100" :src="avatarUrl" class="profile-avatar">
            {{ userInfo.username?.charAt(0).toUpperCase() || 'U' }}
          </el-avatar>
          
          <div class="profile-info">
            <h3>{{ userInfo.username || '未设置用户名' }}</h3>
            <div class="user-meta">
              <span class="user-id">ID: {{ userInfo.userId || '--' }}</span>
              <el-tag type="success" size="small">活跃用户</el-tag>
            </div>
            <p class="user-signature">
              {{ userInfo.signature || '这个人很懒，什么都没写~' }}
            </p>
          </div>
          
          <div class="action-buttons">
            <el-button 
              type="primary" 
              @click="showEditDialog = true"
              :icon="Edit"
            >
              编辑资料
            </el-button>
            <el-button 
              type="warning" 
              @click="showPasswordDialog = true"
              :icon="Lock"
            >
              修改密码
            </el-button>
          </div>
        </div>

        <el-divider />

        <div class="profile-details">
          <div class="detail-grid">
            <div class="detail-item">
              <div class="detail-icon">
                <el-icon color="#409eff"><User /></el-icon>
              </div>
              <div class="detail-content">
                <div class="detail-label">用户名</div>
                <div class="detail-value">{{ userInfo.username || '--' }}</div>
              </div>
            </div>

            <div class="detail-item">
              <div class="detail-icon">
                <el-icon color="#67c23a"><Picture /></el-icon>
              </div>
              <div class="detail-content">
                <div class="detail-label">头像</div>
                <div class="detail-value">{{ avatarUrl ? '已设置' : '未设置' }}</div>
              </div>
            </div>

            <div class="detail-item">
              <div class="detail-icon">
                <el-icon color="#e6a23c"><Calendar /></el-icon>
              </div>
              <div class="detail-content">
                <div class="detail-label">注册时间</div>
                <div class="detail-value">{{ formatDate(userInfo.registerTime) }}</div>
              </div>
            </div>

            <div class="detail-item">
              <div class="detail-icon">
                <el-icon color="#909399"><Document /></el-icon>
              </div>
              <div class="detail-content">
                <div class="detail-label">个性签名</div>
                <div class="detail-value">{{ userInfo.signature || '未设置' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 编辑资料对话框 -->
      <el-dialog 
        v-model="showEditDialog" 
        title="编辑资料"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form :model="editForm" label-width="90px">
          <el-form-item label="用户名">
            <el-input 
              v-model="editForm.username" 
              placeholder="请输入用户名"
              maxlength="20"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="个性签名">
            <el-input 
              v-model="editForm.signature" 
              type="textarea"
              :rows="3"
              placeholder="请输入个性签名"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="用户头像">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <img v-if="avatarPreview" :src="avatarPreview" class="avatar" />
              <el-avatar v-else-if="avatarUrl" :size="120" :src="avatarUrl" />
              <div v-else class="avatar-uploader-icon">
                <el-icon :size="28"><Plus /></el-icon>
                <div class="upload-text">点击上传</div>
              </div>
            </el-upload>
            <div class="upload-tip">支持 jpg、png 格式，大小不超过 2MB</div>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="cancelEdit">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveProfile" 
            :loading="saveLoading"
          >
            保存
          </el-button>
        </template>
      </el-dialog>

      <!-- 修改密码对话框 -->
      <el-dialog 
        v-model="showPasswordDialog" 
        title="修改密码"
        width="450px"
        :close-on-click-modal="false"
      >
        <el-form 
          :model="passwordForm" 
          :rules="passwordRules"
          ref="passwordFormRef"
          label-width="90px"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input 
              v-model="passwordForm.oldPassword" 
              type="password"
              placeholder="请输入原密码"
              show-password
            />
          </el-form-item>
          
          <el-form-item label="新密码" prop="newPassword">
            <el-input 
              v-model="passwordForm.newPassword" 
              type="password"
              placeholder="请输入新密码(至少6位)"
              show-password
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="passwordForm.confirmPassword" 
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="cancelPasswordChange">取消</el-button>
          <el-button 
            type="primary" 
            @click="changePassword" 
            :loading="passwordLoading"
          >
            确认修改
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import type { UploadRequestOptions } from 'element-plus';
import { 
  Edit, 
  User, 
  Picture,
  Calendar, 
  Document,
  Plus,
  Lock
} from '@element-plus/icons-vue';
import { getCurrentUserId } from '@/utils/auth';
import { userAPI } from '@/api';
import TheNavbar from '@/components/TheNavbar.vue';

interface UserInfo {
  userId?: string;
  username?: string;
  signature?: string;
  avatarUrl?: string;
  registerTime?: string;
}

const userId = getCurrentUserId();
const userInfo = ref<UserInfo>({});
const showEditDialog = ref(false);
const showPasswordDialog = ref(false);
const loading = ref(false);
const saveLoading = ref(false);
const passwordLoading = ref(false);
const avatarPreview = ref('');
const passwordFormRef = ref<FormInstance>();

const editForm = reactive({
  username: '',
  signature: ''
});

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 密码验证规则
const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 头像 URL（修复路径拼接）
const avatarUrl = computed(() => {
  if (avatarPreview.value) {
    return avatarPreview.value;
  }
  if (userInfo.value.avatarUrl) {
    // 直接使用完整的本地路径
    return `${userInfo.value.avatarUrl}`;
  }
  return '';
});

// 加载用户信息
const loadUserInfo = async () => {
  if (!userId) {
    ElMessage.warning('请先登录');
    return;
  }
  
  loading.value = true;
  
  try {
    const res = await userAPI.getUserInfo(userId);
    
    if (res.success && res.data) {
      userInfo.value = res.data;
      
      // 初始化编辑表单
      editForm.username = res.data.username || '';
      editForm.signature = res.data.signature || '';
    } else {
      ElMessage.warning(res.message || '获取用户信息失败');
    }
  } catch (error) {
    console.error('加载用户信息失败:', error);
    ElMessage.error('加载用户信息失败');
  } finally {
    loading.value = false;
  }
};

// 格式化日期
const formatDate = (dateStr?: string): string => {
  if (!dateStr) return '--';
  try {
    return new Date(dateStr).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch {
    return '--';
  }
};

// 上传前检查
const beforeAvatarUpload = (rawFile: File): boolean => {
  const isImage = rawFile.type.startsWith('image/');
  const isLt2M = rawFile.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件！');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！');
    return false;
  }
  return true;
};

// 自定义上传
const handleAvatarUpload = async (options: UploadRequestOptions) => {
  const file = options.file as File;
  
  // 预览图片
  const reader = new FileReader();
  reader.onload = (e) => {
    avatarPreview.value = e.target?.result as string;
  };
  reader.readAsDataURL(file);
  
  // 暂存文件，等待保存时再上传
  (editForm as any).avatarFile = file;
};

// 保存资料
const saveProfile = async () => {
  if (!editForm.username.trim()) {
    ElMessage.warning('请输入用户名');
    return;
  }
  
  if (!userId) {
    ElMessage.warning('用户ID不存在');
    return;
  }
  
  saveLoading.value = true;
  
  try {
    let avatarUrl = userInfo.value.avatarUrl;
    
    // 如果有新上传的头像，先上传
    if ((editForm as any).avatarFile) {
      const uploadRes = await userAPI.uploadAvatar((editForm as any).avatarFile, userId);
      if (uploadRes.success && uploadRes.data) {
        avatarUrl = uploadRes.data;
      } else {
        ElMessage.error(uploadRes.message || '头像上传失败');
        return;
      }
    }
    
    // 更新用户信息
    const updateRes = await userAPI.updateUserInfo({
      userId: userId,
      username: editForm.username,
      signature: editForm.signature,
      avatarUrl: avatarUrl
    });
    
    if (updateRes.success) {
      ElMessage.success('资料保存成功');
      showEditDialog.value = false;
      avatarPreview.value = '';
      delete (editForm as any).avatarFile;
      
      // 重新加载用户信息
      await loadUserInfo();
    } else {
      ElMessage.error(updateRes.message || '保存失败');
    }
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败');
  } finally {
    saveLoading.value = false;
  }
};

// 取消编辑
const cancelEdit = () => {
  showEditDialog.value = false;
  avatarPreview.value = '';
  delete (editForm as any).avatarFile;
  
  // 重置表单
  editForm.username = userInfo.value.username || '';
  editForm.signature = userInfo.value.signature || '';
};

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return;
  
  // 验证表单
  const valid = await passwordFormRef.value.validate().catch(() => false);
  if (!valid) return;
  
  if (!userId) {
    ElMessage.warning('用户ID不存在');
    return;
  }
  
  passwordLoading.value = true;
  
  try {
    const res = await userAPI.changePassword({
      userId: userId,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });
    
    if (res.success) {
      ElMessage.success('密码修改成功，请重新登录');
      showPasswordDialog.value = false;
      
      // 清空表单
      passwordForm.oldPassword = '';
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';
      
      // 延迟跳转到登录页
      setTimeout(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        window.location.href = '/login';
      }, 1500);
    } else {
      ElMessage.error(res.message || '密码修改失败');
    }
  } catch (error) {
    console.error('修改密码失败:', error);
    ElMessage.error('修改密码失败');
  } finally {
    passwordLoading.value = false;
  }
};

// 取消修改密码
const cancelPasswordChange = () => {
  showPasswordDialog.value = false;
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordFormRef.value?.clearValidate();
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 20px 40px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h2 {
  font-size: 32px;
  color: white;
  font-weight: 600;
  margin: 0;
}

.profile-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 30px;
}

.profile-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 36px;
  font-weight: bold;
  color: white;
  border: 4px solid rgba(102, 126, 234, 0.1);
}

.profile-info {
  flex: 1;
}

.profile-info h3 {
  font-size: 24px;
  margin: 0 0 12px 0;
  color: #303133;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-id {
  font-size: 14px;
  color: #909399;
}

.user-signature {
  font-size: 14px;
  color: #606266;
  margin: 0;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.profile-details {
  padding: 24px 0 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.detail-item:hover {
  background: #e8eaf0;
  transform: translateY(-2px);
}

.detail-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 8px;
  font-size: 20px;
}

.detail-content {
  flex: 1;
}

.detail-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.detail-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* 头像上传样式 */
.avatar-uploader {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #667eea;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}

.avatar-uploader-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 8px;
}

.upload-text {
  font-size: 12px;
}

.upload-tip {
  text-align: center;
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 70px 15px 30px;
  }
  
  .header h2 {
    font-size: 24px;
  }
  
  .profile-card {
    padding: 24px;
  }
  
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .action-buttons {
    width: 100%;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
<template>
  <div class="settings-page">
    <TheNavbar />
    
    <div class="container">
      <!-- 页面标题 -->
      <div class="header">
        <h2>系统设置</h2>
      </div>

      <!-- 设置卡片 -->
      <div class="settings-card">
        <!-- 通知设置 -->
        <div class="setting-section">
          <div class="section-header">
            <el-icon :size="20" color="#409eff"><Bell /></el-icon>
            <h3>通知设置</h3>
          </div>
          <div class="setting-items">
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">任务提醒</div>
                <div class="setting-desc">任务截止前发送提醒通知</div>
              </div>
              <el-switch v-model="settings.taskReminder" />
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">邮件通知</div>
                <div class="setting-desc">重要任务通过邮件通知</div>
              </div>
              <el-switch v-model="settings.emailNotification" />
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">浏览器通知</div>
                <div class="setting-desc">允许浏览器推送通知</div>
              </div>
              <el-switch v-model="settings.browserNotification" />
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 主题设置 -->
        <div class="setting-section">
          <div class="section-header">
            <el-icon :size="20" color="#67c23a"><Brush /></el-icon>
            <h3>主题设置</h3>
          </div>
          <div class="setting-items">
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">深色模式</div>
                <div class="setting-desc">启用深色主题界面</div>
              </div>
              <el-switch v-model="settings.darkMode" />
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">主题颜色</div>
                <div class="setting-desc">选择您喜欢的主题颜色</div>
              </div>
              <el-color-picker v-model="settings.themeColor" />
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">紧凑模式</div>
                <div class="setting-desc">减少界面元素间距</div>
              </div>
              <el-switch v-model="settings.compactMode" />
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 隐私设置 -->
        <div class="setting-section">
          <div class="section-header">
            <el-icon :size="20" color="#e6a23c"><Lock /></el-icon>
            <h3>隐私与安全</h3>
          </div>
          <div class="setting-items">
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">数据统计</div>
                <div class="setting-desc">允许收集匿名使用数据</div>
              </div>
              <el-switch v-model="settings.analytics" />
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">自动登录</div>
                <div class="setting-desc">保持登录状态</div>
              </div>
              <el-switch v-model="settings.autoLogin" />
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 数据管理 -->
        <div class="setting-section">
          <div class="section-header">
            <el-icon :size="20" color="#f56c6c"><Document /></el-icon>
            <h3>数据管理</h3>
          </div>
          <div class="setting-items">
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">导出数据</div>
                <div class="setting-desc">导出所有任务数据</div>
              </div>
              <el-button size="small" @click="exportData">
                导出
              </el-button>
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">清除缓存</div>
                <div class="setting-desc">清除本地缓存数据</div>
              </div>
              <el-button size="small" @click="clearCache">
                清除
              </el-button>
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-label">删除账号</div>
                <div class="setting-desc">永久删除账号及所有数据</div>
              </div>
              <el-button size="small" type="danger" @click="deleteAccount">
                删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 保存按钮 -->
        <div class="setting-actions">
          <el-button 
            type="primary" 
            size="large"
            @click="saveSettings"
            :loading="loading"
          >
            保存设置
          </el-button>
          <el-button 
            size="large"
            @click="resetSettings"
          >
            恢复默认
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Bell, 
  Brush, 
  Lock, 
  Document 
} from '@element-plus/icons-vue';
import TheNavbar from '@/components/TheNavbar.vue';

const loading = ref(false);

const settings = reactive({
  taskReminder: true,
  emailNotification: false,
  browserNotification: true,
  darkMode: false,
  themeColor: '#667eea',
  compactMode: false,
  analytics: true,
  autoLogin: true
});

// 保存设置
const saveSettings = async () => {
  loading.value = true;
  
  try {
    // 模拟保存操作
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // 保存到 localStorage
    localStorage.setItem('userSettings', JSON.stringify(settings));
    
    ElMessage.success('设置保存成功');
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败');
  } finally {
    loading.value = false;
  }
};

// 恢复默认设置
const resetSettings = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要恢复默认设置吗？',
      '恢复确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    // 恢复默认值
    Object.assign(settings, {
      taskReminder: true,
      emailNotification: false,
      browserNotification: true,
      darkMode: false,
      themeColor: '#667eea',
      compactMode: false,
      analytics: true,
      autoLogin: true
    });

    ElMessage.success('已恢复默认设置');
  } catch {
    // 用户取消
  }
};

// 导出数据
const exportData = async () => {
  try {
    ElMessage.info('正在导出数据...');
    
    // 模拟导出
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    ElMessage.success('数据导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败');
  }
};

// 清除缓存
const clearCache = async () => {
  try {
    await ElMessageBox.confirm(
      '清除缓存后需要重新登录，确定继续吗？',
      '清除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    // 清除缓存
    localStorage.clear();
    sessionStorage.clear();
    
    ElMessage.success('缓存已清除，即将跳转到登录页');
    
    setTimeout(() => {
      window.location.href = '/login';
    }, 1500);
  } catch {
    // 用户取消
  }
};

// 删除账号
const deleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      '删除账号将永久删除所有数据，此操作不可恢复！',
      '危险操作',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    );

    ElMessage.warning('此功能暂未开放');
  } catch {
    // 用户取消
  }
};

// 初始化加载设置
const loadSettings = () => {
  const saved = localStorage.getItem('userSettings');
  if (saved) {
    try {
      Object.assign(settings, JSON.parse(saved));
    } catch (error) {
      console.error('加载设置失败:', error);
    }
  }
};

loadSettings();
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.container {
  max-width: 900px;
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

.settings-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.setting-section {
  margin-bottom: 8px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.section-header h3 {
  font-size: 18px;
  margin: 0;
  color: #303133;
  font-weight: 600;
}

.setting-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.setting-item:hover {
  background: #e8eaf0;
}

.setting-info {
  flex: 1;
}

.setting-label {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 4px;
}

.setting-desc {
  font-size: 13px;
  color: #909399;
}

.setting-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid #e4e7ed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 70px 15px 30px;
  }
  
  .header h2 {
    font-size: 24px;
  }
  
  .settings-card {
    padding: 24px;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .setting-actions {
    flex-direction: column;
  }
  
  .setting-actions .el-button {
    width: 100%;
  }
}
</style>
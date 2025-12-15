<template>
  <div class="data-analysis-page">
    <TheNavbar />
    
    <div class="container">
      <!-- 页面标题 -->
      <div class="header">
        <h2>数据统计分析</h2>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid" v-loading="statsLoading">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总任务数</div>
            <div class="stat-value">{{ stats.totalTask || 0 }}</div>
          </div>
        </div>
        
        <div class="stat-card success">
          <div class="stat-icon">
            <el-icon :size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">已完成</div>
            <div class="stat-value">{{ stats.completedTask || 0 }}</div>
          </div>
        </div>
        
        <div class="stat-card warning">
          <div class="stat-icon">
            <el-icon :size="32"><DataLine /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">完成率</div>
            <div class="stat-value">{{ formatRate(stats.completionRate) }}</div>
          </div>
        </div>
        
        <div class="stat-card info">
          <div class="stat-icon">
            <el-icon :size="32"><List /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">操作记录</div>
            <div class="stat-value">{{ stats.operationCount || 0 }}</div>
          </div>
        </div>
      </div>

      <!-- 操作记录表格 -->
      <div class="records-section">
        <div class="section-header">
          <h3>近期操作记录</h3>
          <el-button @click="loadRecords" :icon="Refresh" circle />
        </div>
        
        <el-table 
          :data="records" 
          v-loading="recordsLoading"
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
          empty-text="暂无操作记录"
        >
          <el-table-column prop="operationTime" label="操作时间" width="200">
            <template #default="{ row }">
              <el-icon style="margin-right: 5px"><Clock /></el-icon>
              {{ formatTime(row.operationTime) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="operationType" label="操作类型" width="150">
            <template #default="{ row }">
              <el-tag :type="getOperationTypeTag(row.operationType)">
                {{ row.operationType }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="operationDetail" label="操作详情" show-overflow-tooltip />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { 
  Document, 
  CircleCheck, 
  DataLine, 
  List, 
  Clock,
  Refresh
} from '@element-plus/icons-vue';
import { analysisAPI } from '@/api';
import { getCurrentUserId } from '@/utils/auth';
import type { StatsData, OperationRecord, ApiResponse } from '@/types';
import TheNavbar from '@/components/TheNavbar.vue';

const router = useRouter();
const userId = getCurrentUserId();

// 响应式数据
const stats = ref<StatsData>({
  totalTask: 0,
  completedTask: 0,
  completionRate: '0',
  operationCount: 0
});

const records = ref<OperationRecord[]>([]);
const statsLoading = ref(false);
const recordsLoading = ref(false);

// 获取统计数据
const loadStats = async () => {
  if (!userId) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  statsLoading.value = true;
  
  try {
    const res: ApiResponse<StatsData> = await analysisAPI.getTaskStats(userId);
    
    if (res.success && res.data) {
      stats.value = {
        totalTask: res.data.totalTask || 0,
        completedTask: res.data.completedTask || 0,
        completionRate: res.data.completionRate || '0',
        operationCount: res.data.operationCount || 0
      };
    } else {
      ElMessage.warning(res.message || '获取统计数据失败');
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
    ElMessage.error('加载统计数据失败');
  } finally {
    statsLoading.value = false;
  }
};

// 获取操作记录
const loadRecords = async () => {
  if (!userId) return;
  
  recordsLoading.value = true;
  
  try {
    const res: ApiResponse<OperationRecord[]> = await analysisAPI.getOperationRecords(userId);
    
    if (res.success && res.data) {
      records.value = res.data;
    } else {
      ElMessage.warning(res.message || '获取操作记录失败');
      records.value = [];
    }
  } catch (error) {
    console.error('加载操作记录失败:', error);
    ElMessage.error('加载操作记录失败');
    records.value = [];
  } finally {
    recordsLoading.value = false;
  }
};

// 格式化完成率
const formatRate = (rate: string | number | undefined): string => {
  if (!rate) return '0%';
  
  if (typeof rate === 'number') {
    return `${rate.toFixed(1)}%`;
  }
  
  if (typeof rate === 'string') {
    return rate.includes('%') ? rate : `${rate}%`;
  }
  
  return '0%';
};

// 格式化时间
const formatTime = (time: string): string => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 根据操作类型返回标签样式
const getOperationTypeTag = (type: string): string => {
  const typeMap: Record<string, string> = {
    '新增任务': 'success',
    '完成任务': 'primary',
    '删除任务': 'danger',
    '修改任务': 'warning'
  };
  return typeMap[type] || 'info';
};

onMounted(() => {
  loadStats();
  loadRecords();
});
</script>

<style scoped>
.data-analysis-page {
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

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 12px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card.success .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-card.warning .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card.info .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

/* 操作记录区域 */
.records-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 20px;
  margin: 0;
  color: #303133;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 16px 0;
}

:deep(.el-table__empty-text) {
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
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .records-section {
    padding: 20px 15px;
  }
}
</style>
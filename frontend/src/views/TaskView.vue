<template>
  <div class="task-page">
    <TheNavbar />
    
    <div class="container">
      <!-- 页面标题 -->
      <div class="header">
        <h2>任务管理</h2>
        <div class="header-actions">
          <el-button 
            type="primary" 
            @click="showAddDialog = true"
            :icon="Plus"
            size="large"
          >
            新增任务
          </el-button>
          <el-button 
            @click="loadTasks"
            :icon="Refresh"
            size="large"
            :loading="loading"
            circle
          />
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid" v-if="taskList.length > 0">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总任务数</div>
            <div class="stat-value">{{ taskList.length }}</div>
          </div>
        </div>
        
        <div class="stat-card success">
          <div class="stat-icon">
            <el-icon :size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">已完成</div>
            <div class="stat-value">{{ completedCount }}</div>
          </div>
        </div>
        
        <div class="stat-card warning">
          <div class="stat-icon">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">进行中</div>
            <div class="stat-value">{{ pendingCount }}</div>
          </div>
        </div>
      </div>

      <!-- 任务树形列表 -->
      <div class="task-section">
        <div class="section-header">
          <div class="section-title">
            <el-icon><Folder /></el-icon>
            <h3>我的任务</h3>
            <el-tag v-if="taskList.length > 0" type="info" size="small">
              {{ taskList.length }} 个任务
            </el-tag>
          </div>
        </div>
        
        <div v-if="taskList.length === 0" class="empty-state">
          <el-empty description="暂无任务，开始创建你的第一个任务吧！">
            <el-button type="primary" @click="showAddDialog = true">
              创建任务
            </el-button>
          </el-empty>
        </div>
        
        <el-tree
          v-else
          v-loading="loading"
          :data="taskList"
          :props="treeProps"
          node-key="taskId"
          default-expand-all
          :expand-on-click-node="false"
          @node-click="handleNodeClick"
          class="task-tree"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-info">
                <el-icon 
                  class="node-icon" 
                  :color="data.completionStatus === 1 ? '#67c23a' : '#e6a23c'"
                >
                  <CircleCheck v-if="data.completionStatus === 1" />
                  <Clock v-else />
                </el-icon>
                
                <span 
                  class="node-label" 
                  :class="{ 
                    'completed': data.completionStatus === 1,
                    'important': data.completionStatus === 0 && isImportantTask(data)
                  }"
                >
                  {{ node.label }}
                </span>
                
                <div v-if="data.endTime" class="deadline-tag">
                  <el-icon size="12"><Calendar /></el-icon>
                  <span>{{ formatDeadline(data.endTime) }}</span>
                </div>
              </div>
              
              <div class="node-actions">
                <el-button 
                  size="small" 
                  @click.stop="handleComplete(data)"
                  :disabled="data.completionStatus === 1"
                  :type="data.completionStatus === 1 ? 'success' : 'primary'"
                  plain
                >
                  <el-icon v-if="data.completionStatus === 1"><Check /></el-icon>
                  {{ data.completionStatus === 1 ? '已完成' : '完成' }}
                </el-button>
                
                <el-button 
                  size="small" 
                  type="danger" 
                  @click.stop="handleDelete(data.taskId)"
                  plain
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>

      <!-- 新增任务对话框 -->
      <el-dialog 
        v-model="showAddDialog" 
        title="新增任务"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form :model="newTask" label-width="90px">
          <el-form-item label="任务内容" required>
            <el-input 
              v-model="newTask.taskContent" 
              placeholder="请输入任务内容"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="父任务">
            <el-select 
              v-model="newTask.parentTaskId" 
              placeholder="选择父任务（可选）"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="task in flattenTasks"
                :key="task.taskId"
                :label="task.taskContent"
                :value="task.taskId"
                :disabled="task.completionStatus === 1"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="截止时间">
            <el-date-picker 
              v-model="newTask.endTime" 
              type="datetime" 
              placeholder="选择截止时间（可选）"
              :disabled-date="disabledDate"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="addTask" 
            :loading="addLoading"
          >
            确认
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Plus,
  Refresh,
  Document, 
  CircleCheck, 
  Clock, 
  Folder, 
  Calendar, 
  Delete, 
  Check
} from '@element-plus/icons-vue';
import { taskAPI } from '@/api';
import { getCurrentUserId, getToken } from '@/utils/auth';
import type { Task, ApiResponse } from '@/types';
import TheNavbar from '@/components/TheNavbar.vue';
import dayjs from 'dayjs';

const router = useRouter();
const userId = getCurrentUserId();

// 响应式数据
const taskList = ref<Task[]>([]);
const showAddDialog = ref(false);
const addLoading = ref(false);
const loading = ref(false);

const treeProps = {
  label: 'taskContent',
  children: 'subTasks'
};

const newTask = ref({
  userId: userId || '',
  parentTaskId: null as number | null,
  taskContent: '',
  endTime: null as string | null
});

// 计算属性
const flattenTasks = computed(() => {
  const flatten = (tasks: Task[]): Task[] => {
    return tasks.reduce((acc: Task[], task) => {
      acc.push(task);
      if (task.subTasks?.length) {
        acc.push(...flatten(task.subTasks));
      }
      return acc;
    }, []);
  };
  return flatten(taskList.value);
});

const completedCount = computed(() => {
  return flattenTasks.value.filter(task => task.completionStatus === 1).length;
});

const pendingCount = computed(() => {
  return flattenTasks.value.filter(task => task.completionStatus === 0).length;
});

// 检查是否为重要任务
const isImportantTask = (task: Task): boolean => {
  if (!task.endTime) return false;
  const deadline = dayjs(task.endTime);
  const diffDays = deadline.diff(dayjs(), 'day');
  return diffDays <= 3 && diffDays >= 0;
};

// 格式化截止时间
const formatDeadline = (endTime: string): string => {
  const deadline = dayjs(endTime);
  const diffDays = deadline.diff(dayjs(), 'day');
  
  if (diffDays < 0) return '已过期';
  if (diffDays === 0) return '今天截止';
  if (diffDays === 1) return '明天截止';
  return `${diffDays}天后截止`;
};

// 加载任务列表
const loadTasks = async () => {
  if (!userId || !getToken()) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  loading.value = true;
  
  try {
    const res: ApiResponse<Task[]> = await taskAPI.getUserTasks(userId);
    
    if (res.success && res.data) {
      taskList.value = res.data;
    } else {
      ElMessage.warning(res.message || '获取任务列表失败');
      taskList.value = [];
    }
  } catch (error) {
    console.error('加载任务失败:', error);
    ElMessage.error('加载任务失败');
    taskList.value = [];
  } finally {
    loading.value = false;
  }
};

// 新增任务
const addTask = async () => {
  if (!newTask.value.taskContent.trim()) {
    ElMessage.warning('请输入任务内容');
    return;
  }
  
  addLoading.value = true;
  
  try {
    const res: ApiResponse<number> = await taskAPI.addTask({
      userId: newTask.value.userId,
      parentTaskId: newTask.value.parentTaskId,
      taskContent: newTask.value.taskContent,
      endTime: newTask.value.endTime
    });
    
    if (res.success) {
      ElMessage.success(res.message || '新增任务成功');
      showAddDialog.value = false;
      
      // 重置表单
      newTask.value.taskContent = '';
      newTask.value.parentTaskId = null;
      newTask.value.endTime = null;
      
      await loadTasks();
    } else {
      ElMessage.error(res.message || '新增任务失败');
    }
  } catch (error) {
    console.error('新增任务失败:', error);
    ElMessage.error('新增任务失败');
  } finally {
    addLoading.value = false;
  }
};

// 完成任务
const handleComplete = async (task: Task) => {
  if (task.completionStatus === 1) {
    ElMessage.info('该任务已完成');
    return;
  }

  try {
    const res: ApiResponse = await taskAPI.completeTask(task.taskId);
    if (res.success) {
      ElMessage.success('任务完成');
      await loadTasks();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (error) {
    console.error('完成任务失败:', error);
    ElMessage.error('操作失败');
  }
};

// 删除任务
const handleDelete = async (taskId: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个任务吗？删除后无法恢复，且会删除所有子任务。',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    const res: ApiResponse = await taskAPI.deleteTask(taskId);
    if (res.success) {
      ElMessage.success('删除成功');
      await loadTasks();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

// 节点点击
const handleNodeClick = (data: Task) => {
  console.log('选中任务:', data);
};

// 禁用过去的日期
const disabledDate = (time: Date): boolean => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000;
};

onMounted(() => {
  loadTasks();
});
</script>

<style scoped>
.task-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 20px 40px;
}

/* 页面标题 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.header h2 {
  font-size: 32px;
  color: white;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
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

/* 任务区域 */
.task-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title h3 {
  font-size: 20px;
  margin: 0;
  color: #303133;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

/* 树形结构 */
.task-tree {
  margin-top: 16px;
}

.task-tree :deep(.el-tree-node__content) {
  height: 60px;
  margin: 4px 0;
  border-radius: 8px;
  transition: all 0.3s;
}

.task-tree :deep(.el-tree-node__content:hover) {
  background: rgba(102, 126, 234, 0.08);
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 0 12px;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.node-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.node-label {
  font-size: 15px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.node-label.completed {
  color: #67c23a;
  text-decoration: line-through;
  opacity: 0.8;
}

.node-label.important {
  color: #e6a23c;
  font-weight: 600;
}

.deadline-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  white-space: nowrap;
}

.node-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.task-tree :deep(.el-tree-node__content:hover .node-actions) {
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 70px 15px 30px;
  }
  
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header h2 {
    font-size: 24px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .tree-node {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .node-actions {
    opacity: 1;
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
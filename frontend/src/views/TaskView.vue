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
            @click="openAddDialog"
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
            <div class="stat-value">{{ flattenTasks.length }}</div>
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

      <!-- 查询区域 -->
      <div class="search-section">
        <el-card shadow="hover">
          <div class="search-form">
            <el-input
              v-model="searchForm.taskContent"
              placeholder="输入任务名称搜索"
              :prefix-icon="Search"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
              style="width: 300px"
            />
            
            <el-select
              v-model="searchForm.completionStatus"
              placeholder="完成状态"
              clearable
              @change="handleSearch"
              style="width: 150px"
            >
              <el-option label="全部" :value="null" />
              <el-option label="未完成" :value="0" />
              <el-option label="已完成" :value="1" />
            </el-select>

            <el-button type="primary" @click="handleSearch" :icon="Search">
              搜索
            </el-button>
            <el-button @click="resetSearch" :icon="Refresh">
              重置
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 任务列表 -->
      <div class="task-section">
        <div class="section-header">
          <div class="section-title">
            <el-icon><Folder /></el-icon>
            <h3>我的任务</h3>
            <el-tag v-if="filteredTasks.length > 0" type="info" size="small">
              {{ filteredTasks.length }} 个任务
            </el-tag>
          </div>
          
          <div class="page-size-selector">
            <span style="margin-right: 8px; color: #606266;">每页显示：</span>
            <el-select v-model="pageSize" @change="handlePageSizeChange" style="width: 100px">
              <el-option :value="5" label="5 条" />
              <el-option :value="10" label="10 条" />
              <el-option :value="20" label="20 条" />
              <el-option :value="50" label="50 条" />
            </el-select>
          </div>
        </div>
        
        <div v-if="filteredTasks.length === 0" class="empty-state">
          <el-empty :description="searchForm.taskContent || searchForm.completionStatus !== null ? '没有找到匹配的任务' : '暂无任务，开始创建你的第一个任务吧！'">
            <el-button v-if="!searchForm.taskContent && searchForm.completionStatus === null" type="primary" @click="openAddDialog">
              创建任务
            </el-button>
          </el-empty>
        </div>
        
        <!-- 任务表格 -->
        <el-table
          v-else
          v-loading="loading"
          :data="paginatedTasks"
          style="width: 100%; margin-top: 16px"
          :row-class-name="tableRowClassName"
          :default-expand-all="false"
        >
          <el-table-column type="expand">
            <template #default="{ row }">
              <div v-if="row.subTasks && row.subTasks.length > 0" class="subtask-container">
                <div class="subtask-header">
                  <el-icon><Folder /></el-icon>
                  <span>子任务列表 ({{ row.subTasks.length }})</span>
                </div>
                <el-table :data="row.subTasks" style="width: 100%">
                  <el-table-column prop="taskContent" label="子任务名称" min-width="200" />
                  <el-table-column label="开始时间" width="180">
                    <template #default="{ row }">
                      {{ formatDateTime(row.startTime) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="结束时间" width="180">
                    <template #default="{ row }">
                      {{ formatDateTime(row.endTime) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="完成状态" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag :type="row.completionStatus === 1 ? 'success' : 'warning'" size="small">
                        {{ row.completionStatus === 1 ? '已完成' : '未完成' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="280" align="center" fixed="right">
                    <template #default="{ row }">
                      <el-button 
                        size="small" 
                        @click="toggleComplete(row)"
                        :type="row.completionStatus === 1 ? 'success' : 'primary'"
                        plain
                      >
                        {{ row.completionStatus === 1 ? '已完成' : '完成' }}
                      </el-button>
                      <el-button 
                        size="small" 
                        type="warning" 
                        @click="openEditDialog(row)"
                        plain
                      >
                        编辑
                      </el-button>
                      <el-button 
                        size="small" 
                        type="danger" 
                        @click="handleDelete(row.taskId)"
                        plain
                      >
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              <div v-else class="no-subtasks">
                <el-empty description="暂无子任务" :image-size="80" />
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="taskContent" label="任务名称" min-width="250" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="task-content-cell">
                <el-icon 
                  :color="row.completionStatus === 1 ? '#67c23a' : '#e6a23c'"
                  style="margin-right: 8px"
                >
                  <CircleCheck v-if="row.completionStatus === 1" />
                  <Clock v-else />
                </el-icon>
                <span :class="{ 'completed-text': row.completionStatus === 1 }">
                  {{ row.taskContent }}
                </span>
                <el-tag 
                  v-if="row.subTasks && row.subTasks.length > 0" 
                  size="small" 
                  type="info"
                  style="margin-left: 8px"
                >
                  {{ row.subTasks.length }} 个子任务
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="开始时间" width="180">
            <template #default="{ row }">
              <div class="time-cell">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDateTime(row.startTime) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="结束时间" width="180">
            <template #default="{ row }">
              <div class="time-cell" :class="{ 'overdue': isOverdue(row) }">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDateTime(row.endTime) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="完成状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.completionStatus === 1 ? 'success' : 'warning'" size="small">
                {{ row.completionStatus === 1 ? '已完成' : '未完成' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="300" align="center" fixed="right">
            <template #default="{ row }">
              <el-button 
                size="small" 
                @click="toggleComplete(row)"
                :type="row.completionStatus === 1 ? 'success' : 'primary'"
                plain
              >
                <el-icon><Check v-if="row.completionStatus === 1" /><Clock v-else /></el-icon>
                {{ row.completionStatus === 1 ? '已完成' : '完成' }}
              </el-button>
              <el-button 
                size="small" 
                type="warning" 
                @click="openEditDialog(row)"
                plain
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="handleDelete(row.taskId)"
                plain
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div v-if="filteredTasks.length > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            :total="filteredTasks.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePageSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>

      <!-- 新增/编辑任务对话框 -->
      <el-dialog 
        v-model="showTaskDialog" 
        :title="isEdit ? '编辑任务' : '新增任务'"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form 
          :model="taskForm" 
          :rules="taskRules"
          ref="taskFormRef"
          label-width="90px"
        >
          <el-form-item label="任务内容" prop="taskContent">
            <el-input 
              v-model="taskForm.taskContent" 
              placeholder="请输入任务内容"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="父任务" v-if="!isEdit">
            <el-select 
              v-model="taskForm.parentTaskId" 
              placeholder="选择父任务（可选）"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="task in availableParentTasks"
                :key="task.taskId"
                :label="task.taskContent"
                :value="task.taskId"
                :disabled="task.completionStatus === 1"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker 
              v-model="taskForm.startTime" 
              type="datetime" 
              placeholder="选择开始时间"
              style="width: 100%"
              :disabled-date="disabledStartDate"
              @change="handleStartTimeChange"
            />
          </el-form-item>
          
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker 
              v-model="taskForm.endTime" 
              type="datetime" 
              placeholder="选择结束时间"
              :disabled-date="disabledEndDate"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="closeTaskDialog">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitTask" 
            :loading="submitLoading"
          >
            确认
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { 
  Plus,
  Refresh,
  Document, 
  CircleCheck, 
  Clock, 
  Folder, 
  Calendar, 
  Delete, 
  Check,
  Edit,
  Search
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
const showTaskDialog = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const loading = ref(false);
const taskFormRef = ref<FormInstance>();

// 分页
const currentPage = ref(1);
const pageSize = ref(10);

// 搜索表单
const searchForm = ref({
  taskContent: '',
  completionStatus: null as number | null
});

const taskForm = ref({
  taskId: null as number | null,
  userId: userId || '',
  parentTaskId: null as number | null,
  taskContent: '',
  startTime: null as Date | null,
  endTime: null as Date | null
});

// 表单验证规则
const taskRules: FormRules = {
  taskContent: [
    { required: true, message: '请输入任务内容', trigger: 'blur' },
    { min: 1, max: 200, message: '任务内容长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  startTime: [
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请选择开始时间'));
          return;
        }
        
        const now = new Date();
        const startTime = new Date(value);
        
        // 新增任务：开始时间必须晚于当前时间
        if (!isEdit.value && startTime < now) {
          callback(new Error('开始时间必须晚于当前时间'));
          return;
        }
        
        callback();
      },
      trigger: 'change'
    }
  ],
  endTime: [
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请选择结束时间'));
          return;
        }
        
        const now = new Date();
        const endTime = new Date(value);
        const startTime = taskForm.value.startTime ? new Date(taskForm.value.startTime) : null;
        
        // 结束时间必须晚于当前时间
        if (endTime < now) {
          callback(new Error('结束时间必须晚于当前时间'));
          return;
        }
        
        // 结束时间必须晚于开始时间
        if (startTime && endTime < startTime) {
          callback(new Error('结束时间必须晚于开始时间'));
          return;
        }
        
        callback();
      },
      trigger: 'change'
    }
  ]
};

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

// 过滤任务
const filteredTasks = computed(() => {
  let result = flattenTasks.value;
  
  // 按任务名称搜索
  if (searchForm.value.taskContent) {
    const keyword = searchForm.value.taskContent.toLowerCase();
    result = result.filter(task => 
      task.taskContent.toLowerCase().includes(keyword)
    );
  }
  
  // 按完成状态筛选
  if (searchForm.value.completionStatus !== null) {
    result = result.filter(task => 
      task.completionStatus === searchForm.value.completionStatus
    );
  }
  
  return result;
});

// 分页数据
const paginatedTasks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredTasks.value.slice(start, end);
});

// 可选的父任务
const availableParentTasks = computed(() => {
  if (isEdit.value && taskForm.value.taskId) {
    const currentTaskId = taskForm.value.taskId;
    return flattenTasks.value.filter(task => 
      task.taskId !== currentTaskId && 
      task.completionStatus !== 1 &&
      !isDescendant(task, currentTaskId)
    );
  }
  return flattenTasks.value.filter(task => task.completionStatus !== 1);
});

const completedCount = computed(() => {
  return flattenTasks.value.filter(task => task.completionStatus === 1).length;
});

const pendingCount = computed(() => {
  return flattenTasks.value.filter(task => task.completionStatus === 0).length;
});

// 检查是否为子孙任务
const isDescendant = (task: Task, ancestorId: number): boolean => {
  if (!task.subTasks || task.subTasks.length === 0) return false;
  return task.subTasks.some(subTask => 
    subTask.taskId === ancestorId || isDescendant(subTask, ancestorId)
  );
};

// 判断是否逾期
const isOverdue = (task: Task): boolean => {
  if (!task.endTime || task.completionStatus === 1) return false;
  return dayjs(task.endTime).isBefore(dayjs());
};

// 格式化日期时间
const formatDateTime = (dateStr?: string | Date): string => {
  if (!dateStr) return '--';
  try {
    return dayjs(dateStr).format('YYYY-MM-DD HH:mm');
  } catch {
    return '--';
  }
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

// 表格行样式
const tableRowClassName = ({ row }: { row: Task }): string => {
  if (row.completionStatus === 1) return 'completed-row';
  if (isOverdue(row)) return 'overdue-row';
  return '';
};

// 禁用开始日期（新增时：必须晚于当前；编辑时：可以是过去）
const disabledStartDate = (time: Date): boolean => {
  if (isEdit.value) {
    return false; // 编辑时允许选择任何日期
  }
  // 新增时：禁用今天之前的日期
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000;
};

// 禁用结束日期（必须晚于开始时间和当前时间）
const disabledEndDate = (time: Date): boolean => {
  const now = Date.now();
  const startTime = taskForm.value.startTime ? new Date(taskForm.value.startTime).getTime() : null;
  
  // 结束时间必须晚于当前时间
  if (time.getTime() < now - 24 * 60 * 60 * 1000) {
    return true;
  }
  
  // 如果有开始时间，结束时间必须晚于开始时间
  if (startTime && time.getTime() <= startTime - 24 * 60 * 60 * 1000) {
    return true;
  }
  
  return false;
};

// 开始时间变化时重新验证结束时间
const handleStartTimeChange = () => {
  if (taskForm.value.endTime) {
    taskFormRef.value?.validateField('endTime');
  }
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1; // 重置到第一页
};

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    taskContent: '',
    completionStatus: null
  };
  currentPage.value = 1;
};

// 分页处理
const handlePageSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1;
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
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

// 打开新增对话框
const openAddDialog = () => {
  isEdit.value = false;
  taskForm.value = {
    taskId: null,
    userId: userId || '',
    parentTaskId: null,
    taskContent: '',
    startTime: null,
    endTime: null
  };
  showTaskDialog.value = true;
  
  // 清除验证
  setTimeout(() => {
    taskFormRef.value?.clearValidate();
  }, 0);
};

// 打开编辑对话框
const openEditDialog = (task: Task) => {
  isEdit.value = true;
  taskForm.value = {
    taskId: task.taskId,
    userId: task.userId.toString(),
    parentTaskId: task.parentTaskId || null,
    taskContent: task.taskContent,
    startTime: task.startTime ? new Date(task.startTime) : null,
    endTime: task.endTime ? new Date(task.endTime) : null
  };
  showTaskDialog.value = true;
  
  // 清除验证
  setTimeout(() => {
    taskFormRef.value?.clearValidate();
  }, 0);
};

// 关闭对话框
const closeTaskDialog = () => {
  showTaskDialog.value = false;
  taskForm.value = {
    taskId: null,
    userId: userId || '',
    parentTaskId: null,
    taskContent: '',
    startTime: null,
    endTime: null
  };
  taskFormRef.value?.clearValidate();
};

// 提交任务
const submitTask = async () => {
  if (!taskFormRef.value) return;
  
  // 验证表单
  const valid = await taskFormRef.value.validate().catch(() => false);
  if (!valid) return;
  
  submitLoading.value = true;
  
  try {
        const res: ApiResponse = isEdit.value 
      ? await taskAPI.updateTask(taskForm.value)
      : await taskAPI.addTask(taskForm.value);
    if (res.success) {
      ElMessage.success(res.message || (isEdit.value ? '修改成功' : '新增成功'));
      closeTaskDialog();
      await loadTasks();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (error) {
    console.error('提交失败:', error);
    ElMessage.error('操作失败');
  } finally {
    submitLoading.value = false;
  }
};

// 切换完成状态
const toggleComplete = async (task: Task) => {
  try {
    const res: ApiResponse = await taskAPI.toggleTaskStatus(task.taskId);
    if (res.success) {
      ElMessage.success(res.message || '操作成功');
      await loadTasks();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (error) {
    console.error('操作失败:', error);
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

// 监听搜索条件变化
watch([() => searchForm.value.taskContent, () => searchForm.value.completionStatus], () => {
  currentPage.value = 1;
});

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
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 20px 40px;
}

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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
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
.search-section {
margin-bottom: 24px;
}
.search-form {
display: flex;
gap: 12px;
align-items: center;
flex-wrap: wrap;
}

.task-section {
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

.page-size-selector {
display: flex;
align-items: center;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.task-content-cell {
display: flex;
align-items: center;
}
.completed-text {
color: #67c23a;
text-decoration: line-through;
}
.time-cell {
display: flex;
align-items: center;
gap: 6px;
color: #606266;
}
.time-cell.overdue {
color: #f56c6c;
font-weight: 500;
}
.subtask-container {
padding: 16px;
background: #f5f7fa;
border-radius: 8px;
margin: 8px 0;
}
.subtask-header {
display: flex;
align-items: center;
gap: 8px;
margin-bottom: 12px;
font-weight: 500;
color: #303133;
}
.no-subtasks {
padding: 20px;
text-align: center;
}
.pagination-container {
display: flex;
justify-content: center;
margin-top: 24px;
}
:deep(.el-table) {
font-size: 14px;
}
:deep(.el-table .completed-row) {
background: #f0f9ff;
}
:deep(.el-table .overdue-row) {
background: #fef0f0;
}
:deep(.el-table__expand-icon) {
font-size: 16px;
}


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
  .search-form {
flex-direction: column;
align-items: stretch;
}
.search-form > * {
width: 100% !important;
}
.section-header {
flex-direction: column;
align-items: flex-start;
gap: 12px;
}
.page-size-selector {
width: 100%;
}
}

</style>
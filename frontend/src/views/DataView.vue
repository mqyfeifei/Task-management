
<template>
  <div class="data-analysis-page">
    <TheNavbar />
    
    <div class="container">
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
            <el-icon :size="32"><TrendCharts /></el-icon>
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

      <!-- 可视化图表 -->
      <div class="charts-grid">
        <div class="chart-card">
          <div class="chart-header">
            <h3>任务完成率</h3>
          </div>
          <div ref="pieChartRef" class="chart-container"></div>
        </div>

        <div class="chart-card">
          <div class="chart-header">
            <h3>操作类型统计</h3>
          </div>
          <div ref="barChartRef" class="chart-container"></div>
        </div>

        <div class="chart-card full-width">
          <div class="chart-header">
            <h3>每日任务处理量</h3>
          </div>
          <div ref="lineChartRef" class="chart-container"></div>
        </div>
      </div>

      <!-- 操作记录表格 -->
      <div class="records-section">
        <div class="section-header">
          <h3>操作记录</h3>
          <div class="header-actions">

            <div class="page-size-selector">
            <span style="margin-right: 8px; color: #606266;">每页显示：</span>
            <el-select v-model="pageSize" @change="handlePageSizeChange" style="width: 100px">
              <el-option :value="5" label="5 条" />
              <el-option :value="10" label="10 条" />
              <el-option :value="20" label="20 条" />
              <el-option :value="50" label="50 条" />
            </el-select>
          </div>
            <el-button 
              @click="refreshRecords" 
              :icon="Refresh" 
              circle 
            />
            
          </div>
        </div>

        <!-- 筛选器 -->
        <div class="filter-bar">
          <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="custom">自定义</el-radio-button>
          </el-radio-group>

          <el-date-picker
            v-if="timeRange === 'custom'"
            v-model="customTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            @change="loadRecords"
            style="margin-left: 12px;"
          />

          <el-select
            v-model="selectedOperationType"
            placeholder="操作类型"
            clearable
            @change="loadRecords"
            style="width: 150px; margin-left: 12px;"
          >
            <el-option label="全部" value="" />
            <el-option label="新增任务" value="新增任务" />
            <el-option label="修改任务" value="修改任务" />
            <el-option label="完成任务" value="完成任务" />
            <el-option label="取消完成" value="取消完成" />
            <el-option label="删除任务" value="删除任务" />
          </el-select>

          <el-button
            v-if="selectedRecords.length > 0"
            type="danger"
            :icon="Delete"
            @click="handleBatchDelete"
            style="margin-left: 12px;"
          >
            批量删除 ({{ selectedRecords.length }})
          </el-button>
        </div>
        
        <el-table 
          :data="paginatedRecords"
          v-loading="recordsLoading"
          @selection-change="handleSelectionChange"
          style="width: 100%; margin-top: 16px;"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
          empty-text="暂无操作记录"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column prop="operationTime" label="操作时间" width="180">
            <template #default="{ row }">
              <el-icon style="margin-right: 5px"><Clock /></el-icon>
              {{ formatTime(row.operationTime) }}
            </template>
          </el-table-column>
          
          <el-table-column prop="operationType" label="操作类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getOperationTypeTag(row.operationType)">
                {{ row.operationType }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="operationDetail" label="操作详情" show-overflow-tooltip />

          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                type="danger"
                link
                @click="handleDeleteRecord(row.recordId)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页 -->
        <div v-if="records.length > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            :total="records.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePageSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>        
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Document, 
  CircleCheck, 
  TrendCharts,
  List, 
  Clock,
  Refresh,
  Delete
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { analysisAPI } from '@/api';
import { getCurrentUserId } from '@/utils/auth';
import type { StatsData, OperationRecord, ApiResponse } from '@/types';
import TheNavbar from '@/components/TheNavbar.vue';
import dayjs from 'dayjs';
import { computed} from 'vue';


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
const selectedRecords = ref<OperationRecord[]>([]);
const statsLoading = ref(false);
const recordsLoading = ref(false);
const timeRange = ref('today');
const customTimeRange = ref<[Date, Date] | null>(null);
const selectedOperationType = ref('');
// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);

// 图表引用
const pieChartRef = ref<HTMLElement>();
const barChartRef = ref<HTMLElement>();
const lineChartRef = ref<HTMLElement>();

let pieChart: echarts.ECharts | null = null;
let barChart: echarts.ECharts | null = null;
let lineChart: echarts.ECharts | null = null;

// 分页数据
const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return records.value.slice(start, end);
});

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
      
      // 更新饼图
      updatePieChart();
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

// 获取时间范围
const getTimeRange = (): { startTime?: string; endTime?: string } => {
  const now = dayjs();
  let startTime: string | undefined;
  let endTime: string | undefined;

  switch (timeRange.value) {
    case 'today':
      startTime = now.startOf('day').format('YYYY-MM-DD HH:mm:ss');
      endTime = now.endOf('day').format('YYYY-MM-DD HH:mm:ss');
      break;
    case 'week':
      startTime = now.startOf('week').format('YYYY-MM-DD HH:mm:ss');
      endTime = now.endOf('week').format('YYYY-MM-DD HH:mm:ss');
      break;
    case 'month':
      startTime = now.startOf('month').format('YYYY-MM-DD HH:mm:ss');
      endTime = now.endOf('month').format('YYYY-MM-DD HH:mm:ss');
      break;
    case 'custom':
      if (customTimeRange.value) {
        startTime = dayjs(customTimeRange.value[0]).format('YYYY-MM-DD HH:mm:ss');
        endTime = dayjs(customTimeRange.value[1]).format('YYYY-MM-DD HH:mm:ss');
      }
      break;
  }

  return { startTime, endTime };
};

// 获取操作记录
const loadRecords = async () => {
  if (!userId) return;
  
  recordsLoading.value = true;
  
  try {
    const { startTime, endTime } = getTimeRange();
    const params: any = {};
    
    if (startTime) params.startTime = startTime;
    if (endTime) params.endTime = endTime;
    if (selectedOperationType.value) params.operationType = selectedOperationType.value;

    const res: ApiResponse<OperationRecord[]> = await analysisAPI.getOperationRecords(userId, params);
    
    if (res.success && res.data) {
      records.value = res.data;
      
      // 更新图表
      updateBarChart();
      updateLineChart();
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
  currentPage.value = 1; // 重置到第一页
};



// 时间范围变化
const handleTimeRangeChange = () => {
  if (timeRange.value !== 'custom') {
    customTimeRange.value = null;
  }

currentPage.value = 1;
  loadRecords();
};

// 刷新记录
const refreshRecords = () => {
  loadStats();
  loadRecords();
};

// 选择变化
const handleSelectionChange = (selection: OperationRecord[]) => {
  selectedRecords.value = selection;
};

// 删除单条记录
const handleDeleteRecord = async (recordId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const res = await analysisAPI.deleteRecord(recordId);
    if (res.success) {
      ElMessage.success('删除成功');
      refreshRecords();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRecords.value.length} 条记录吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    const recordIds = selectedRecords.value.map(r => r.recordId!);
    const res = await analysisAPI.batchDeleteRecords(recordIds);
    
    if (res.success) {
      ElMessage.success('批量删除成功');
      selectedRecords.value = [];
      refreshRecords();
    } else {
      ElMessage.error(res.message || '批量删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error);
      ElMessage.error('批量删除失败');
    }
  }
};

// 初始化图表
const initCharts = () => {
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value);
  }
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value);
  }
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value);
  }

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize);
};

// 更新饼图
const updatePieChart = () => {
  if (!pieChart) return;

  const completed = stats.value.completedTask || 0;
  const pending = (stats.value.totalTask || 0) - completed;

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [
      {
        name: '任务状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: completed, name: '已完成', itemStyle: { color: '#67c23a' } },
          { value: pending, name: '进行中', itemStyle: { color: '#e6a23c' } }
        ]
      }
    ]
  };

  pieChart.setOption(option);
};

// 更新柱状图
const updateBarChart = () => {
  if (!barChart) return;

  const typeMap: Record<string, number> = {};
  records.value.forEach(record => {
    const type = record.operationType;
    typeMap[type] = (typeMap[type] || 0) + 1;
  });

  const types = Object.keys(typeMap);
  const counts = Object.values(typeMap);

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: types,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '操作次数',
        type: 'bar',
        barWidth: '60%',
        data: counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        }
      }
    ]
  };

  barChart.setOption(option);
};

// 更新折线图
const updateLineChart = () => {
  if (!lineChart) return;

  const dateMap: Record<string, number> = {};
  records.value.forEach(record => {
    const date = dayjs(record.operationTime).format('MM-DD');
    dateMap[date] = (dateMap[date] || 0) + 1;
  });

  const dates = Object.keys(dateMap).sort();
  const counts = dates.map(date => dateMap[date]);

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '处理量',
        type: 'line',
        smooth: true,
        data: counts,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#667eea',
          width: 2
        },
        itemStyle: {
          color: '#667eea'
        }
      }
    ]
  };

  lineChart.setOption(option);
};

// 窗口大小变化
const handleResize = () => {
  pieChart?.resize();
  barChart?.resize();
  lineChart?.resize();
};

// 格式化完成率
const formatRate = (rate: string | number | undefined): string => {
  if (!rate) return '0%';
  if (typeof rate === 'number') return `${rate.toFixed(1)}%`;
  if (typeof rate === 'string') return rate.includes('%') ? rate : `${rate}%`;
  return '0%';
};

// 格式化时间
const formatTime = (time: string): string => {
  if (!time) return '';
  return dayjs(time).format('YYYY-MM-DD HH:mm');
};

// 获取操作类型标签样式
const getOperationTypeTag = (type: string): string => {
  const typeMap: Record<string, string> = {
    '新增任务': 'success',
    '完成任务': 'primary',
    '取消完成': 'info',
    '删除任务': 'danger',
    '修改任务': 'warning'
  };
  return typeMap[type] || 'info';
};
// 分页处理
const handlePageSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1;
};

const handleCurrentChange = (val: number) => {
  currentPage.value = val;
};

// 监听记录变化，更新图表
watch(records, () => {
  updateBarChart();
  updateLineChart();
});

onMounted(() => {
  loadStats();
  loadRecords();
  
  // 延迟初始化图表，确保 DOM 已渲染
  setTimeout(() => {
    initCharts();
    updatePieChart();
    updateBarChart();
    updateLineChart();
  }, 100);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  pieChart?.dispose();
  barChart?.dispose();
  lineChart?.dispose();
});
</script>

<style scoped>
.data-analysis-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.container {
  max-width: 1400px;
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

/* 图表网格 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-header {
  margin-bottom: 16px;
}

.chart-header h3 {
  font-size: 18px;
  margin: 0;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 300px;
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

.header-actions {
  display: flex;
  gap: 12px;
}

/* 筛选器 */
.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-card.full-width {
    grid-column: 1;
  }
}

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
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-bar > * {
    width: 100% !important;
  }
}
</style>
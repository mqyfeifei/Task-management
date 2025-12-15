// @/types/index.ts
export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
  code?: number;
}

export interface Task {
  taskId: number;
  taskContent: string;
  completionStatus: number;  // 0-未完成, 1-已完成
  endTime?: string;
  subTasks?: Task[];        // 子任务列表
  userId: number;
  parentTaskId?: number;
  createTime?: string;
  updateTime?: string;
}


export interface StatsData {
  totalTask?: number;
  completedTask?: number;
  completionRate?: string | number; // 修改为联合类型
  operationCount?: number;
}



// 新增任务请求类型
export interface NewTaskRequest {
  userId: string | number;
  parentTaskId?: number | null;
  taskContent: string;
  endTime?: string | null;
}

export interface UserInfo {
  userId: string;
  username?: string;
  signature?: string;
  avatar?: string;
  email?: string;
}

// 在现有类型基础上添加以下内容

// 操作记录类型（已存在，补充更多字段）
export interface OperationRecord {
  recordId?: number;
  userId?: number;
  taskId?: number;
  operationType: string;
  operationTime: string;  // 使用 string 类型
  operationDetail: string;
}

// 操作记录查询参数
export interface RecordQueryParams {
  pageNum?: number;
  pageSize?: number;
  operationType?: string;
  userId?: string | number;
  taskId?: string | number;
  startTime?: string;
  endTime?: string;
}

// 操作记录分页响应
export interface RecordPageResponse {
  records: OperationRecord[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

// 操作统计数据（用于图表）
export interface OperationStatistics {
  typeDistribution: Array<{
    name: string;
    value: number;
  }>;
  timeTrend: {
    labels: string[];
    data: number[];
  };
  totalCount: number;
}

// 批量删除请求
export interface BatchDeleteRequest {
  recordIds: number[];
}
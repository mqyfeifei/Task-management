import service from '@/utils/request';
import type { StatsData, OperationRecord, Task, ApiResponse } from '@/types';

// 用户相关 API
export const userAPI = {
  // 注册
  register: (data: any): Promise<ApiResponse> => 
    service.post('/api/user/register', data),
  
  // 登录
  login: (params: { username: string; password: string }): Promise<ApiResponse<string>> => 
    service.post('/api/user/login', null, { 
      params: {
        username: params.username,
        password: params.password
      }
    }),
  
  // 获取用户信息
  getUserInfo: (userId: string): Promise<ApiResponse<any>> =>
    service.get(`/api/user/info/${userId}`),
  
  // 更新用户信息
  updateUserInfo: (data: any): Promise<ApiResponse> =>
    service.put('/api/user/update', data),
  
  // 上传头像
  uploadAvatar: (file: File, userId: string): Promise<ApiResponse<string>> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId);
    
    return service.post('/api/user/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },
    // 修改密码
  changePassword: (data: {
    userId: string;
    oldPassword: string;
    newPassword: string;
  }): Promise<ApiResponse> =>
    service.put('/api/user/change-password', data),
};



// 任务相关 API
// 任务相关 API
export const taskAPI = {
  // 获取用户任务列表
  getUserTasks: (userId: string): Promise<ApiResponse<Task[]>> => 
    service.get(`/api/task/user/${userId}`),
  
  // 新增任务
  addTask: (data: any): Promise<ApiResponse<number>> => 
    service.post('/api/task/add', data),
  
  // 修改任务
  updateTask: (data: any): Promise<ApiResponse> =>
    service.put('/api/task/update', data),
  
  // 切换任务完成状态
  toggleTaskStatus: (taskId: number): Promise<ApiResponse> =>
    service.put(`/api/task/toggle/${taskId}`),
  
  // 完成任务（兼容旧接口）
  completeTask: (taskId: number): Promise<ApiResponse> => 
    service.put(`/api/task/complete/${taskId}`),
  
  // 删除任务
  deleteTask: (taskId: number): Promise<ApiResponse> => 
    service.delete(`/api/task/delete/${taskId}`),
};

// 数据统计相关 API
// api/index.ts
export const analysisAPI = {
  // 获取任务统计数据
  getTaskStats: (userId: string): Promise<ApiResponse<StatsData>> => 
    service.get(`/api/analysis/task/${userId}`),
  
  // 获取用户操作记录（支持筛选）
  getOperationRecords: (
    userId: string, 
    params?: {
      startTime?: string;
      endTime?: string;
      operationType?: string;
    }
  ): Promise<ApiResponse<OperationRecord[]>> => 
    service.get(`/api/record/user/${userId}`, { params }),
  
  // 删除单条记录
  deleteRecord: (recordId: number): Promise<ApiResponse> =>
    service.delete(`/api/record/${recordId}`),
  
  // 批量删除记录
  batchDeleteRecords: (recordIds: number[]): Promise<ApiResponse> =>
    service.delete('/api/record/batch', { data: recordIds }),
};
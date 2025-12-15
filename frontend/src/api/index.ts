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
  }
};



// 任务相关 API
export const taskAPI = {
  // 获取用户任务列表 - 返回 ApiResponse<Task[]>
  getUserTasks: (userId: string): Promise<ApiResponse<Task[]>> => 
    service.get(`/api/task/user/${userId}`),
  
  // 新增任务 - 返回 ApiResponse<number>（taskId）
  addTask: (data: any): Promise<ApiResponse<number>> => 
    service.post('/api/task/add', data),
  
  // 完成任务 - 返回 ApiResponse
  completeTask: (taskId: number): Promise<ApiResponse> => 
    service.put(`/api/task/complete/${taskId}`),
  
  // 删除任务 - 返回 ApiResponse
  deleteTask: (taskId: number): Promise<ApiResponse> => 
    service.delete(`/api/task/delete/${taskId}`),
};

// 数据统计相关 API
export const analysisAPI = {
  // 明确返回 ApiResponse<StatsData>
  getTaskStats: (userId: string): Promise<ApiResponse<StatsData>> => 
    service.get(`/api/analysis/task/${userId}`),
  
  // 明确返回 ApiResponse<OperationRecord[]>
  getOperationRecords: (userId: string): Promise<ApiResponse<OperationRecord[]>> => 
    service.get(`/api/record/user/${userId}`),
};
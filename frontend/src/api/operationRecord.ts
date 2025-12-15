import service from '@/utils/request';
import type { 
  OperationRecord, 
  RecordQueryParams, 
  RecordPageResponse,
  OperationStatistics,
  ApiResponse 
} from '@/types';

// 操作记录相关 API
export const operationRecordAPI = {
  // 获取操作记录列表（分页）
  getRecordList: (params: RecordQueryParams): Promise<ApiResponse<RecordPageResponse>> => 
    service.get('/api/record/list', { params }),
  
  // 根据用户ID查询记录
  getRecordsByUser: (userId: string | number): Promise<ApiResponse<OperationRecord[]>> => 
    service.get(`/api/record/user/${userId}`),
  
  // 根据任务ID查询记录
  getRecordsByTask: (taskId: string | number): Promise<ApiResponse<OperationRecord[]>> => 
    service.get(`/api/record/task/${taskId}`),
  
  // 删除单个记录
  deleteRecord: (recordId: number): Promise<ApiResponse> => 
    service.delete(`/api/record/${recordId}`),
  
  // 批量删除记录
  batchDeleteRecords: (recordIds: number[]): Promise<ApiResponse> => 
    service.delete('/api/record/batch', { data: recordIds }),
  
  // 获取操作统计数据
  getOperationStatistics: (params: {
    startTime?: string;
    endTime?: string;
  }): Promise<ApiResponse<OperationStatistics>> => 
    service.get('/api/record/statistics', { params }),
};
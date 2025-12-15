package com.task.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.task.backend.entity.SysOperationRecord;
import java.util.List;

/**
 * 操作记录服务接口（定义操作记录相关业务方法）
 */
public interface ISysOperationRecordService extends IService<SysOperationRecord> {

    /**
     * 创建操作记录
     * @param userId 操作用户ID
     * @param taskId 关联任务ID
     * @param operationType 操作类型（新增/修改/删除/完成）
     * @param detail 操作详情描述
     */
    void createRecord(Long userId, Long taskId, String operationType, String detail);

    /**
     * 查询用户的操作记录（按时间倒序）
     * @param userId 用户ID
     * @param limit 最多返回的记录数（0表示无限制）
     * @return 操作记录列表
     */
    List<SysOperationRecord> getUserRecords(Long userId, int limit);

    /**
     * 查询任务的操作记录（按时间倒序）
     * @param taskId 任务ID
     * @return 操作记录列表
     */
    List<SysOperationRecord> getTaskRecords(Long taskId);
}
package com.task.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.task.backend.entity.SysOperationRecord;
import com.task.backend.mapper.SysOperationRecordMapper;
import com.task.backend.service.ISysOperationRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysOperationRecordServiceImpl
        extends ServiceImpl<SysOperationRecordMapper, SysOperationRecord>
        implements ISysOperationRecordService {

    /**
     * 创建操作记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRecord(Long userId, Long taskId, String operationType, String detail) {
        SysOperationRecord record = new SysOperationRecord();
        record.setUserId(userId);
        record.setTaskId(taskId);
        record.setOperationType(operationType);
        record.setOperationTime(LocalDateTime.now());  // 使用 LocalDateTime
        record.setOperationDetail(detail);
        save(record);
    }

    /**
     * 查询用户的操作记录（按时间倒序）
     */
    @Override
    public List<SysOperationRecord> getUserRecords(Long userId, int limit) {
        QueryWrapper<SysOperationRecord> query = new QueryWrapper<>();
        query.eq("user_id", userId)
                .orderByDesc("operation_time");

        if (limit > 0) {
            query.last("LIMIT " + limit);
        }

        return list(query);
    }

    /**
     * 查询任务的操作记录（按时间倒序）
     */
    @Override
    public List<SysOperationRecord> getTaskRecords(Long taskId) {
        QueryWrapper<SysOperationRecord> query = new QueryWrapper<>();
        query.eq("task_id", taskId)
                .orderByDesc("operation_time");
        return list(query);
    }
}


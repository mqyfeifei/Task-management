package com.task.backend.controller;

import com.task.backend.entity.SysOperationRecord;
import com.task.backend.service.ISysOperationRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.task.backend.common.Result;
import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/record")
public class SysOperationRecordController {

    @Resource
    private ISysOperationRecordService recordService;

    // 查询用户操作记录
    @GetMapping("/user/{userId}")
    public Result<List<SysOperationRecord>> getUserRecords(@PathVariable Long userId) {
        try {
            List<SysOperationRecord> records = recordService.lambdaQuery()
                    .eq(SysOperationRecord::getUserId, userId)
                    .orderByDesc(SysOperationRecord::getOperationTime)
                    .list();
            return Result.success(records, "获取操作记录成功");
        } catch (Exception e) {
            return Result.fail("获取操作记录失败: " + e.getMessage());
        }
    }

    // 查询任务操作记录
    @GetMapping("/task/{taskId}")
    public List<SysOperationRecord> getTaskRecords(@PathVariable Long taskId) {
        return recordService.lambdaQuery()
                .eq(SysOperationRecord::getTaskId, taskId)
                .orderByDesc(SysOperationRecord::getOperationTime)
                .list();
    }

    // 查询已删除任务的操作记录
    @GetMapping("/deleted")
    public List<SysOperationRecord> getDeletedTaskRecords(@PathVariable Long userId) {
        return recordService.lambdaQuery()
                .eq(SysOperationRecord::getUserId, userId)
                .isNull(SysOperationRecord::getTaskId)  // task_id 为 NULL 的就是已删除任务的记录
                .orderByDesc(SysOperationRecord::getOperationTime)
                .list();
    }
}

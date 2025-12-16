package com.task.backend.controller;

import com.task.backend.common.Result;
import com.task.backend.entity.SysOperationRecord;
import com.task.backend.service.ISysOperationRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/record")
public class SysOperationRecordController {

    @Resource
    private ISysOperationRecordService recordService;

    // 查询用户操作记录（支持时间范围和类型筛选）
    @GetMapping("/user/{userId}")
    public Result<List<SysOperationRecord>> getUserRecords(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String operationType
    ) {
        try {
            List<SysOperationRecord> records = recordService.lambdaQuery()
                    .eq(SysOperationRecord::getUserId, userId)
                    .ge(startTime != null, SysOperationRecord::getOperationTime, startTime)
                    .le(endTime != null, SysOperationRecord::getOperationTime, endTime)
                    .eq(operationType != null && !operationType.isEmpty(),
                            SysOperationRecord::getOperationType, operationType)
                    .orderByDesc(SysOperationRecord::getOperationTime)
                    .list();
            return Result.success(records, "获取操作记录成功");
        } catch (Exception e) {
            return Result.fail("获取操作记录失败: " + e.getMessage());
        }
    }

    // 删除单条记录
    @DeleteMapping("/{recordId}")
    public Result<Void> deleteRecord(@PathVariable Long recordId) {
        try {
            boolean success = recordService.removeById(recordId);
            if (success) {
                return Result.success(null, "删除成功");
            } else {
                return Result.fail("删除失败");
            }
        } catch (Exception e) {
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    // 批量删除记录
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteRecords(@RequestBody List<Long> recordIds) {
        try {
            boolean success = recordService.removeByIds(recordIds);
            if (success) {
                return Result.success(null, "批量删除成功");
            } else {
                return Result.fail("批量删除失败");
            }
        } catch (Exception e) {
            return Result.fail("批量删除失败: " + e.getMessage());
        }
    }

    // 查询任务操作记录
    @GetMapping("/task/{taskId}")
    public Result<List<SysOperationRecord>> getTaskRecords(@PathVariable Long taskId) {
        try {
            List<SysOperationRecord> records = recordService.lambdaQuery()
                    .eq(SysOperationRecord::getTaskId, taskId)
                    .orderByDesc(SysOperationRecord::getOperationTime)
                    .list();
            return Result.success(records, "获取任务操作记录成功");
        } catch (Exception e) {
            return Result.fail("获取任务操作记录失败: " + e.getMessage());
        }
    }

    // 查询已删除任务的操作记录
    @GetMapping("/deleted/{userId}")
    public Result<List<SysOperationRecord>> getDeletedTaskRecords(@PathVariable Long userId) {
        try {
            List<SysOperationRecord> records = recordService.lambdaQuery()
                    .eq(SysOperationRecord::getUserId, userId)
                    .isNull(SysOperationRecord::getTaskId)
                    .orderByDesc(SysOperationRecord::getOperationTime)
                    .list();
            return Result.success(records, "获取已删除任务的操作记录成功");
        } catch (Exception e) {
            return Result.fail("获取已删除任务的操作记录失败: " + e.getMessage());
        }
    }
}
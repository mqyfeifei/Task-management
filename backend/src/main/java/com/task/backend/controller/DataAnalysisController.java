package com.task.backend.controller;

import com.task.backend.entity.SysTask;
import com.task.backend.entity.SysOperationRecord;
import com.task.backend.service.ISysTaskService;
import com.task.backend.service.ISysOperationRecordService;
import com.task.backend.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class DataAnalysisController {

    @Resource
    private ISysTaskService taskService;
    @Resource
    private ISysOperationRecordService recordService;

    // 获取用户任务统计数据
    @GetMapping("/task/{userId}")
    public Result<Map<String, Object>> getTaskAnalysis(@PathVariable Long userId) {
        try {
            Map<String, Object> result = new HashMap<>();

            long total = taskService.lambdaQuery()
                    .eq(SysTask::getUserId, userId)
                    .count();

            long completed = taskService.lambdaQuery()
                    .eq(SysTask::getUserId, userId)
                    .eq(SysTask::getCompletionStatus, 1)
                    .count();

            double rate = total == 0 ? 0 : (double) completed / total * 100;

            long recordCount = recordService.lambdaQuery()
                    .eq(SysOperationRecord::getUserId, userId)
                    .count();

            result.put("totalTask", total);
            result.put("completedTask", completed);
            result.put("completionRate", String.format("%.2f", rate));
            result.put("operationCount", recordCount);

            return Result.success(result, "获取统计数据成功");
        } catch (Exception e) {
            return Result.fail("获取统计数据失败: " + e.getMessage());
        }
    }
}
package com.task.backend.controller;

import com.task.backend.common.Result;
import com.task.backend.entity.SysTask;
import com.task.backend.service.ISysTaskService;
import com.task.backend.service.ISysOperationRecordService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/task")
public class SysTaskController {

    @Resource
    private ISysTaskService taskService;
    @Resource
    private ISysOperationRecordService operationRecordService;

    // 获取用户所有任务（树形结构）
    @GetMapping("/user/{userId}")
    public Result<List<SysTask>> getUserTasks(@PathVariable Long userId) {
        try {
            List<SysTask> tasks = taskService.getUserTasks(userId);
            return Result.success(tasks, "获取任务列表成功");
        } catch (Exception e) {
            return Result.fail("获取任务列表失败: " + e.getMessage());
        }
    }

    // 新增任务
    @PostMapping("/add")
    public Result<Long> addTask(@RequestBody SysTask task) {
        try {
            boolean success = taskService.save(task);
            if (success) {
                // 记录操作
                String detail = task.getParentTaskId() == null
                        ? "新增任务: " + task.getTaskContent()
                        : "新增子任务: " + task.getTaskContent() + " (父任务ID: " + task.getParentTaskId() + ")";
                operationRecordService.createRecord(
                        task.getUserId(),
                        task.getTaskId(),
                        "新增任务",
                        detail
                );
                return Result.success(task.getTaskId(), "任务新增成功");
            } else {
                return Result.fail("新增失败");
            }
        } catch (Exception e) {
            return Result.fail("新增任务失败: " + e.getMessage());
        }
    }

    // 完成任务
    @PutMapping("/complete/{taskId}")
    public Result<Void> completeTask(@PathVariable Long taskId) {
        try {
            SysTask task = taskService.getById(taskId);
            if (task == null) return Result.fail("任务不存在");

            task.setCompletionStatus(1); // 1=已完成
            boolean success = taskService.updateById(task);

            if (success) {
                // 记录操作
                operationRecordService.createRecord(
                        task.getUserId(),
                        taskId,
                        "完成任务",
                        "完成任务: " + task.getTaskContent()
                );
                return Result.success(null, "任务已标记为完成");
            } else {
                return Result.fail("操作失败");
            }
        } catch (Exception e) {
            return Result.fail("完成任务失败: " + e.getMessage());
        }
    }

    // 删除任务（级联删除子任务）
    @DeleteMapping("/delete/{taskId}")
    public Result<Void> deleteTask(@PathVariable Long taskId) {
        try {
            SysTask task = taskService.getById(taskId);
            if (task == null) {
                return Result.fail("任务不存在");
            }

            // 直接调用Service的deleteTask（事务内会处理操作记录）
            boolean success = taskService.deleteTask(taskId);
            if (success) {
                return Result.success(null, "任务及子任务已删除");
            } else {
                return Result.fail("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("删除任务失败: " + e.getMessage());
        }
    }
}
package com.task.backend.controller;

import com.task.backend.common.Result;
import com.task.backend.entity.SysTask;
import com.task.backend.service.ISysTaskService;
import com.task.backend.service.ISysOperationRecordService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Date;

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
            // 设置创建时间和更新时间
            task.setCreateTime(new Date());
            task.setUpdateTime(new Date());
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


    // 修改任务
    @PutMapping("/update")
    public Result<Void> updateTask(@RequestBody SysTask task) {
        try {
            SysTask oldTask = taskService.getById(task.getTaskId());
            if (oldTask == null) {
                return Result.fail("任务不存在");
            }

            // 更新任务
            task.setUpdateTime(new Date());
            boolean success = taskService.updateById(task);

            if (success) {
                // 记录操作
                StringBuilder detail = new StringBuilder("修改任务: ");
                detail.append(task.getTaskContent());

                if (!oldTask.getTaskContent().equals(task.getTaskContent())) {
                    detail.append(" [内容已修改]");
                }
                if ((task.getStartTime() != null && !task.getStartTime().equals(oldTask.getStartTime()))
                        || (oldTask.getStartTime() != null && !oldTask.getStartTime().equals(task.getStartTime()))) {
                    detail.append(" [开始时间已修改]");
                }
                if ((task.getEndTime() != null && !task.getEndTime().equals(oldTask.getEndTime()))
                        || (oldTask.getEndTime() != null && !oldTask.getEndTime().equals(task.getEndTime()))) {
                    detail.append(" [结束时间已修改]");
                }

                operationRecordService.createRecord(
                        task.getUserId(),
                        task.getTaskId(),
                        "修改任务",
                        detail.toString()
                );

                return Result.success(null, "任务修改成功");
            } else {
                return Result.fail("修改失败");
            }
        } catch (Exception e) {
            return Result.fail("修改任务失败: " + e.getMessage());
        }
    }

    // 切换任务完成状态
    @PutMapping("/toggle/{taskId}")
    public Result<Void> toggleTaskStatus(@PathVariable Long taskId) {
        try {
            SysTask task = taskService.getById(taskId);
            if (task == null) {
                return Result.fail("任务不存在");
            }

            // 切换状态
            int newStatus = task.getCompletionStatus() == 1 ? 0 : 1;
            task.setCompletionStatus(newStatus);
            task.setUpdateTime(new Date());

            boolean success = taskService.updateById(task);

            if (success) {
                // 记录操作
                String operationType = newStatus == 1 ? "完成任务" : "取消完成";
                String detail = (newStatus == 1 ? "完成任务: " : "取消完成: ") + task.getTaskContent();

                operationRecordService.createRecord(
                        task.getUserId(),
                        taskId,
                        operationType,
                        detail
                );

                return Result.success(null, newStatus == 1 ? "任务已标记为完成" : "任务已标记为未完成");
            } else {
                return Result.fail("操作失败");
            }
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }


    // 完成任务
    // 完成任务（保留兼容性）
    @PutMapping("/complete/{taskId}")
    public Result<Void> completeTask(@PathVariable Long taskId) {
        return toggleTaskStatus(taskId);
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
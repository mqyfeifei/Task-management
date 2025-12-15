package com.task.backend.service.impl;

//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.task.backend.entity.SysTask;
import com.task.backend.mapper.SysTaskMapper;
import com.task.backend.service.ISysTaskService;
import com.task.backend.service.ISysOperationRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.ArrayList;

@Service
public class SysTaskServiceImpl extends ServiceImpl<SysTaskMapper, SysTask> implements ISysTaskService {
    @Resource
    private ISysOperationRecordService operationRecordService;

    // 查询用户所有任务（含子任务层级）
    @Override
    public List<SysTask> getUserTasks(Long userId) {
        // 1. 查询所有根任务（parentTaskId为null）
        List<SysTask> rootTasks = lambdaQuery()
                .eq(SysTask::getUserId, userId)
                .isNull(SysTask::getParentTaskId)
                .list();
        // 2. 递归添加子任务
        rootTasks.forEach(this::addSubTasks);
        return rootTasks;
    }

    // 递归添加子任务
    private void addSubTasks(SysTask parentTask) {
        List<SysTask> subTasks = lambdaQuery()
                .eq(SysTask::getParentTaskId, parentTask.getTaskId())
                .list();
        parentTask.setSubTasks(subTasks); // 需要在SysTask实体类中添加subTasks字段
        subTasks.forEach(this::addSubTasks); // 递归处理子任务的子任务
    }

    // 级联删除任务（含子任务）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTask(Long taskId) {
        // 1. 获取任务信息
        SysTask task = getById(taskId);
        if (task == null) {
            return false;
        }

        // 2. 收集所有要删除的任务ID（用于记录详情）
        List<Long> allTaskIds = new ArrayList<>();
        collectAllTaskIds(taskId, allTaskIds);

        // 3. 先插入操作记录（和删除任务在同一个事务中）
        try {
            operationRecordService.createRecord(
                    task.getUserId(),
                    taskId,
                    "删除任务",
                    "删除任务: " + task.getTaskContent() + " 及其 " + (allTaskIds.size() - 1) + " 个子任务"
            );
        } catch (Exception e) {
            // 记录失败不影响删除，但建议打印日志
            System.err.println("记录删除操作失败: " + e.getMessage());
        }

        // 4. 删除所有子任务（递归）
        deleteSubTasks(taskId);
        // 5. 删除当前任务
        return removeById(taskId);
    }

    // 收集所有任务ID（包括子任务）
    private void collectAllTaskIds(Long taskId, List<Long> taskIds) {
        taskIds.add(taskId);
        List<SysTask> subTasks = lambdaQuery()
                .eq(SysTask::getParentTaskId, taskId)
                .list();
        subTasks.forEach(subTask -> collectAllTaskIds(subTask.getTaskId(), taskIds));
    }

    // 递归删除子任务
    private void deleteSubTasks(Long parentTaskId) {
        List<SysTask> subTasks = lambdaQuery()
                .eq(SysTask::getParentTaskId, parentTaskId)
                .list();
        subTasks.forEach(subTask -> {
            deleteSubTasks(subTask.getTaskId()); // 先删子任务的子任务
            removeById(subTask.getTaskId()); // 再删当前子任务
        });
    }

    // 实现接口中的 getSubTasks 方法
    @Override
    public List<SysTask> getSubTasks(Long parentTaskId) {
        // 查询直接子任务
        List<SysTask> directSubTasks = lambdaQuery()
                .eq(SysTask::getParentTaskId, parentTaskId)
                .list();

        // 递归查询所有层级的子任务
        List<SysTask> allSubTasks = new ArrayList<>(directSubTasks);
        directSubTasks.forEach(subTask -> {
            List<SysTask> nestedSubTasks = getSubTasks(subTask.getTaskId());
            allSubTasks.addAll(nestedSubTasks);
        });

        return allSubTasks;
    }
}
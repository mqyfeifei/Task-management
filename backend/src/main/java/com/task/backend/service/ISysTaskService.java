package com.task.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.task.backend.entity.SysTask;
import java.util.List;

/**
 * 任务服务接口（定义任务相关业务方法）
 */
public interface ISysTaskService extends IService<SysTask> {

    /**
     * 查询用户的所有任务（含子任务层级结构）
     * @param userId 用户ID
     * @return 任务列表（根任务包含子任务）
     */
    List<SysTask> getUserTasks(Long userId);

    /**
     * 级联删除任务（含所有子任务）
     * @param taskId 要删除的任务ID
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteTask(Long taskId);

    /**
     * 标记任务为完成状态
     * @param taskId 任务ID
     * @return 操作成功返回true，失败返回false
     */
    default boolean completeTask(Long taskId) {
        SysTask task = getById(taskId);
        if (task == null) {
            return false;
        }
        task.setCompletionStatus(1); // 1=已完成
        return updateById(task);
    }

    /**
     * 查询指定任务的所有子任务（递归）
     * @param parentTaskId 父任务ID
     * @return 子任务列表
     */
    List<SysTask> getSubTasks(Long parentTaskId);
}

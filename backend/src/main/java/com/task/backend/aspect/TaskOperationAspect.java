package com.task.backend.aspect;

import com.task.backend.entity.SysTask;
import com.task.backend.service.ISysOperationRecordService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;


//@Aspect
//@Component
public class TaskOperationAspect {

    @Resource
    private ISysOperationRecordService recordService;

    // 切入点：任务模块的增删改方法
    @Pointcut("execution(* com.task.backend.service.ISysTaskService.save(..)) || " +
            "execution(* com.task.backend.service.ISysTaskService.updateById(..)) || " +
            "execution(* com.task.backend.service.ISysTaskService.deleteTask(..))")
    public void taskOperationPointcut() {}

    // 后置通知：记录操作
    @AfterReturning("taskOperationPointcut() && args(task)")
    public void recordTaskOperation(SysTask task) {
        String operationType = "";
        String detail = "";
        if (task.getTaskId() == null) { // 新增任务（ID为null）
            operationType = "新增";
            detail = "任务内容：" + task.getTaskContent();
        } else if (task.getCompletionStatus() != null) { // 完成任务
            operationType = "完成";
            detail = "任务ID：" + task.getTaskId() + " 标记为已完成";
        } else { // 修改任务
            operationType = "修改";
            detail = "任务ID：" + task.getTaskId() + " 内容更新";
        }
        recordService.createRecord(task.getUserId(), task.getTaskId(), operationType, detail);
    }

    // 处理删除任务的记录（参数为taskId）
    @AfterReturning("execution(* com.task.backend.service.ISysTaskService.deleteTask(..)) && args(taskId)")
    public void recordDeleteOperation(Long taskId) {
        // 实际应查询任务所属用户ID，此处简化（从ThreadLocal获取当前登录用户ID）
        Long userId = 1L; // 临时测试值，需替换为真实用户ID获取逻辑
        recordService.createRecord(userId, taskId, "删除", "任务ID：" + taskId + " 及子任务被删除");
    }
}
package com.task.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SysTask {
    @TableId(type = IdType.AUTO)
    private Long taskId;

    private Long parentTaskId;

    private Long userId;

    private String taskContent;

    private Date startTime;

    private Date endTime;

    private Integer completionStatus; // 0=未完成，1=已完成

    private Date createTime;

    private Date updateTime;

    // 非数据库字段，用于存储子任务
    @TableField(exist = false)
    private List<SysTask> subTasks;
}
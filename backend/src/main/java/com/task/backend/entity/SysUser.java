package com.task.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;

    private String password;

    private String signature;

    private String avatarUrl;//用户头像地址

    private Date registerTime;

    private Integer accountStatus; // 1=启用，0=禁用
}
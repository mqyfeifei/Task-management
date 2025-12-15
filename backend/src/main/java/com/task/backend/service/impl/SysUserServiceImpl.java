package com.task.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.task.backend.entity.SysUser;
import com.task.backend.mapper.SysUserMapper;
import com.task.backend.service.ISysUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Date;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    // 用户注册
    @Override
    public boolean register(SysUser user) {
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置默认值
        user.setRegisterTime(new Date());
        user.setAccountStatus(1); // 默认为启用
        System.out.println("SysUserServiceImpl里的user"+user);
        return save(user);
    }

    // 根据用户名查询用户
    @Override
    public SysUser getByUsername(String username) {
        return lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();
    }
}
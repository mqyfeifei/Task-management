package com.task.backend.service.impl;

import com.task.backend.entity.SysUser;
import com.task.backend.service.ISysUserService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private ISysUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 注意：这里的 "username" 实际传入的是 userId（从 JWT 解析而来），也可以改为根据 userId 查询
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        // 返回 Spring Security 的 UserDetails 对象（需包含用户名、密码、权限）
        return new User(
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList() // 暂时无权限，后续可扩展
        );
    }
}
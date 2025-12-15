package com.example.usermanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usermanagement.entity.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);
    boolean register(User user);
}
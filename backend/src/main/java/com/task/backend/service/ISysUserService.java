package com.task.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.task.backend.entity.SysUser;

/**
 * 用户服务接口（定义用户相关业务方法）
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 用户注册
     * @param user 注册用户信息（包含用户名、密码等）
     * @return 注册成功返回true，失败返回false
     */
    boolean register(SysUser user);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 匹配的用户实体（无匹配则返回null）
     */
    SysUser getByUsername(String username);

    /**
     * 检查用户名是否已存在
     * @param username 待检查的用户名
     * @return 存在返回true，不存在返回false
     */
    default boolean isUsernameExists(String username) {
        return getByUsername(username) != null;
    }
}
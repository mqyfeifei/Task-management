package com.task.backend.controller;

import com.task.backend.common.Result;
import com.task.backend.common.JwtUtil;
import com.task.backend.entity.SysUser;
import com.task.backend.service.ISysUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class SysUserController {

    @Resource
    private ISysUserService userService;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Resource
    private JwtUtil jwtUtil;

    @Value("${file.upload.path}")
    private String uploadPath;

    // 注册
    @PostMapping("/register")
    public Result<String> register(@RequestBody SysUser user) {
        if (userService.getByUsername(user.getUsername()) != null) {
            return Result.fail("用户名已存在");
        }
        boolean success = userService.register(user);
        return success ? Result.success(null, "注册成功") : Result.fail("注册失败");
    }

    // 登录
    @PostMapping("/login")
    public Result<String> login(@RequestParam String username, @RequestParam String password) {
        SysUser user = userService.getByUsername(username);
        if (user == null) return Result.fail("用户不存在");

        if (passwordEncoder.matches(password, user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUserId());
            return Result.success(token, "登录成功");
        }
        return Result.fail("密码错误");
    }

    // 获取用户信息
    @GetMapping("/info/{userId}")
    public Result<Map<String, Object>> getUserInfo(@PathVariable Long userId) {
        try {
            SysUser user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("username", user.getUsername());
            userInfo.put("signature", user.getSignature());
            userInfo.put("avatarUrl", user.getAvatarUrl());
            userInfo.put("registerTime", user.getRegisterTime());

            return Result.success(userInfo, "获取用户信息成功");
        } catch (Exception e) {
            return Result.fail("获取用户信息失败: " + e.getMessage());
        }
    }

    // 更新用户信息
    @PutMapping("/update")
    public Result<Void> updateUserInfo(@RequestBody SysUser user) {
        try {
            boolean success = userService.updateById(user);
            if (success) {
                return Result.success(null, "更新成功");
            } else {
                return Result.fail("更新失败");
            }
        } catch (Exception e) {
            return Result.fail("更新用户信息失败: " + e.getMessage());
        }
    }

    // 上传头像
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId
    ) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.fail("只能上传图片文件");
            }

            // 验证文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return Result.fail("图片大小不能超过2MB");
            }

            // 确保上传目录存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件到指定目录
            Path filePath = Paths.get(uploadPath, filename);
            Files.write(filePath, file.getBytes());

            // 生成访问URL（只保存相对路径）
            String avatarUrl = "/public/files/" + filename;

            // 更新用户头像URL
            SysUser user = userService.getById(userId);
            if (user != null) {
                // 删除旧头像文件（如果存在）
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    String oldFilename = user.getAvatarUrl().substring(
                            user.getAvatarUrl().lastIndexOf("/") + 1
                    );
                    File oldFile = new File(uploadPath, oldFilename);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                user.setAvatarUrl(avatarUrl);
                userService.updateById(user);
            }

            return Result.success(avatarUrl, "上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    // 修改密码
    @PutMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        try {
            Long userId = Long.parseLong(params.get("userId"));
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            // 验证参数
            if (oldPassword == null || oldPassword.isEmpty() ||
                    newPassword == null || newPassword.isEmpty()) {
                return Result.fail("密码不能为空");
            }

            if (newPassword.length() < 6) {
                return Result.fail("新密码长度不能少于6位");
            }

            // 获取用户
            SysUser user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }

            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return Result.fail("原密码错误");
            }

            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            boolean success = userService.updateById(user);

            if (success) {
                return Result.success(null, "密码修改成功");
            } else {
                return Result.fail("密码修改失败");
            }
        } catch (NumberFormatException e) {
            return Result.fail("用户ID格式错误");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改密码失败: " + e.getMessage());
        }
    }
}
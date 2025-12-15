package com.task.backend.common;

import lombok.Data;

@Data
public class Result<T> {
    private boolean success;  // 操作是否成功
    private T data;           // 响应数据（可选）
    private String message;   // 提示信息（可选）

    // 成功返回（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    // 新增：成功返回（带数据和消息）
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        result.setMessage(message);
        return result;
    }

    // 成功返回（不带数据）
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败返回（带提示信息）
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}
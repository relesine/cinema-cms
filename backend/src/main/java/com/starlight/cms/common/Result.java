package com.starlight.cms.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果封装
 * 约定所有接口都返回 {code, message, data}：
 * code=200 成功；401 未登录；403 无权限；500 业务/系统错误
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功（无数据） */
    public static <T> Result<T> ok() {
        return new Result<>(200, "操作成功", null);
    }

    /** 成功（仅提示消息，固定用于 Result<Void> 场景；具体重载优先于泛型方法） */
    public static Result<Void> ok(String message) {
        return new Result<>(200, message, null);
    }

    /** 成功（带数据） */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 成功（自定义提示 + 数据） */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, message, data);
    }

    /** 失败（自定义 code + 提示） */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /** 业务失败（默认 500） */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}

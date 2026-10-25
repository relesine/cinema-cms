package com.starlight.cms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 注册请求参数（默认注册为普通用户角色） */
@Data
public class RegisterDTO {
    @NotBlank(message = "请输入账号")
    @Size(min = 4, max = 20, message = "账号长度需在4~20位之间")
    private String username;
    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 20, message = "密码长度需在6~20位之间")
    private String password;
    private String nickname;
    private String phone;
}

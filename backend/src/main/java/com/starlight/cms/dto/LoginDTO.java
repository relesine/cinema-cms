package com.starlight.cms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 登录请求参数 */
@Data
public class LoginDTO {
    @NotBlank(message = "请输入账号")
    private String username;
    @NotBlank(message = "请输入密码")
    private String password;
}

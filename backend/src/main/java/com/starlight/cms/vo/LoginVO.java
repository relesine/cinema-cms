package com.starlight.cms.vo;

import lombok.Data;

import java.util.List;

/** 登录成功返回给前端的信息 */
@Data
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    /** 角色编码集合 */
    private List<String> roles;
    /** 主角色（用于登录后跳转对应端） */
    private String primaryRole;
}

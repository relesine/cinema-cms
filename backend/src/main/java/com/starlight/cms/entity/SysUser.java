package com.starlight.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.starlight.cms.common.BaseLogicEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user")
public class SysUser extends BaseLogicEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    // 安全：密码只允许前端传入(反序列化)，永不随接口返回(序列化时忽略)，避免哈希外泄
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String nickname;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer gender;
    private Integer status;
    private String remark;
}

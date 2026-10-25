package com.starlight.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starlight.cms.entity.SysUser;

import java.util.List;

public interface ISysUserService extends IService<SysUser> {

    /** 按账号查用户 */
    SysUser findByUsername(String username);

    /** 查用户拥有的角色编码集合 */
    List<String> getRoleCodes(Long userId);

    /** 给用户绑定一个角色 */
    void bindRole(Long userId, Long roleId);

    /** 新增用户并绑定角色（管理端） */
    Long addUserWithRole(SysUser user, Long roleId);

    /** 重置密码 */
    void resetPassword(Long userId, String newPwd);
}

package com.starlight.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starlight.cms.entity.SysRole;
import com.starlight.cms.entity.SysUser;
import com.starlight.cms.entity.SysUserRole;
import com.starlight.cms.mapper.SysRoleMapper;
import com.starlight.cms.mapper.SysUserMapper;
import com.starlight.cms.mapper.SysUserRoleMapper;
import com.starlight.cms.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser findByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Override
    public List<String> getRoleCodes(Long userId) {
        List<SysUserRole> urs = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (CollectionUtils.isEmpty(urs)) {
            return Collections.emptyList();
        }
        List<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(SysRole::getRoleCode).collect(Collectors.toList());
    }

    @Override
    public void bindRole(Long userId, Long roleId) {
        Long count = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId).eq(SysUserRole::getRoleId, roleId));
        if (count == 0) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addUserWithRole(SysUser user, Long roleId) {
        // 新增用户时密码必须 BCrypt 加密，禁止明文落库
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        this.save(user);
        if (roleId != null) {
            bindRole(user.getId(), roleId);
        }
        return user.getId();
    }

    @Override
    public void resetPassword(Long userId, String newPwd) {
        SysUser u = new SysUser();
        u.setId(userId);
        u.setPassword(passwordEncoder.encode(newPwd));
        this.updateById(u);
    }
}

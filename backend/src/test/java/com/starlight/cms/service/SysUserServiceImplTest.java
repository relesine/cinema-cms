package com.starlight.cms.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.starlight.cms.entity.SysUser;
import com.starlight.cms.entity.SysUserRole;
import com.starlight.cms.mapper.SysRoleMapper;
import com.starlight.cms.mapper.SysUserMapper;
import com.starlight.cms.mapper.SysUserRoleMapper;
import com.starlight.cms.service.impl.SysUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 * 覆盖：密码 BCrypt 加密落库、角色绑定幂等、重置密码
 */
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SysUserServiceImpl userService;

    /** MyBatis-Plus ServiceImpl 的泛型 baseMapper 无法被 Mockito 自动注入，手动反射注入 */
    @BeforeEach
    void injectBaseMapper() {
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    @DisplayName("新增用户：密码必须加密后再落库")
    void addUserShouldEncodePassword() {
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$encoded");
        when(userMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            SysUser u = inv.getArgument(0);
            u.setId(9L);
            return 1;
        });
        when(userRoleMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        SysUser user = new SysUser();
        user.setUsername("newuser");
        user.setPassword("123456");

        Long id = userService.addUserWithRole(user, 1L);

        assertEquals(9L, id);
        assertEquals("$2a$10$encoded", user.getPassword());
        verify(passwordEncoder).encode("123456");
    }

    @Test
    @DisplayName("新增用户：绑定角色只写一条关联记录")
    void addUserShouldBindRole() {
        when(userMapper.insert(any(SysUser.class))).thenAnswer(inv -> {
            ((SysUser) inv.getArgument(0)).setId(3L);
            return 1;
        });
        when(userRoleMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        SysUser user = new SysUser();
        user.setUsername("author");
        user.setPassword("pwd");
        userService.addUserWithRole(user, 2L);

        ArgumentCaptor<SysUserRole> captor = ArgumentCaptor.forClass(SysUserRole.class);
        verify(userRoleMapper).insert(captor.capture());
        assertEquals(3L, captor.getValue().getUserId());
        assertEquals(2L, captor.getValue().getRoleId());
    }

    @Test
    @DisplayName("新增用户：roleId 为空时不写关联表")
    void addUserWithoutRoleShouldSkipBind() {
        when(userMapper.insert(any(SysUser.class))).thenReturn(1);
        SysUser user = new SysUser();
        user.setUsername("norole");
        user.setPassword("pwd");

        userService.addUserWithRole(user, null);

        verify(userRoleMapper, never()).insert(any(SysUserRole.class));
    }

    @Test
    @DisplayName("角色已存在：绑定幂等，不重复插入")
    void bindExistingRoleShouldNotInsert() {
        when(userRoleMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        userService.bindRole(1L, 1L);
        verify(userRoleMapper, never()).insert(any(SysUserRole.class));
    }

    @Test
    @DisplayName("重置密码：新密码加密后更新")
    void resetPasswordShouldEncode() {
        when(passwordEncoder.encode("newPwd123")).thenReturn("$2a$10$new");
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        userService.resetPassword(5L, "newPwd123");

        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals(5L, captor.getValue().getId());
        assertEquals("$2a$10$new", captor.getValue().getPassword());
    }
}

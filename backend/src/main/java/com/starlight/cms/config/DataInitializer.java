package com.starlight.cms.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starlight.cms.entity.SysUser;
import com.starlight.cms.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动初始化：把 SQL 中以 PLAIN: 开头的初始密码自动转为 BCrypt 密文
 * 这样 SQL 脚本可读（能看出初始密码），落库后又是安全密文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ISysUserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        List<SysUser> users = userService.list(new LambdaQueryWrapper<SysUser>()
                .likeRight(SysUser::getPassword, "PLAIN:"));
        for (SysUser user : users) {
            String raw = user.getPassword().substring("PLAIN:".length());
            SysUser update = new SysUser();
            update.setId(user.getId());
            update.setPassword(passwordEncoder.encode(raw));
            userService.updateById(update);
            log.info("初始账号[{}]密码已加密", user.getUsername());
        }
        if (!users.isEmpty()) {
            log.info("共完成 {} 个初始账号的密码加密", users.size());
        }
    }
}

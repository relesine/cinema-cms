package com.starlight.cms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密器：BCrypt（自带随机盐，同一明文每次密文不同）
 * 只引入 spring-security-crypto，不会启用 Security 的登录拦截
 */
@Configuration
public class SecurityBeanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

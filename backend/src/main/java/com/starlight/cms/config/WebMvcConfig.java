package com.starlight.cms.config;

import com.starlight.cms.security.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：跨域、JWT 拦截器、本地上传文件静态映射
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Value("${cms.upload.path}")
    private String uploadPath;

    /** 全局跨域：允许前端 Vite 开发服务器(5173)访问，放行常用方法与请求头 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                // 登录、注册、验证码、文件访问、错误页、swagger 等放行
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/auth/captcha",
                        "/upload/**",
                        "/app/movie/list",
                        "/app/movie/detail/**",
                        "/app/movie/hot",
                        "/app/notice/list",
                        "/app/schedule/**",
                        // Swagger 与 Actuator 放行
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**",
                        "/error"
                );
    }

    /** 把磁盘上传目录映射成可访问 URL：/upload/xxx.jpg -> F:/.../uploads/xxx.jpg */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath);
    }
}

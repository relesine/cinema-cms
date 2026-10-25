package com.starlight.cms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 电影院管理系统后端启动类
 * 1. @SpringBootApplication：Spring Boot 主入口
 * 2. @MapperScan：扫描 Mapper 接口所在包，自动生成实现类
 * 3. @EnableScheduling：开启定时任务（用于超时未支付订单自动取消、释放座位）
 */
@EnableScheduling
@MapperScan("com.starlight.cms.mapper")
@SpringBootApplication
public class CmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CmsApplication.class, args);
        System.out.println("\n========== 星光国际影城 CMS 后端启动成功：http://localhost:8080/api ==========\n");
    }
}

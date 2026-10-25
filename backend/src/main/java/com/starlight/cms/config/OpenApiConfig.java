package com.starlight.cms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI(Swagger 3) 配置
 * 文档地址：http://localhost:8080/api/swagger-ui/index.html
 * 在右上角 Authorize 中粘贴登录返回的 token 即可调试需鉴权接口。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cmsOpenApi() {
        final String schemeName = "Authorization";
        return new OpenAPI()
                .info(new Info()
                        .title("星光国际影城 CMS 接口文档")
                        .description(
                                "星光国际影城（Cinema Management System）后端接口文档，含用户购票端 / 员工检票端 / 管理后台三端接口。" +
                                "调试需鉴权接口：先调用 /auth/login 获取 token，点击右上角 Authorize 粘贴 token（可直接带 Bearer 前缀）。")
                        .version("v1.1.0"))
                .components(new Components().addSecuritySchemes(schemeName,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("登录接口返回的 token，可直接粘贴，也可带 Bearer 前缀")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }
}

package com.hg.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 文档配置。
 * 访问地址：http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI healthGuardianOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("慢病专病全周期一体化健康守护体系 API")
                .description("管理后台与移动端 REST 接口文档")
                .version("1.0.0")
                .contact(new Contact().name("health-guardian")));
    }
}

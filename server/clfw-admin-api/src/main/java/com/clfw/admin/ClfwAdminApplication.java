package com.clfw.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 管理后台启动类。
 */
@EnableAsync
@EnableTransactionManagement
@MapperScan("com.clfw.**.mapper")
@SpringBootApplication(scanBasePackages = "com.clfw")
public class ClfwAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClfwAdminApplication.class, args);
        System.out.println("""
                
                ----------------------------------------------------------
                  CLFW Platform 基础服务启动成功
                  接口文档: http://localhost:8080/doc.html
                ----------------------------------------------------------
                """);
    }
}

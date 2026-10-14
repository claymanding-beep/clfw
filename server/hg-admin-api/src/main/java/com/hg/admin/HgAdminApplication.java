package com.hg.admin;

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
@MapperScan("com.hg.**.mapper")
@SpringBootApplication(scanBasePackages = "com.hg")
public class HgAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(HgAdminApplication.class, args);
        System.out.println("""

                ----------------------------------------------------------
                  慢病专病全周期一体化健康守护体系 - 管理后台启动成功
                  接口文档: http://localhost:8080/doc.html
                ----------------------------------------------------------
                """);
    }
}

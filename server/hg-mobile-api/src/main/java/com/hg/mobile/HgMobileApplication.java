package com.hg.mobile;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 移动端（患者端）启动类。
 * 当前为最小骨架，后续接入患者登录、健康档案、随访等业务。
 */
@EnableAsync
@MapperScan("com.hg.**.mapper")
@SpringBootApplication(scanBasePackages = "com.hg")
public class HgMobileApplication {

    public static void main(String[] args) {
        SpringApplication.run(HgMobileApplication.class, args);
        System.out.println("""

                ----------------------------------------------------------
                  慢病专病全周期一体化健康守护体系 - 移动端接口启动成功
                  接口文档: http://localhost:8081/doc.html
                ----------------------------------------------------------
                """);
    }
}

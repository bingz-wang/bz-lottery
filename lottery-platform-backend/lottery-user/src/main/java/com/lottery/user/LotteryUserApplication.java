package com.lottery.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
@MapperScan("com.lottery.user.infrastructure.mapper")
@SpringBootApplication(scanBasePackages = "com.lottery")
public class LotteryUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryUserApplication.class, args);
    }
}

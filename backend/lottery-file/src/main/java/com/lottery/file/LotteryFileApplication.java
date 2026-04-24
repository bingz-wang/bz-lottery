package com.lottery.file;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@EnableDubbo
@MapperScan("com.lottery.file.infrastructure.mapper")
@SpringBootApplication(scanBasePackages = "com.lottery", exclude = DataSourceAutoConfiguration.class)
public class LotteryFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryFileApplication.class, args);
    }
}

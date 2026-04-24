package com.lottery.monitor;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@EnableDubbo
@MapperScan("com.lottery.monitor.infrastructure.mapper")
@SpringBootApplication(scanBasePackages = "com.lottery", exclude = DataSourceAutoConfiguration.class)
public class LotteryMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryMonitorApplication.class, args);
    }
}

package com.lottery.workflow;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@EnableDubbo
@MapperScan("com.lottery.workflow.infrastructure.mapper")
@SpringBootApplication(scanBasePackages = "com.lottery", exclude = DataSourceAutoConfiguration.class)
public class LotteryWorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryWorkflowApplication.class, args);
    }
}

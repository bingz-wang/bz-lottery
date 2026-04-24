package com.lottery.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.lottery.ai.infrastructure.mapper")
@SpringBootApplication
public class LotteryAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryAiApplication.class, args);
    }
}

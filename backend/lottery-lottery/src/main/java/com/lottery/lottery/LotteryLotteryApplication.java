package com.lottery.lottery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.lottery.lottery.infrastructure.mapper")
@SpringBootApplication(scanBasePackages = "com.lottery")
public class LotteryLotteryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryLotteryApplication.class, args);
    }
}

package com.lottery.award;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.lottery.award.infrastructure.mapper")
@SpringBootApplication(scanBasePackages = "com.lottery")
public class LotteryAwardApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryAwardApplication.class, args);
    }
}

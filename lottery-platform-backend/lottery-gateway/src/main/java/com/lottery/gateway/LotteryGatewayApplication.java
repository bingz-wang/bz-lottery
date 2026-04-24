package com.lottery.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lottery")
public class LotteryGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryGatewayApplication.class, args);
    }
}

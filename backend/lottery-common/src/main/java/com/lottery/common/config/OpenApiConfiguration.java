package com.lottery.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI lotteryOpenApi(@Value("${spring.application.name:lottery-service}") String applicationName) {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .description("Lottery platform skeleton service API document.")
                        .version("1.0.0-SNAPSHOT")
                        .contact(new Contact().name("lottery-platform")));
    }
}


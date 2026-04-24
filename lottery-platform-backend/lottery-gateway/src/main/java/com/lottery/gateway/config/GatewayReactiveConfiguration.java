package com.lottery.gateway.config;

import com.lottery.gateway.support.TraceIdConstants;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class GatewayReactiveConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader(CorsConfiguration.ALL);
        config.addAllowedMethod(HttpMethod.GET.name());
        config.addAllowedMethod(HttpMethod.POST.name());
        config.addAllowedMethod(HttpMethod.PUT.name());
        config.addAllowedMethod(HttpMethod.DELETE.name());
        config.addAllowedMethod(HttpMethod.PATCH.name());
        config.addAllowedMethod(HttpMethod.OPTIONS.name());
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    @Bean
    public WebFilter traceWebFilter() {
        return (exchange, chain) -> {
            String traceId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(TraceIdConstants.TRACE_ID))
                    .filter(header -> !header.isBlank())
                    .orElseGet(() -> UUID.randomUUID().toString().replace("-", ""));
            exchange.getResponse().getHeaders().add(TraceIdConstants.TRACE_ID, traceId);
            return chain.filter(exchange)
                    .contextWrite(context -> context.put(TraceIdConstants.MDC_TRACE_ID, traceId))
                    .doFirst(() -> MDC.put(TraceIdConstants.MDC_TRACE_ID, traceId))
                    .doFinally(signalType -> MDC.remove(TraceIdConstants.MDC_TRACE_ID));
        };
    }
}

package com.lottery.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.common.response.ApiResponse;
import com.lottery.common.response.CommonErrorCode;
import com.lottery.gateway.config.GatewayAuthProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class GatewayAuthFilter implements org.springframework.cloud.gateway.filter.GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final GatewayAuthProperties authProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GatewayAuthFilter(GatewayAuthProperties authProperties,
                             @LoadBalanced WebClient.Builder webClientBuilder,
                             ObjectMapper objectMapper) {
        this.authProperties = authProperties;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (HttpMethod.OPTIONS.equals(request.getMethod()) || isPublicPath(path) || !isProtectedPath(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return writeUnauthorized(exchange, "Login required before accessing lottery resources");
        }

        return webClient.get()
                .uri("http://lottery-user/api/user/auth/me")
                .header(authProperties.getTokenName(), token)
                .retrieve()
                .toBodilessEntity()
                .flatMap(response -> chain.filter(exchange))
                .onErrorResume(error -> writeUnauthorized(exchange, "Session expired or invalid token"));
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(String path) {
        return authProperties.getPublicPaths().stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private boolean isProtectedPath(String path) {
        return authProperties.getProtectedPaths().stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private String resolveToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(authProperties.getTokenName());
        if (StringUtils.hasText(token)) {
            return token;
        }
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, String message) {
        byte[] body = toJsonBytes(ApiResponse.failure(CommonErrorCode.UNAUTHORIZED.code(), message));
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory()
                .wrap(body)));
    }

    private byte[] toJsonBytes(ApiResponse<Void> response) {
        try {
            return objectMapper.writeValueAsBytes(response);
        } catch (JsonProcessingException exception) {
            return "{\"code\":\"401\",\"message\":\"Authentication required\"}".getBytes(StandardCharsets.UTF_8);
        }
    }
}

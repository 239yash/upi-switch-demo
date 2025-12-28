package com.upi_switch.demo.configuration.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestMethodLogFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest originalRequest = exchange.getRequest();
        log.info("[REQUEST_LOG] request logging method: {}, path: {}",
                originalRequest.getMethod(),
                originalRequest.getPath());
        return chain.filter(exchange);
    }
}

package com.upi_switch.demo.configuration.filter;

import jakarta.annotation.Nullable;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcFilter implements WebFilter {
    @Override
    public Mono<Void> filter(@Nullable ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange == null) {
            throw new IllegalArgumentException("server web exchange must not be null");
        }
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        return chain
                .filter(exchange)
                .contextWrite(reactorContext -> reactorContext.put("traceId", traceId))
                .doFinally(signal -> MDC.clear());
    }
}

package com.upi_switch.demo.service.impl;

import com.upi_switch.demo.model.dto.RateLimitingResultDTO;
import com.upi_switch.demo.service.RateLimitingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@Slf4j
public class RollingWindowRateLimitingServiceImpl implements RateLimitingService {

    private static final int MAX_REQUESTS = 20;
    private static final Duration WINDOW = Duration.ofSeconds(60);
    private final ConcurrentHashMap<String, Deque<Instant>> store =
            new ConcurrentHashMap<>();

    @Override
    public Mono<RateLimitingResultDTO> checkLimit(String merchantId) {
        return Mono.fromSupplier(() -> {
            Instant now = Instant.now();
            Instant windowStart = now.minus(WINDOW);
            Deque<Instant> deque =
                    store.computeIfAbsent(merchantId, k -> new ConcurrentLinkedDeque<>());
            synchronized (deque) {
                while (!deque.isEmpty() && deque.peekFirst().isBefore(windowStart)) {
                    deque.pollFirst();
                }
                if (deque.size() >= MAX_REQUESTS) {
                    long retryAfter = Duration.between(
                            now,
                            deque.peekFirst().plus(WINDOW)
                    ).getSeconds();
                    return RateLimitingResultDTO.builder()
                            .allowed(false)
                            .retryAfterSeconds(Math.max(retryAfter, 1))
                            .build();
                }
                deque.addLast(now);
                return RateLimitingResultDTO.builder()
                        .allowed(true)
                        .retryAfterSeconds(0)
                        .build();
            }
        });
    }
}


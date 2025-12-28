package com.upi_switch.demo.service;

import com.upi_switch.demo.service.impl.RollingWindowRateLimitingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RollingWindowRateLimitingServiceImplTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RollingWindowRateLimitingServiceImpl();
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        String merchantId = "M123";

        for (int i = 0; i < 20; i++) {
            StepVerifier.create(rateLimitingService.checkLimit(merchantId))
                    .assertNext(result -> {
                        assertThat(result.isAllowed()).isTrue();
                        assertThat(result.getRetryAfterSeconds()).isZero();
                    })
                    .verifyComplete();
        }
    }

    @Test
    void shouldBlockWhenLimitExceeded() {
        String merchantId = "M456";

        // exhaust the limit
        for (int i = 0; i < 20; i++) {
            StepVerifier.create(rateLimitingService.checkLimit(merchantId))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        // 21st request should be blocked
        StepVerifier.create(rateLimitingService.checkLimit(merchantId))
                .assertNext(result -> {
                    assertThat(result.isAllowed()).isFalse();
                    assertThat(result.getRetryAfterSeconds()).isGreaterThan(0);
                })
                .verifyComplete();
    }

    @Test
    void shouldApplyRateLimitPerMerchant() {
        String merchantA = "M-A";
        String merchantB = "M-B";

        // exhaust merchant: A
        for (int i = 0; i < 20; i++) {
            StepVerifier.create(rateLimitingService.checkLimit(merchantA))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        // merchant A should now be blocked
        StepVerifier.create(rateLimitingService.checkLimit(merchantA))
                .assertNext(result -> assertThat(result.isAllowed()).isFalse())
                .verifyComplete();

        // merchant B should still be allowed
        StepVerifier.create(rateLimitingService.checkLimit(merchantB))
                .assertNext(result -> assertThat(result.isAllowed()).isTrue())
                .verifyComplete();
    }

    @Test
    void shouldAllowAgainAfterWindowExpires() {
        String merchantId = "M789";

        // exhaust the limit
        for (int i = 0; i < 20; i++) {
            StepVerifier.create(rateLimitingService.checkLimit(merchantId))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        // blocked
        StepVerifier.create(rateLimitingService.checkLimit(merchantId))
                .assertNext(result -> assertThat(result.isAllowed()).isFalse())
                .verifyComplete();

        // wait for window to expire
        StepVerifier.withVirtualTime(() -> {
                    try {
                        Thread.sleep(Duration.ofSeconds(61).toMillis());
                    } catch (InterruptedException ignored) {}
                    return rateLimitingService.checkLimit(merchantId);
                })
                .assertNext(result -> assertThat(result.isAllowed()).isTrue())
                .verifyComplete();
    }
}


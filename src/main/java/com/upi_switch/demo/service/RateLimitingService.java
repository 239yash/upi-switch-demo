package com.upi_switch.demo.service;

import com.upi_switch.demo.model.dto.RateLimitingResultDTO;
import reactor.core.publisher.Mono;

public interface RateLimitingService {

    Mono<RateLimitingResultDTO> checkLimit(String merchantId);
}

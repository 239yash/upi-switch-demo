package com.upi_switch.demo.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitingResultDTO {

    private boolean allowed;
    private long retryAfterSeconds;
}


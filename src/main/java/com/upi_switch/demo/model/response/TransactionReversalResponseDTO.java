package com.upi_switch.demo.model.response;

import com.upi_switch.demo.constant.TransactionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class TransactionReversalResponseDTO {
    private String originalRrn;
    private String reversalRrn;
    private TransactionStatus status;
    private Instant createdAt;
}


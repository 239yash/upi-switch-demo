package com.upi_switch.demo.model.response;

import com.upi_switch.demo.constant.TransactionStatus;
import com.upi_switch.demo.constant.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
public class TransactionResponseDTO {

    private String rrn;
    private TransactionType type;
    private String payerVpa;
    private String merchantId;
    private BigDecimal amount;
    private TransactionStatus status;
    private String failureReason;
    private String issuerRef;
    private Instant createdAt;
    private Instant updatedAt;
}
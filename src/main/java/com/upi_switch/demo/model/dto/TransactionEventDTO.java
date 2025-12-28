package com.upi_switch.demo.model.dto;

import com.upi_switch.demo.constant.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TransactionEventDTO {

    private TransactionStatus eventType;
    private Instant timestamp;
    private String details;
}


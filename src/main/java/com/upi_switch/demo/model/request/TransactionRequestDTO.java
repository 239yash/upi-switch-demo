package com.upi_switch.demo.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionRequestDTO {

    @NotBlank
    private String rrn;

    @NotBlank
    private String payerVpa;

    @NotBlank
    private String merchantId;

    @NotNull
    @Positive
    private BigDecimal amount;
    private String note;
}
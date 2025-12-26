package com.upi_switch.demo.model.dto;

import com.upi_switch.demo.constant.MerchantStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDTO {

    private String merchantId;
    private String name;
    private String mcc;
    private MerchantStatus status;
    private BigDecimal dailyLimit;
    private BigDecimal perTxnLimit;
    private Instant createdAt;
    private Instant updatedAt;
}

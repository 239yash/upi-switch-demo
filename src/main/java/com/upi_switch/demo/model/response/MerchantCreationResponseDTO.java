package com.upi_switch.demo.model.response;


import com.upi_switch.demo.constant.MerchantStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
public class MerchantCreationResponseDTO {

    private String merchantId;
    private String name;
    private String mcc;
    private MerchantStatus status;
    private BigDecimal dailyLimit;
    private BigDecimal perTxnLimit;
    private Instant createdAt;
    private Instant updatedAt;
}


package com.upi_switch.demo.model.request;

import com.upi_switch.demo.constant.MerchantStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MerchantCreationRequestDTO {

    @NotBlank
    private String merchantId;

    @NotBlank
    private String name;

    @NotBlank
    private String mcc;

    @NotNull
    private MerchantStatus status;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal dailyLimit;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal perTxnLimit;
}

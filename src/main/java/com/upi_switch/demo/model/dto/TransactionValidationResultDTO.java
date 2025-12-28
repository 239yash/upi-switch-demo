package com.upi_switch.demo.model.dto;

import com.upi_switch.demo.constant.TransactionValidationFailureReasonCode;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionValidationResultDTO {

    private boolean valid;
    private String message;
    private TransactionValidationFailureReasonCode reasonCode;
}

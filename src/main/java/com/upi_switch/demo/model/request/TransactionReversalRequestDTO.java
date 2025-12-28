package com.upi_switch.demo.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionReversalRequestDTO {
    private String originalRrn;
    private String reversalRrn;
}


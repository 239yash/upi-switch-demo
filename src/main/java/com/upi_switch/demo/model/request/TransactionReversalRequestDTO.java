package com.upi_switch.demo.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TransactionReversalRequestDTO {
    private String originalRrn;
    private String reversalRrn;
}


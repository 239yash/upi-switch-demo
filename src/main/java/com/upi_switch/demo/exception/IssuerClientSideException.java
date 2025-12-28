package com.upi_switch.demo.exception;

import lombok.Getter;

@Getter
public class IssuerClientSideException extends RuntimeException {

    private final String issuerRef;

    public IssuerClientSideException(String message, String issuerRef) {
        super(message);
        this.issuerRef = issuerRef;
    }

    public String getIssuerRef() {
        return issuerRef;
    }
}

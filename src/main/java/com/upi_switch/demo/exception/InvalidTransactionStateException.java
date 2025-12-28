package com.upi_switch.demo.exception;

public class InvalidTransactionStateException extends RuntimeException {

    public  InvalidTransactionStateException(String message) {
        super(message);
    }
}

package com.upi_switch.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ClientSideException extends RuntimeException {

    private final HttpStatus httpStatus;

    public ClientSideException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ClientSideException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}

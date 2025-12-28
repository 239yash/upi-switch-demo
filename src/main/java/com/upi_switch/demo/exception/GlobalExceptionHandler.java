package com.upi_switch.demo.exception;

import com.upi_switch.demo.model.response.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Mono<ResponseEntity<ResponseDTO<Void>>> handleBaseException(BaseException ex) {
        ResponseDTO<Void> response = ResponseDTO.error(ex.getMessage());
        return Mono.just(
                ResponseEntity
                        .status(ex.getHttpStatus())
                        .body(response)
        );
    }

    @ExceptionHandler(InvalidTransactionStateException.class)
    public Mono<ResponseEntity<ResponseDTO<Void>>> handleInvalidTransactionStateException(InvalidTransactionStateException ex) {
        ResponseDTO<Void> response = ResponseDTO.error(ex.getMessage());
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(response)
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ResponseDTO<Void>>> handleException(Exception ex) {
        ResponseDTO<Void> response = ResponseDTO.error(ex.getMessage());
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(response)
        );
    }
}


package com.upi_switch.demo.exception;

import com.upi_switch.demo.model.response.ResponseDTO;
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
}


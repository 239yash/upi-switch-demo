package com.upi_switch.demo.exception;

import com.upi_switch.demo.model.response.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
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
        log.error("[ERROR] invalid transaction state exception occurred: {}", ex.getMessage(), ex);
        ResponseDTO<Void> response = ResponseDTO.error(ex.getMessage());
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(response)
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ResponseDTO<Void>>> handleException(Exception ex) {
        log.error("[ERROR] exception occurred: {}", ex.getMessage(), ex);
        ResponseDTO<Void> response = ResponseDTO.error(ex.getMessage());
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(response)
        );
    }

    @ExceptionHandler(ClientSideException.class)
    public Mono<ResponseEntity<ResponseDTO<Void>>> handleClientSideException(ClientSideException ex) {
        log.error("[ERROR] client side exception occurred: {}", ex.getMessage());
        ResponseDTO<Void> response = ResponseDTO.error(ex.getMessage());
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response)
        );
    }

    @ExceptionHandler(IssuerClientSideException.class)
    public Mono<ResponseEntity<ResponseDTO<Void>>> handleIssuerClientException(IssuerClientSideException ex) {
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ResponseDTO.error(ex.getMessage()))
        );
    }

    @ExceptionHandler(IssuerErrorException.class)
    public Mono<ResponseEntity<ResponseDTO<Void>>> handleIssuerServerErrorException(IssuerErrorException ex) {
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.BAD_GATEWAY)
                        .body(ResponseDTO.error(ex.getMessage()))
        );
    }

    @ExceptionHandler(IssuerTimeoutException.class)
    public Mono<ResponseEntity<ResponseDTO<Void>>> handleIssuerTimeoutException(IssuerTimeoutException ex) {
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.GATEWAY_TIMEOUT)
                        .body(ResponseDTO.error(ex.getMessage()))
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ResponseDTO<Void>>> handleNotFoundException(NotFoundException ex) {
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ResponseDTO.error(ex.getMessage()))
        );
    }
}


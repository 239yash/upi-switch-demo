package com.upi_switch.demo.controller;

import com.upi_switch.demo.constant.TransactionType;
import com.upi_switch.demo.model.request.TransactionRequestDTO;
import com.upi_switch.demo.model.response.ResponseDTO;
import com.upi_switch.demo.model.response.TransactionDetailsResponseDTO;
import com.upi_switch.demo.model.response.TransactionResponseDTO;
import com.upi_switch.demo.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/upi")
@AllArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/pay")
    public Mono<ResponseEntity<ResponseDTO<TransactionResponseDTO>>> pay(
            @RequestBody TransactionRequestDTO request) {
        return transactionService
                .processTransaction(request, TransactionType.PAY)
                .map(response ->
                        ResponseEntity.ok(ResponseDTO.success(response))
                );
    }

    @PostMapping("/collect")
    public Mono<ResponseEntity<ResponseDTO<TransactionResponseDTO>>> collect(
            @RequestBody TransactionRequestDTO request) {
        return transactionService
                .processTransaction(request, TransactionType.COLLECT)
                .map(response ->
                        ResponseEntity.ok(ResponseDTO.success(response))
                );
    }

    @GetMapping("/transactions/{rrn}")
    public Mono<ResponseEntity<ResponseDTO<TransactionDetailsResponseDTO>>> getTransaction(
            @PathVariable String rrn) {
        return transactionService.getTransaction(rrn)
                .map(res -> ResponseEntity.ok(ResponseDTO.success(res)));
    }
}

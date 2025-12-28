package com.upi_switch.demo.service;

import com.upi_switch.demo.constant.TransactionType;
import com.upi_switch.demo.model.request.TransactionRequestDTO;
import com.upi_switch.demo.model.response.TransactionDetailsResponseDTO;
import com.upi_switch.demo.model.response.TransactionResponseDTO;
import reactor.core.publisher.Mono;

public interface TransactionService {

    Mono<TransactionResponseDTO> processTransaction(TransactionRequestDTO request, TransactionType transactionType);

    Mono<TransactionDetailsResponseDTO> getTransaction(String rrn);
}

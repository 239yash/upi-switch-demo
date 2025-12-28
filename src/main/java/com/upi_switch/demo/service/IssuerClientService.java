package com.upi_switch.demo.service;

import com.upi_switch.demo.model.request.TransactionRequestDTO;
import com.upi_switch.demo.model.response.IssuerResponseDTO;
import reactor.core.publisher.Mono;

public interface IssuerClientService {

    Mono<IssuerResponseDTO> processTransaction(TransactionRequestDTO request);}

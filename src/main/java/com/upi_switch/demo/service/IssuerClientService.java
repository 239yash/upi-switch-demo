package com.upi_switch.demo.service;

import com.upi_switch.demo.model.entity.TransactionEntity;
import com.upi_switch.demo.model.response.IssuerResponseDTO;
import reactor.core.publisher.Mono;

public interface IssuerClientService {

    Mono<IssuerResponseDTO> processTransaction(TransactionEntity request);}

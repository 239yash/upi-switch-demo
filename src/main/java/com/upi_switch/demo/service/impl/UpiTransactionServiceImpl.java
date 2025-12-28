package com.upi_switch.demo.service.impl;

import com.upi_switch.demo.configuration.StateMachineConfiguration;
import com.upi_switch.demo.constant.TransactionStatus;
import com.upi_switch.demo.constant.TransactionType;
import com.upi_switch.demo.exception.*;
import com.upi_switch.demo.model.dto.TransactionEventDTO;
import com.upi_switch.demo.model.entity.TransactionEntity;
import com.upi_switch.demo.model.entity.TransactionEventEntity;
import com.upi_switch.demo.model.request.TransactionRequestDTO;
import com.upi_switch.demo.model.response.IssuerResponseDTO;
import com.upi_switch.demo.model.response.TransactionDetailsResponseDTO;
import com.upi_switch.demo.model.response.TransactionResponseDTO;
import com.upi_switch.demo.repository.TransactionEventRepository;
import com.upi_switch.demo.repository.TransactionRepository;
import com.upi_switch.demo.service.IssuerClientService;
import com.upi_switch.demo.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class UpiTransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ValidationServiceImpl validationService;
    private final IssuerClientService issuerClient;
    private final TransactionEventAuditService auditService;
    private final StateMachineConfiguration stateMachine;
    private final TransactionEventRepository transactionEventRepository;

    @Override
    public Mono<TransactionResponseDTO> processTransaction(TransactionRequestDTO request, TransactionType transactionType) {
        return transactionRepository.findByRrn(request.getRrn())
                .flatMap(existing -> Mono.just(toResponse(existing)))
                .switchIfEmpty(
                        createTransaction(request, transactionType)
                                .flatMap(this::validateTransaction)
                                .flatMap(transactionEntity -> {
                                    if (transactionEntity.getStatus().name().equals("FAILED")) {
                                        return Mono.error(new ClientSideException("validations failed for the transaction with the reason: " + transactionEntity.getFailureReason()));
                                    } else {
                                        return sendToIssuer(transactionEntity);
                                    }
                                })
                                .map(this::toResponse)
                                .onErrorResume(this::handleFailure)
                );
    }

    private Mono<TransactionResponseDTO> handleFailure(Throwable ex) {
        log.error("[TXN_OPS] transaction processing failed: {}", ex.getMessage());
        return Mono.error(ex);
    }


    private Mono<TransactionEntity> createTransaction(TransactionRequestDTO request, TransactionType transactionType) {
        TransactionEntity txn = TransactionEntity.builder()
                .rrn(request.getRrn())
                .type(transactionType)
                .merchantId(request.getMerchantId())
                .payerVpa(request.getPayerVpa())
                .amount(request.getAmount())
                .status(TransactionStatus.INITIATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return transactionRepository.save(txn)
                .flatMap(saved -> auditService.audit(saved, TransactionStatus.INITIATED));
    }

    private Mono<TransactionEntity> validateTransaction(TransactionEntity txn) {
        return validationService.validate(txn)
                .flatMap(result -> {
                    if (!result.isValid()) {
                        log.info("[TXN_OPS] transaction validation failed: {}", result.getMessage());
                        stateMachine.validateTransition(txn.getStatus(), TransactionStatus.FAILED);
                        txn.setStatus(TransactionStatus.FAILED);
                        txn.setFailureReason(result.getMessage());
                        txn.setUpdatedAt(Instant.now());
                        return transactionRepository.save(txn)
                                .flatMap(saved ->
                                        auditService.audit(saved, TransactionStatus.FAILED, result.getMessage()));
                    }
                    stateMachine.validateTransition(
                            txn.getStatus(), TransactionStatus.VALIDATED);
                    txn.setStatus(TransactionStatus.VALIDATED);
                    txn.setUpdatedAt(Instant.now());
                    return transactionRepository.save(txn)
                            .flatMap(saved ->
                                    auditService.audit(saved, TransactionStatus.VALIDATED));
                });
    }

    private Mono<TransactionEntity> sendToIssuer(TransactionEntity txn) {
        stateMachine.validateTransition(
                txn.getStatus(), TransactionStatus.SENT_TO_BANK);
        txn.setStatus(TransactionStatus.SENT_TO_BANK);
        txn.setUpdatedAt(Instant.now());
        return transactionRepository.save(txn)
                .flatMap(saved ->
                        auditService.audit(saved, TransactionStatus.SENT_TO_BANK))
                .flatMap(this::callIssuer);
    }

    private Mono<TransactionEntity> callIssuer(TransactionEntity txn) {
        return issuerClient.processTransaction(txn)
                .flatMap(resp -> handleIssuerSuccess(txn, resp))
                .onErrorResume(IssuerTimeoutException.class, ex -> handleTimeout(txn))
                .onErrorResume(IssuerClientSideException.class, ex -> handleIssuerFailure(txn, ex.getMessage()))
                .onErrorResume(IssuerErrorException.class, ex -> handleIssuerFailure(txn, ex.getMessage()));
    }

    private Mono<TransactionEntity> handleIssuerSuccess(
            TransactionEntity txn,
            IssuerResponseDTO response) {
        stateMachine.validateTransition(
                txn.getStatus(), TransactionStatus.SUCCESS);
        txn.setStatus(TransactionStatus.SUCCESS);
        txn.setIssuerRef(response.getIssuerRef());
        txn.setFailureReason(null);
        txn.setUpdatedAt(Instant.now());
        return transactionRepository.save(txn)
                .flatMap(saved ->
                        auditService.audit(saved, TransactionStatus.SUCCESS));
    }

    private Mono<TransactionEntity> handleIssuerFailure(
            TransactionEntity txn,
            String reason) {
        stateMachine.validateTransition(
                txn.getStatus(), TransactionStatus.FAILED);
        txn.setStatus(TransactionStatus.FAILED);
        txn.setFailureReason(reason);
        txn.setUpdatedAt(Instant.now());
        return transactionRepository.save(txn)
                .flatMap(saved ->
                        auditService.audit(saved, TransactionStatus.FAILED, "issuer-failure"));
    }

    private Mono<TransactionEntity> handleTimeout(TransactionEntity txn) {
        stateMachine.validateTransition(
                txn.getStatus(), TransactionStatus.TIMEOUT);
        txn.setStatus(TransactionStatus.TIMEOUT);
        txn.setUpdatedAt(Instant.now());
        return transactionRepository.save(txn)
                .flatMap(saved ->
                        auditService.audit(saved, TransactionStatus.TIMEOUT, "issuer-timeout"));
    }

    private TransactionResponseDTO toResponse(TransactionEntity txn) {
        return TransactionResponseDTO.builder()
                .rrn(txn.getRrn())
                .type(txn.getType())
                .payerVpa(txn.getPayerVpa())
                .merchantId(txn.getMerchantId())
                .amount(txn.getAmount())
                .status(txn.getStatus())
                .failureReason(txn.getFailureReason())
                .issuerRef(txn.getIssuerRef())
                .createdAt(txn.getCreatedAt())
                .updatedAt(txn.getUpdatedAt())
                .build();
    }

    public Mono<TransactionDetailsResponseDTO> getTransaction(String rrn) {
        Mono<TransactionEntity> txnMono = transactionRepository.findByRrn(rrn)
                .switchIfEmpty(Mono.error(new NotFoundException("transaction not found with rrn: " + rrn)));
        Mono<List<TransactionEventEntity>> eventsMono = transactionEventRepository.findByRrnOrderByEventTimeAsc(rrn).collectList();
        return Mono.zip(txnMono, eventsMono)
                .map(tuple -> TransactionDetailsResponseDTO.builder()
                        .transaction(toResponse(tuple.getT1()))
                        .events(tuple.getT2().stream()
                                .map(this::toEventDTO)
                                .toList())
                        .build());
    }

    private TransactionEventDTO toEventDTO(TransactionEventEntity e) {
        return new TransactionEventDTO(e.getEventType(), e.getEventTime(), e.getDetails());
    }
}

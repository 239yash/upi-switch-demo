package com.upi_switch.demo.service.impl;

import com.upi_switch.demo.configuration.StateMachineConfiguration;
import com.upi_switch.demo.constant.TransactionStatus;
import com.upi_switch.demo.exception.InvalidTransactionStateException;
import com.upi_switch.demo.exception.NotFoundException;
import com.upi_switch.demo.model.entity.TransactionEntity;
import com.upi_switch.demo.model.request.TransactionReversalRequestDTO;
import com.upi_switch.demo.model.response.TransactionReversalResponseDTO;
import com.upi_switch.demo.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@AllArgsConstructor
@Service
public class TransactionReversalServiceImpl {

    private final TransactionRepository transactionRepository;
    private final TransactionEventAuditService auditService;
    private final StateMachineConfiguration stateMachine;

    public Mono<TransactionReversalResponseDTO> reverse(TransactionReversalRequestDTO request) {
        return transactionRepository.findByReversalRrn(request.getReversalRrn())
                .map(this::toResponse)
                .switchIfEmpty(Mono.defer(() -> processNewReversal(request)));
    }

    private Mono<TransactionReversalResponseDTO> processNewReversal(TransactionReversalRequestDTO request) {
        return transactionRepository.findByRrn(request.getOriginalRrn())
                .switchIfEmpty(Mono.error(
                        new NotFoundException("original transaction not found")))
                .flatMap(txn -> {
                    if (txn.getStatus() != TransactionStatus.TIMEOUT) {
                        return Mono.error(new InvalidTransactionStateException("reversal allowed only for TIMEOUT transactions"));
                    }
                    if (txn.getReversalRrn() != null) {
                        return Mono.just(txn);
                    }
                    stateMachine.validateTransition(
                            txn.getStatus(), TransactionStatus.REVERSAL_INITIATED);
                    txn.setStatus(TransactionStatus.REVERSAL_INITIATED);
                    txn.setReversalRrn(request.getReversalRrn());
                    txn.setUpdatedAt(Instant.now());
                    return transactionRepository.save(txn)
                            .flatMap(saved ->
                                    auditService.audit(txn, TransactionStatus.REVERSAL_INITIATED, "reversal-rrn-" +  request.getReversalRrn())
                            );
                })
                .flatMap(txn -> {
                    stateMachine.validateTransition(txn.getStatus(), TransactionStatus.REVERSED);
                    txn.setStatus(TransactionStatus.REVERSED);
                    txn.setUpdatedAt(Instant.now());
                    return transactionRepository.save(txn)
                            .flatMap(saved ->
                                    auditService.audit(txn, TransactionStatus.REVERSED, "reversal-done")
                            );
                })
                .map(txn -> TransactionReversalResponseDTO.builder()
                        .originalRrn(txn.getRrn())
                        .reversalRrn(txn.getReversalRrn())
                        .status(txn.getStatus())
                        .createdAt(txn.getUpdatedAt())
                        .build());
    }

    private TransactionReversalResponseDTO toResponse(TransactionEntity txn) {
        return TransactionReversalResponseDTO.builder()
                .originalRrn(txn.getRrn())
                .reversalRrn(txn.getReversalRrn())
                .status(txn.getStatus())
                .createdAt(txn.getUpdatedAt())
                .build();
    }
}

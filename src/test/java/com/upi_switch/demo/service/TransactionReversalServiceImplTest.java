package com.upi_switch.demo.service;

import com.upi_switch.demo.configuration.StateMachineConfiguration;
import com.upi_switch.demo.constant.TransactionStatus;
import com.upi_switch.demo.exception.InvalidTransactionStateException;
import com.upi_switch.demo.exception.NotFoundException;
import com.upi_switch.demo.model.entity.TransactionEntity;
import com.upi_switch.demo.model.request.TransactionReversalRequestDTO;
import com.upi_switch.demo.repository.TransactionRepository;
import com.upi_switch.demo.service.impl.TransactionEventAuditService;
import com.upi_switch.demo.service.impl.TransactionReversalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionReversalServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEventAuditService auditService;

    @Mock
    private StateMachineConfiguration stateMachine;

    @InjectMocks
    private TransactionReversalServiceImpl reversalService;
    private static final String ORIGINAL_RRN = "123456789012";
    private static final String REVERSAL_RRN = "R123456789999";

    @Test
    void shouldReturnExistingReversalIfReversalRrnExists() {

        TransactionEntity reversedTxn = TransactionEntity.builder()
                .rrn(ORIGINAL_RRN)
                .reversalRrn(REVERSAL_RRN)
                .status(TransactionStatus.REVERSED)
                .updatedAt(Instant.now())
                .build();
        when(transactionRepository.findByReversalRrn(REVERSAL_RRN))
                .thenReturn(Mono.just(reversedTxn));
        StepVerifier.create(
                        reversalService.reverse(
                                new TransactionReversalRequestDTO(ORIGINAL_RRN, REVERSAL_RRN)))
                .assertNext(resp -> {
                    assertEquals(ORIGINAL_RRN, resp.getOriginalRrn());
                    assertEquals(REVERSAL_RRN, resp.getReversalRrn());
                    assertEquals(TransactionStatus.REVERSED, resp.getStatus());
                })
                .verifyComplete();
        verify(transactionRepository, never()).save(any());
        verify(auditService, never()).audit(any(), any(), any());
    }

    @Test
    void shouldReverseTimeoutTransactionSuccessfully() {

        TransactionEntity timeoutTxn = TransactionEntity.builder()
                .rrn(ORIGINAL_RRN)
                .status(TransactionStatus.TIMEOUT)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(transactionRepository.findByReversalRrn(REVERSAL_RRN))
                .thenReturn(Mono.empty());
        when(transactionRepository.findByRrn(ORIGINAL_RRN))
                .thenReturn(Mono.just(timeoutTxn));
        when(transactionRepository.save(any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(auditService.audit(any(), any(), any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        StepVerifier.create(
                        reversalService.reverse(
                                new TransactionReversalRequestDTO(ORIGINAL_RRN, REVERSAL_RRN)))
                .assertNext(resp -> {
                    assertEquals(ORIGINAL_RRN, resp.getOriginalRrn());
                    assertEquals(REVERSAL_RRN, resp.getReversalRrn());
                    assertEquals(TransactionStatus.REVERSED, resp.getStatus());
                })
                .verifyComplete();
        verify(stateMachine).validateTransition(
                TransactionStatus.TIMEOUT, TransactionStatus.REVERSAL_INITIATED);
        verify(stateMachine).validateTransition(
                TransactionStatus.REVERSAL_INITIATED, TransactionStatus.REVERSED);

        verify(transactionRepository, times(2)).save(any());
        verify(auditService, times(2)).audit(any(), any(), any());
    }

    @Test
    void shouldFailIfTransactionIsNotTimeout() {

        TransactionEntity successTxn = TransactionEntity.builder()
                .rrn(ORIGINAL_RRN)
                .status(TransactionStatus.SUCCESS)
                .build();
        when(transactionRepository.findByReversalRrn(REVERSAL_RRN))
                .thenReturn(Mono.empty());
        when(transactionRepository.findByRrn(ORIGINAL_RRN))
                .thenReturn(Mono.just(successTxn));
        StepVerifier.create(
                        reversalService.reverse(
                                new TransactionReversalRequestDTO(ORIGINAL_RRN, REVERSAL_RRN)))
                .expectError(InvalidTransactionStateException.class)
                .verify();

        verify(transactionRepository, never()).save(any());
        verify(auditService, never()).audit(any(), any(), any());
    }

    @Test
    void shouldFailIfOriginalTransactionNotFound() {
        when(transactionRepository.findByReversalRrn(REVERSAL_RRN))
                .thenReturn(Mono.empty());
        when(transactionRepository.findByRrn(ORIGINAL_RRN))
                .thenReturn(Mono.empty());
        StepVerifier.create(
                        reversalService.reverse(
                                new TransactionReversalRequestDTO(ORIGINAL_RRN, REVERSAL_RRN)))
                .expectError(NotFoundException.class)
                .verify();
        verify(transactionRepository, never()).save(any());
        verify(auditService, never()).audit(any(), any(), any());
    }
}

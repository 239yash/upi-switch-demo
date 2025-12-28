package com.upi_switch.demo.service;

import com.upi_switch.demo.configuration.StateMachineConfiguration;
import com.upi_switch.demo.constant.TransactionStatus;
import com.upi_switch.demo.constant.TransactionType;
import com.upi_switch.demo.exception.IssuerClientSideException;
import com.upi_switch.demo.exception.IssuerTimeoutException;
import com.upi_switch.demo.exception.NotFoundException;
import com.upi_switch.demo.model.dto.TransactionValidationResultDTO;
import com.upi_switch.demo.model.entity.TransactionEntity;
import com.upi_switch.demo.model.entity.TransactionEventEntity;
import com.upi_switch.demo.model.request.TransactionRequestDTO;
import com.upi_switch.demo.model.response.IssuerResponseDTO;
import com.upi_switch.demo.repository.TransactionEventRepository;
import com.upi_switch.demo.repository.TransactionRepository;
import com.upi_switch.demo.service.impl.TransactionEventAuditService;
import com.upi_switch.demo.service.impl.UpiTransactionServiceImpl;
import com.upi_switch.demo.service.impl.ValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpiTransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ValidationServiceImpl validationService;
    @Mock
    private IssuerClientService issuerClient;
    @Mock
    private TransactionEventAuditService auditService;
    @Mock
    private StateMachineConfiguration stateMachine;
    @Mock
    private TransactionEventRepository transactionEventRepository;

    @InjectMocks
    private UpiTransactionServiceImpl service;

    private static final String RRN = "123456789012";
    private static final String MERCHANT_ID = "MID123";

    private TransactionRequestDTO request;
    private TransactionEntity txn;

    @BeforeEach
    void setup() {
        request = TransactionRequestDTO.builder()
                .rrn(RRN)
                .merchantId(MERCHANT_ID)
                .payerVpa("a@upi")
                .amount(BigDecimal.valueOf(100))
                .build();
        txn = TransactionEntity.builder()
                .rrn(RRN)
                .merchantId(MERCHANT_ID)
                .payerVpa("a@upi")
                .amount(BigDecimal.valueOf(100))
                .status(TransactionStatus.INITIATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void shouldProcessTransactionSuccessfully() {
        when(transactionRepository.findByRrn(RRN))
                .thenReturn(Mono.empty());
        when(transactionRepository.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(auditService.audit(any(), any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(validationService.validate(any()))
                .thenReturn(Mono.just(TransactionValidationResultDTO.builder()
                        .valid(true)
                        .build()));
        when(issuerClient.processTransaction(any()))
                .thenReturn(Mono.just(new IssuerResponseDTO(RRN, null, "ISS-123")));
        StepVerifier.create(
                        service.processTransaction(request, TransactionType.PAY))
                .assertNext(resp -> {
                    assertEquals(TransactionStatus.SUCCESS, resp.getStatus());
                    assertEquals("ISS-123", resp.getIssuerRef());
                })
                .verifyComplete();
    }

    @Test
    void shouldMarkTransactionTimeoutOnIssuerTimeout() {
        when(transactionRepository.findByRrn(RRN))
                .thenReturn(Mono.empty());
        when(transactionRepository.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(auditService.audit(any(), any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(auditService.audit(any(), any(), any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(validationService.validate(any()))
                .thenReturn(Mono.just(TransactionValidationResultDTO.builder()
                        .valid(true)
                        .build()));
        when(issuerClient.processTransaction(any()))
                .thenReturn(Mono.error(new IssuerTimeoutException("timeout")));
        StepVerifier.create(
                        service.processTransaction(request, TransactionType.PAY))
                .assertNext(resp ->
                        assertEquals(TransactionStatus.TIMEOUT, resp.getStatus()))
                .verifyComplete();
    }

    @Test
    void shouldFailTransactionOnIssuerFailure() {
        when(transactionRepository.findByRrn(RRN))
                .thenReturn(Mono.empty());
        when(transactionRepository.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(auditService.audit(any(), any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(auditService.audit(any(), any(), any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(validationService.validate(any()))
                .thenReturn(Mono.just(TransactionValidationResultDTO.builder()
                        .valid(true)
                        .build()));
        when(issuerClient.processTransaction(any()))
                .thenReturn(Mono.error(new IssuerClientSideException("issuer error", "ISS-X")));
        StepVerifier.create(
                        service.processTransaction(request, TransactionType.PAY))
                .assertNext(resp ->
                        assertEquals(TransactionStatus.FAILED, resp.getStatus()))
                .verifyComplete();
    }

    @Test
    void shouldReturnTransactionWithEvents() {
        when(transactionRepository.findByRrn(RRN))
                .thenReturn(Mono.just(txn));
        when(transactionEventRepository.findByRrnOrderByEventTimeAsc(RRN))
                .thenReturn(Flux.just(
                        TransactionEventEntity.builder()
                                .eventType(TransactionStatus.INITIATED)
                                .eventTime(Instant.now())
                                .build()
                ));
        StepVerifier.create(service.getTransaction(RRN))
                .assertNext(resp -> {
                    assertEquals(RRN, resp.getTransaction().getRrn());
                    assertEquals(1, resp.getEvents().size());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailIfTransactionNotFound() {
        when(transactionRepository.findByRrn(RRN))
                .thenReturn(Mono.empty());
        when(transactionEventRepository.findByRrnOrderByEventTimeAsc(RRN))
                .thenReturn(Flux.just());
        StepVerifier.create(service.getTransaction(RRN))
                .expectError(NotFoundException.class)
                .verify();
    }
}


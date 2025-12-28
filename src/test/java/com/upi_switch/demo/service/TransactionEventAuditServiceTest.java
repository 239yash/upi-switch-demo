package com.upi_switch.demo.service;

import com.upi_switch.demo.constant.TransactionStatus;
import com.upi_switch.demo.model.entity.TransactionEntity;
import com.upi_switch.demo.model.entity.TransactionEventEntity;
import com.upi_switch.demo.repository.TransactionEventRepository;
import com.upi_switch.demo.service.impl.TransactionEventAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionEventAuditServiceTest {

    private TransactionEventRepository eventRepository;
    private TransactionEventAuditService auditService;

    @BeforeEach
    void setUp() {
        eventRepository = mock(TransactionEventRepository.class);
        auditService = new TransactionEventAuditService(eventRepository);
    }

    @Test
    void shouldAuditEventWithoutDetails() {
        TransactionEntity txn = TransactionEntity.builder()
                .rrn("123456789012")
                .status(TransactionStatus.INITIATED)
                .createdAt(Instant.now())
                .build();
        when(eventRepository.save(any(TransactionEventEntity.class)))
                .thenAnswer(invocation -> {
                    TransactionEventEntity e = invocation.getArgument(0);
                    return Mono.just(e);
                });
        StepVerifier.create(
                        auditService.audit(txn, TransactionStatus.VALIDATED)
                )
                .assertNext(returnedTxn -> assertThat(returnedTxn).isSameAs(txn))
                .verifyComplete();
        ArgumentCaptor<TransactionEventEntity> captor =
                ArgumentCaptor.forClass(TransactionEventEntity.class);
        verify(eventRepository, times(1)).save(captor.capture());
        TransactionEventEntity savedEvent = captor.getValue();
        assertThat(savedEvent.getRrn()).isEqualTo("123456789012");
        assertThat(savedEvent.getEventType()).isEqualTo(TransactionStatus.VALIDATED);
        assertThat(savedEvent.getEventTime()).isNotNull();
        assertThat(savedEvent.getDetails()).isNull();
    }

    @Test
    void shouldAuditEventWithDetails() {
        TransactionEntity txn = TransactionEntity.builder()
                .rrn("987654321098")
                .status(TransactionStatus.TIMEOUT)
                .createdAt(Instant.now())
                .build();
        when(eventRepository.save(any(TransactionEventEntity.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        StepVerifier.create(
                        auditService.audit(
                                txn,
                                TransactionStatus.REVERSAL_INITIATED,
                                "reversalRrn=R123456789"
                        )
                )
                .assertNext(returnedTxn -> assertThat(returnedTxn).isSameAs(txn))
                .verifyComplete();
        ArgumentCaptor<TransactionEventEntity> captor =
                ArgumentCaptor.forClass(TransactionEventEntity.class);
        verify(eventRepository).save(captor.capture());
        TransactionEventEntity savedEvent = captor.getValue();
        assertThat(savedEvent.getRrn()).isEqualTo("987654321098");
        assertThat(savedEvent.getEventType()).isEqualTo(TransactionStatus.REVERSAL_INITIATED);
        assertThat(savedEvent.getDetails()).isEqualTo("reversalRrn=R123456789");
        assertThat(savedEvent.getEventTime()).isNotNull();
    }

    @Test
    void shouldPropagateRepositoryError() {
        TransactionEntity txn = TransactionEntity.builder()
                .rrn("111222333444")
                .build();
        when(eventRepository.save(any()))
                .thenReturn(Mono.error(new RuntimeException("db down")));
        StepVerifier.create(
                        auditService.audit(txn, TransactionStatus.FAILED)
                )
                .expectErrorMatches(ex ->
                        ex instanceof RuntimeException &&
                                ex.getMessage().equals("db down")
                )
                .verify();
    }
}

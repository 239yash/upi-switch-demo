package com.upi_switch.demo.service.impl;

import com.upi_switch.demo.constant.TransactionStatus;
import com.upi_switch.demo.model.entity.TransactionEntity;
import com.upi_switch.demo.model.entity.TransactionEventEntity;
import com.upi_switch.demo.repository.TransactionEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventAuditService {

    private final TransactionEventRepository eventRepository;

    public Mono<TransactionEntity> audit(TransactionEntity txn, TransactionStatus newStatus) {
        TransactionEventEntity event = TransactionEventEntity.builder()
                .rrn(txn.getRrn())
                .eventType(newStatus)
                .eventTime(Instant.now())
                .build();
        return eventRepository.save(event)
                .doOnSuccess(e -> log.info("[AUDIT-CREATE] auditing the event for rrn: {}, event: {}", txn.getRrn(), newStatus))
                .thenReturn(txn);
    }

    public Mono<TransactionEntity> audit(TransactionEntity txn, TransactionStatus newStatus, String details) {
        TransactionEventEntity event = TransactionEventEntity.builder()
                .rrn(txn.getRrn())
                .eventType(newStatus)
                .eventTime(Instant.now())
                .details(details)
                .build();
        return eventRepository.save(event)
                .doOnSuccess(e -> log.info("[AUDIT] auditing the event for rrn: {}, event: {}", txn.getRrn(), newStatus))
                .thenReturn(txn);
    }
}

package com.upi_switch.demo.repository;

import com.upi_switch.demo.constant.TransactionStatus;
import com.upi_switch.demo.model.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Repository
public interface TransactionRepository
        extends R2dbcRepository<TransactionEntity, String> {

    Mono<Boolean> existsByRrn(String rrn);

    Mono<TransactionEntity> findByRrn(String rrn);

    Flux<TransactionEntity> findByMerchantIdAndCreatedAtAfter(String merchantId, Instant after);

    Flux<TransactionEntity> findByMerchantIdAndStatusAndCreatedAtAfter(String merchantId, TransactionStatus status, Instant after);

    @Query("""
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE merchant_id = :merchantId
          AND status = 'SUCCESS'
          AND created_at >= :startOfDay
        """)
    Mono<BigDecimal> sumSuccessfulAmountForMerchantToday(String merchantId, Instant startOfDay);
}


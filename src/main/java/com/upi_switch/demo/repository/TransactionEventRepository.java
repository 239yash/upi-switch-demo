package com.upi_switch.demo.repository;

import com.upi_switch.demo.model.entity.TransactionEventEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TransactionEventRepository extends R2dbcRepository<TransactionEventEntity, Long> {

    Flux<TransactionEventEntity> findByRrnOrderByEventTimeAsc(String rrn);
}

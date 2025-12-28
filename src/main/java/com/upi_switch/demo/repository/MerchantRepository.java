package com.upi_switch.demo.repository;

import com.upi_switch.demo.model.entity.MerchantEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MerchantRepository extends R2dbcRepository<MerchantEntity, Long> {

    Mono<MerchantEntity> findByMerchantId(String merchantId);

    Mono<Boolean> existsByMerchantId(String merchantId);
}

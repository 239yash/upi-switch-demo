package com.upi_switch.demo.repository;

import com.upi_switch.demo.model.entity.MerchantEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface MerchantRepository extends R2dbcRepository<MerchantEntity, String> {
}

package com.upi_switch.demo.service.impl;

import com.upi_switch.demo.exception.BaseException;
import com.upi_switch.demo.model.entity.MerchantEntity;
import com.upi_switch.demo.model.request.MerchantCreationRequestDTO;
import com.upi_switch.demo.model.response.MerchantCreationResponseDTO;
import com.upi_switch.demo.repository.MerchantRepository;
import com.upi_switch.demo.service.MerchantService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;

    @Override
    public Mono<MerchantCreationResponseDTO> createMerchant(
            MerchantCreationRequestDTO request) {
        return merchantRepository.existsByMerchantId(request.getMerchantId())
                .flatMap(exists -> {
                    if (exists) {
                        log.error("[MERCHANT_OPS] duplicate merchant creation attempt: {}", request.getMerchantId());
                        return Mono.error(new BaseException("merchant already exists with id: " + request.getMerchantId(), HttpStatus.FORBIDDEN)
                        );
                    }
                    Instant now = Instant.now();
                    MerchantEntity merchant = MerchantEntity.builder()
                            .merchantId(request.getMerchantId())
                            .name(request.getName())
                            .mcc(request.getMcc())
                            .status(request.getStatus())
                            .dailyLimit(request.getDailyLimit())
                            .perTxnLimit(request.getPerTxnLimit())
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return merchantRepository
                            .save(merchant)
                            .flatMap(savedEntity -> {
                                log.info("[MERCHANT_OPS] saved merchant entity with id: {}", savedEntity.getMerchantId());
                                return Mono.just(toResponse(savedEntity));
                            });
                });
    }

    private MerchantCreationResponseDTO toResponse(MerchantEntity merchant) {
        return MerchantCreationResponseDTO.builder()
                .merchantId(merchant.getMerchantId())
                .name(merchant.getName())
                .mcc(merchant.getMcc())
                .status(merchant.getStatus())
                .dailyLimit(merchant.getDailyLimit())
                .perTxnLimit(merchant.getPerTxnLimit())
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .build();
    }
}


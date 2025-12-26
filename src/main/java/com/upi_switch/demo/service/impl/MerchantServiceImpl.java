package com.upi_switch.demo.service.impl;

import com.upi_switch.demo.exception.BaseException;
import com.upi_switch.demo.model.dto.MerchantDTO;
import com.upi_switch.demo.model.entity.MerchantEntity;
import com.upi_switch.demo.model.request.MerchantCreationRequestDTO;
import com.upi_switch.demo.model.response.MerchantCreationResponseDTO;
import com.upi_switch.demo.repository.MerchantRepository;
import com.upi_switch.demo.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;

    @Override
    public Mono<MerchantCreationResponseDTO> createMerchant(
            MerchantCreationRequestDTO request) {
        return merchantRepository.existsById(request.getMerchantId())
                .flatMap(exists -> {
                    if (exists) {
                        log.error("duplicate merchant creation attempt: {}", request.getMerchantId());
                        return Mono.error(new BaseException("merchantId already exists", HttpStatus.FORBIDDEN)
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
                    return merchantRepository.save(merchant)
                            .map(this::toResponse);
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


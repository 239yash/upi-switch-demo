package com.upi_switch.demo.service;

import com.upi_switch.demo.constant.MerchantStatus;
import com.upi_switch.demo.exception.BaseException;
import com.upi_switch.demo.model.entity.MerchantEntity;
import com.upi_switch.demo.model.request.MerchantCreationRequestDTO;
import com.upi_switch.demo.repository.MerchantRepository;
import com.upi_switch.demo.service.impl.MerchantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceImplTest {

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private MerchantServiceImpl merchantService;

    private MerchantCreationRequestDTO request;

    @BeforeEach
    void setUp() {
        request = MerchantCreationRequestDTO.builder()
                .merchantId("M123")
                .name("Test Merchant")
                .mcc("5411")
                .status(MerchantStatus.ACTIVE)
                .dailyLimit(BigDecimal.valueOf(100000L))
                .perTxnLimit(BigDecimal.valueOf(10000L))
                .build();
    }

    @Test
    void createMerchant_success() {
        MerchantEntity savedEntity = MerchantEntity.builder()
                .merchantId("M123")
                .name("Test Merchant")
                .mcc("5411")
                .status(MerchantStatus.ACTIVE)
                .dailyLimit(BigDecimal.valueOf(100000))
                .perTxnLimit(BigDecimal.valueOf(10000))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(merchantRepository.existsByMerchantId("M123"))
                .thenReturn(Mono.just(false));
        when(merchantRepository.save(any(MerchantEntity.class)))
                .thenReturn(Mono.just(savedEntity));
        StepVerifier.create(merchantService.createMerchant(request))
                .expectNextMatches(response ->
                        response.getMerchantId().equals("M123") &&
                                response.getName().equals("Test Merchant")
                )
                .verifyComplete();
        verify(merchantRepository).existsByMerchantId("M123");
        verify(merchantRepository).save(any(MerchantEntity.class));
    }

    @Test
    void createMerchant_duplicateMerchant() {
        when(merchantRepository.existsByMerchantId("M123"))
                .thenReturn(Mono.just(true));
        StepVerifier.create(merchantService.createMerchant(request))
                .expectErrorMatches(throwable ->
                        throwable instanceof BaseException &&
                                throwable.getMessage().contains("merchant already exists")
                )
                .verify();
        verify(merchantRepository).existsByMerchantId("M123");
        verify(merchantRepository, never()).save(any());
    }

    @Test
    void createMerchant_saveFailure() {
        when(merchantRepository.existsByMerchantId("M123"))
                .thenReturn(Mono.just(false));
        when(merchantRepository.save(any(MerchantEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("db down")));
        StepVerifier.create(merchantService.createMerchant(request))
                .expectErrorMatches(ex ->
                        ex instanceof RuntimeException &&
                                ex.getMessage().equals("db down")
                )
                .verify();
        verify(merchantRepository).save(any(MerchantEntity.class));
    }
}

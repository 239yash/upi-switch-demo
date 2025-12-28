package com.upi_switch.demo.service;

import com.upi_switch.demo.constant.MerchantStatus;
import com.upi_switch.demo.constant.TransactionValidationFailureReasonCode;
import com.upi_switch.demo.exception.NotFoundException;
import com.upi_switch.demo.model.dto.RateLimitingResultDTO;
import com.upi_switch.demo.model.entity.MerchantEntity;
import com.upi_switch.demo.model.entity.TransactionEntity;
import com.upi_switch.demo.repository.MerchantRepository;
import com.upi_switch.demo.repository.TransactionRepository;
import com.upi_switch.demo.service.impl.ValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RateLimitingService rateLimitingService;

    @InjectMocks
    private ValidationServiceImpl validationService;
    private static final String RRN = "123456789012";
    private static final String MERCHANT_ID = "MID123";
    private TransactionEntity txn;
    private MerchantEntity merchant;

    @BeforeEach
    void setup() {
        txn = TransactionEntity.builder()
                .rrn(RRN)
                .merchantId(MERCHANT_ID)
                .amount(BigDecimal.valueOf(100))
                .build();
        merchant = MerchantEntity.builder()
                .merchantId(MERCHANT_ID)
                .status(MerchantStatus.ACTIVE)
                .perTxnLimit(BigDecimal.valueOf(500))
                .dailyLimit(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    void shouldValidateSuccessfully() {
        when(transactionRepository.existsByRrn(RRN))
                .thenReturn(Mono.just(true));
        when(merchantRepository.findByMerchantId(MERCHANT_ID))
                .thenReturn(Mono.just(merchant));
        when(transactionRepository
                .sumSuccessfulAmountForMerchantToday(anyString(), any()))
                .thenReturn(Mono.just(BigDecimal.valueOf(200)));
        when(rateLimitingService.checkLimit(MERCHANT_ID))
                .thenReturn(Mono.just(
                        RateLimitingResultDTO.builder()
                                .allowed(true)
                                .retryAfterSeconds(0)
                                .build()));
        StepVerifier.create(validationService.validate(txn))
                .assertNext(resp -> {
                    assertTrue(resp.isValid());
                    assertNull(resp.getReasonCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailIfTransactionDoesNotExist() {
        when(transactionRepository.existsByRrn(RRN))
                .thenReturn(Mono.just(false));
        StepVerifier.create(validationService.validate(txn))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailIfMerchantNotFound() {
        when(transactionRepository.existsByRrn(RRN))
                .thenReturn(Mono.just(true));
        when(merchantRepository.findByMerchantId(MERCHANT_ID))
                .thenReturn(Mono.empty());
        StepVerifier.create(validationService.validate(txn))
                .assertNext(resp -> {
                    assertFalse(resp.isValid());
                    assertEquals(
                            TransactionValidationFailureReasonCode.MERCHANT_NOT_FOUND,
                            resp.getReasonCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailIfMerchantInactive() {
        merchant.setStatus(MerchantStatus.INACTIVE);
        when(transactionRepository.existsByRrn(RRN))
                .thenReturn(Mono.just(true));
        when(merchantRepository.findByMerchantId(MERCHANT_ID))
                .thenReturn(Mono.just(merchant));
        StepVerifier.create(validationService.validate(txn))
                .assertNext(resp -> {
                    assertFalse(resp.isValid());
                    assertEquals(
                            TransactionValidationFailureReasonCode.MERCHANT_INACTIVE,
                            resp.getReasonCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailIfPerTxnLimitExceeded() {
        txn.setAmount(BigDecimal.valueOf(1000));
        when(transactionRepository.existsByRrn(RRN))
                .thenReturn(Mono.just(true));
        when(merchantRepository.findByMerchantId(MERCHANT_ID))
                .thenReturn(Mono.just(merchant));
        StepVerifier.create(validationService.validate(txn))
                .assertNext(resp -> {
                    assertFalse(resp.isValid());
                    assertEquals(
                            TransactionValidationFailureReasonCode.PER_TXN_LIMIT_EXCEEDED,
                            resp.getReasonCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailIfDailyLimitExceeded() {
        when(transactionRepository.existsByRrn(RRN))
                .thenReturn(Mono.just(true));
        when(merchantRepository.findByMerchantId(MERCHANT_ID))
                .thenReturn(Mono.just(merchant));
        when(transactionRepository
                .sumSuccessfulAmountForMerchantToday(anyString(), any()))
                .thenReturn(Mono.just(BigDecimal.valueOf(950)));
        StepVerifier.create(validationService.validate(txn))
                .assertNext(resp -> {
                    assertFalse(resp.isValid());
                    assertEquals(
                            TransactionValidationFailureReasonCode.DAILY_LIMIT_EXCEEDED,
                            resp.getReasonCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailIfRateLimitExceeded() {
        when(transactionRepository.existsByRrn(RRN))
                .thenReturn(Mono.just(true));
        when(merchantRepository.findByMerchantId(MERCHANT_ID))
                .thenReturn(Mono.just(merchant));
        when(transactionRepository
                .sumSuccessfulAmountForMerchantToday(anyString(), any()))
                .thenReturn(Mono.just(BigDecimal.valueOf(100)));
        when(rateLimitingService.checkLimit(MERCHANT_ID))
                .thenReturn(Mono.just(
                        RateLimitingResultDTO.builder()
                                .allowed(false)
                                .retryAfterSeconds(10)
                                .build()));
        StepVerifier.create(validationService.validate(txn))
                .assertNext(resp -> {
                    assertFalse(resp.isValid());
                    assertEquals(
                            TransactionValidationFailureReasonCode.RATE_LIMIT_EXCEEDED,
                            resp.getReasonCode());
                })
                .verifyComplete();
    }
}


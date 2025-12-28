package com.upi_switch.demo.service.impl;

import com.upi_switch.demo.constant.MerchantStatus;
import com.upi_switch.demo.constant.TransactionValidationFailureReasonCode;
import com.upi_switch.demo.model.dto.TransactionValidationResultDTO;
import com.upi_switch.demo.model.entity.MerchantEntity;
import com.upi_switch.demo.model.request.TransactionRequestDTO;
import com.upi_switch.demo.repository.MerchantRepository;
import com.upi_switch.demo.repository.TransactionRepository;
import com.upi_switch.demo.service.RateLimitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationServiceImpl {

    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final RateLimitingService rateLimitingService;

    public Mono<TransactionValidationResultDTO> validate(TransactionRequestDTO req) {
        return transactionRepository.existsByRrn(req.getRrn())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(failure(
                                TransactionValidationFailureReasonCode.DUPLICATE_RRN,
                                "duplicate rrn"));
                    }
                    return merchantRepository.findByMerchantId(req.getMerchantId())
                            .flatMap(merchant -> validateAgainstMerchant(req, merchant))
                            .switchIfEmpty(Mono.just(failure(TransactionValidationFailureReasonCode.MERCHANT_NOT_FOUND, "merchant does not exist"
                            )));
                });
    }


    private Mono<TransactionValidationResultDTO> validateAgainstMerchant(
            TransactionRequestDTO req,
            MerchantEntity merchant) {
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            return Mono.just(failure(
                    TransactionValidationFailureReasonCode.MERCHANT_INACTIVE,
                    "merchant is not active"));
        }
        if (req.getAmount().compareTo(merchant.getPerTxnLimit()) > 0) {
            return Mono.just(failure(
                    TransactionValidationFailureReasonCode.PER_TXN_LIMIT_EXCEEDED,
                    "per transaction limit exceeded"));
        }
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
        return transactionRepository
                .sumSuccessfulAmountForMerchantToday(merchant.getMerchantId(), startOfDay)
                .flatMap(todayAmount -> {
                    if (todayAmount.add(req.getAmount())
                            .compareTo(merchant.getDailyLimit()) > 0) {
                        return Mono.just(failure(
                                TransactionValidationFailureReasonCode.DAILY_LIMIT_EXCEEDED,
                                "daily limit exceeded"));
                    }
                    return rateLimitingService.checkLimit(merchant.getMerchantId())
                            .flatMap(rateLimit -> {
                                if (!rateLimit.isAllowed()) {
                                    return Mono.just(failure(
                                            TransactionValidationFailureReasonCode.RATE_LIMIT_EXCEEDED,
                                            "too many requests, retry after "
                                                    + rateLimit.getRetryAfterSeconds() + " seconds"));
                                }
                                return Mono.just(success());
                            });
                });
    }

    private TransactionValidationResultDTO success() {
        return TransactionValidationResultDTO.builder()
                .valid(true)
                .build();
    }

    private TransactionValidationResultDTO failure(
            TransactionValidationFailureReasonCode code,
            String message) {

        return TransactionValidationResultDTO.builder()
                .valid(false)
                .reasonCode(code)
                .message(message)
                .build();
    }
}




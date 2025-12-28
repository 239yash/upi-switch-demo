package com.upi_switch.demo.service.impl;

import com.upi_switch.demo.exception.IssuerClientSideException;
import com.upi_switch.demo.exception.IssuerErrorException;
import com.upi_switch.demo.exception.IssuerTimeoutException;
import com.upi_switch.demo.model.request.TransactionRequestDTO;
import com.upi_switch.demo.model.response.IssuerResponseDTO;
import com.upi_switch.demo.service.IssuerClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class MockIssuerClientServiceImpl implements IssuerClientService {

    private static final Duration ISSUER_TIMEOUT = Duration.ofSeconds(3);
    private static final Random RANDOM = new Random();

    @Override
    public Mono<IssuerResponseDTO> processTransaction(TransactionRequestDTO request) {
        int outcome = RANDOM.nextInt(4);
        String rrn = request.getRrn();
        return Mono.defer(() -> switch (outcome) {
            case 0 -> Mono.just(new IssuerResponseDTO(rrn, null, generateIssuerRef()));
            case 1 -> Mono.error(new IssuerClientSideException("client side error", generateIssuerRef()));
            case 2 -> Mono.error(new IssuerErrorException("something went wrong with the issuer"));
            default -> Mono.delay(Duration.ofSeconds(10)).then(Mono.error(new TimeoutException("issuer timeout")));
        }).timeout(ISSUER_TIMEOUT)
                .onErrorMap(TimeoutException.class, ex -> new IssuerTimeoutException("issuer timeout"));
    }

    private String generateIssuerRef() {
        return "ISS-" + UUID.randomUUID().toString().substring(0, 8);
    }
}


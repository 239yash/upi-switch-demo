package com.upi_switch.demo.service;

import com.upi_switch.demo.exception.IssuerClientSideException;
import com.upi_switch.demo.exception.IssuerErrorException;
import com.upi_switch.demo.exception.IssuerTimeoutException;
import com.upi_switch.demo.model.entity.TransactionEntity;
import com.upi_switch.demo.service.impl.MockIssuerClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Random;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockIssuerClientServiceImplTest {

    @Mock
    private Random random;
    private MockIssuerClientServiceImpl service;
    private TransactionEntity transaction;

    @BeforeEach
    void setUp() {
        service = new MockIssuerClientServiceImpl(random);
        transaction = TransactionEntity.builder()
                .rrn("RRN123456789")
                .build();
    }

    @Test
    void processTransaction_success() {
        when(random.nextInt(4)).thenReturn(0);

        StepVerifier.create(service.processTransaction(transaction))
                .expectNextMatches(response ->
                        response.getRrn().equals("RRN123456789") && response.getIssuerRef() != null && response.getIssuerRef().startsWith("ISS-")
                )
                .verifyComplete();
    }

    @Test
    void processTransaction_clientSideError() {
        when(random.nextInt(4)).thenReturn(1);

        StepVerifier.create(service.processTransaction(transaction))
                .expectErrorMatches(ex ->
                        ex instanceof IssuerClientSideException &&
                                ex.getMessage().contains("client side error")
                )
                .verify();
    }

    @Test
    void processTransaction_issuerError() {
        when(random.nextInt(4)).thenReturn(2);

        StepVerifier.create(service.processTransaction(transaction))
                .expectError(IssuerErrorException.class)
                .verify();
    }

    @Test
    void processTransaction_timeout() {
        when(random.nextInt(4)).thenReturn(3);
        StepVerifier.withVirtualTime(() -> service.processTransaction(transaction))
                .thenAwait(Duration.ofSeconds(3))
                .expectError(IssuerTimeoutException.class)
                .verify();
    }
}

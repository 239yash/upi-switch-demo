package com.upi_switch.demo.configuration;

import com.upi_switch.demo.constant.TransactionStatus;
import com.upi_switch.demo.exception.InvalidTransactionStateException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.upi_switch.demo.constant.TransactionStatus.*;

@Component
public class StateMachineConfiguration {
    private static final Map<TransactionStatus, Set<TransactionStatus>> ALLOWED_TRANSITIONS =
            Map.of(
                    INITIATED, Set.of(VALIDATED, FAILED),
                    VALIDATED, Set.of(SENT_TO_BANK, FAILED),
                    SENT_TO_BANK, Set.of(SUCCESS, FAILED, TIMEOUT),
                    SUCCESS, Set.of(REVERSAL_INITIATED),
                    TIMEOUT, Set.of(REVERSAL_INITIATED),
                    REVERSAL_INITIATED, Set.of(REVERSED, REVERSAL_FAILED)
            );

    public void validateTransition(
            TransactionStatus from,
            TransactionStatus to) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw new InvalidTransactionStateException("invalid transition from " + from + " to " + to);
        }
    }
}

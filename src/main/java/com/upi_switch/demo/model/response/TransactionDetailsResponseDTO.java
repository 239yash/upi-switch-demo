package com.upi_switch.demo.model.response;

import com.upi_switch.demo.model.dto.TransactionEventDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TransactionDetailsResponseDTO {
    private TransactionResponseDTO transaction;
    private List<TransactionEventDTO> events;
}

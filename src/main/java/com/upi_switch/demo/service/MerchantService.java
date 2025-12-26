package com.upi_switch.demo.service;

import com.upi_switch.demo.model.request.MerchantCreationRequestDTO;
import com.upi_switch.demo.model.response.MerchantCreationResponseDTO;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface MerchantService {
    Mono<MerchantCreationResponseDTO> createMerchant(@Valid MerchantCreationRequestDTO request);
}

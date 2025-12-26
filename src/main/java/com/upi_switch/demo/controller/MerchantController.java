package com.upi_switch.demo.controller;

import com.upi_switch.demo.model.request.MerchantCreationRequestDTO;
import com.upi_switch.demo.model.response.MerchantCreationResponseDTO;
import com.upi_switch.demo.model.response.ResponseDTO;
import com.upi_switch.demo.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping
    public Mono<ResponseEntity<ResponseDTO<MerchantCreationResponseDTO>>> createMerchant(
            @Valid @RequestBody MerchantCreationRequestDTO request) {
        return merchantService.createMerchant(request)
                .map(ResponseDTO::success)
                .map(response ->
                        ResponseEntity.status(HttpStatus.CREATED).body(response)
                );
    }
}

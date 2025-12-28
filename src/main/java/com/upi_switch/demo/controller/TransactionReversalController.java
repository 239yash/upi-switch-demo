package com.upi_switch.demo.controller;

import com.upi_switch.demo.model.request.TransactionReversalRequestDTO;
import com.upi_switch.demo.model.response.ResponseDTO;
import com.upi_switch.demo.model.response.TransactionReversalResponseDTO;
import com.upi_switch.demo.service.impl.TransactionReversalServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("/upi")
public class TransactionReversalController {

    private final TransactionReversalServiceImpl reversalService;

    @PostMapping("/reversal")
    public Mono<ResponseEntity<ResponseDTO<TransactionReversalResponseDTO>>> reverse(
            @RequestBody TransactionReversalRequestDTO request) {
        return reversalService.reverse(request)
                .map(ResponseDTO::success)
                .map(response ->
                        ResponseEntity.status(HttpStatus.OK).body(response)
                );
    }
}


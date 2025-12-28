package com.upi_switch.demo.model.response;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IssuerResponseDTO {

    private String rrn;
    private String failureReason;
    private String issuerRef;
}

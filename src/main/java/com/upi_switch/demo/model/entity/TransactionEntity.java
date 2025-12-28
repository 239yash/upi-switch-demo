package com.upi_switch.demo.model.entity;

import com.upi_switch.demo.constant.TransactionStatus;
import com.upi_switch.demo.constant.TransactionType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("transaction")
public class TransactionEntity {

    @Id
    @Column("rrn")
    private String rrn;

    @Column("type")
    private TransactionType type;

    @Column("merchant_id")
    private String merchantId;

    @Column("payer_vpa")
    private String payerVpa;

    @Column("amount")
    private BigDecimal amount;

    @Column("status")
    private TransactionStatus status;

    @Column("failure_reason")
    private String failureReason;

    @Column("issuer_ref")
    private String issuerRef;

    @Column("reversal_rrn")
    private String reversalRrn;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}
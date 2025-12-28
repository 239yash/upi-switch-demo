package com.upi_switch.demo.model.entity;

import com.upi_switch.demo.constant.MerchantStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("merchant")
public class MerchantEntity {

    @Id
    private Long id;

    @Column("merchant_id")
    private String merchantId;

    @Column("name")
    private String name;

    @Column("mcc")
    private String mcc;

    @Column("status")
    private MerchantStatus status;

    @Column("daily_limit")
    private BigDecimal dailyLimit;

    @Column("per_txn_limit")
    private BigDecimal perTxnLimit;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;
}

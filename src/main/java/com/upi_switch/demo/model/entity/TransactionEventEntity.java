package com.upi_switch.demo.model.entity;

import com.upi_switch.demo.constant.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table("transaction_events")
public class TransactionEventEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("rrn")
    private String rrn;

    @Column("event_type")
    private TransactionStatus eventType;

    @Column("event_time")
    private Instant eventTime;

    @Column("details")
    private String details;
}

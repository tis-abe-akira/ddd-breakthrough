package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position relatedPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amount_pie_id")
    private AmountPie amountPie;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column
    private LocalDateTime processedDate;

    @Version
    private Long version;
}

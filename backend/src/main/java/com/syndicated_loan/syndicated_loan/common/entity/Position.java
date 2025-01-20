package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "share_pie_id")
    private SharePie sharePie;

    @Version
    private Long version;
}

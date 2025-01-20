package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Investor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "investor_type", nullable = false)
    private String type;

    @Column(name = "investment_capacity", precision = 19, scale = 4)
    private BigDecimal investmentCapacity;

    @Column(name = "current_investments", precision = 19, scale = 4)
    private BigDecimal currentInvestments;

    @Version
    private Long version;
}

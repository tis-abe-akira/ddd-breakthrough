package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@Getter
@Setter
public abstract class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", insertable = false, updatable = false)
    private String type;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "share_pie_id")
    private SharePie sharePie;

    @Version
    private Long version;

    /**
     * ポジションの金額。
     * Facilityの場合、この値はtotalAmountと同じ値となる。
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;
}

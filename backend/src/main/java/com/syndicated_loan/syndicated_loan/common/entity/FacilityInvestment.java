package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class FacilityInvestment extends Transaction {
    @ManyToOne
    @JoinColumn(name = "investor_id", nullable = false)
    private Investor investor;

    @Column(name = "investment_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal investmentAmount;

    @PrePersist
    public void prePersist() {
        setType("FACILITY_INVESTMENT");
    }
}

/*

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
 
 */

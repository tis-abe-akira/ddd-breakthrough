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

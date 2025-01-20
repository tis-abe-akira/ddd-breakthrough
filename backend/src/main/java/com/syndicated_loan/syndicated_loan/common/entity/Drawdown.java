package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Drawdown extends Transaction {
    @Column(name = "drawdown_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal drawdownAmount;

    @ManyToOne
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility relatedFacility;

    @PrePersist
    public void prePersist() {
        setType("DRAWDOWN");
    }
}

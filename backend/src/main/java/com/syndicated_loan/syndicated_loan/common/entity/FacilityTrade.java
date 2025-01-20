package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class FacilityTrade extends Transaction {
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Investor seller;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private Investor buyer;

    @Column(name = "trade_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal tradeAmount;

    @PrePersist
    public void prePersist() {
        setType("FACILITY_TRADE");
    }
}

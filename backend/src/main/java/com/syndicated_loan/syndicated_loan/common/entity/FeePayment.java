package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class FeePayment extends Transaction {
    @Column(name = "fee_type", nullable = false)
    private String feeType;

    @Column(name = "payment_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal paymentAmount;

    @ManyToOne
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @PrePersist
    public void prePersist() {
        setType("FEE_PAYMENT");
    }
}

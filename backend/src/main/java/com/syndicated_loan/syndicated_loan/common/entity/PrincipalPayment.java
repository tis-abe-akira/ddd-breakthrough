package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class PrincipalPayment extends Transaction {
    @Column(name = "payment_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal paymentAmount;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @PrePersist
    public void prePersist() {
        setType("PRINCIPAL_PAYMENT");
    }
}

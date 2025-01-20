package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class InterestPayment extends Transaction {
    @Column(name = "interest_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "payment_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal paymentAmount;

    @Column(name = "interest_start_date", nullable = false)
    private LocalDate interestStartDate;

    @Column(name = "interest_end_date", nullable = false)
    private LocalDate interestEndDate;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @PrePersist
    public void prePersist() {
        setType("INTEREST_PAYMENT");
    }
}

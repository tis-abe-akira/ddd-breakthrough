package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Loan extends Position {
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "available_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal availableAmount;

    // @ManyToOne
    // @JoinColumn(name = "borrower_id", nullable = false)
    // private Borrower borrower;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "term", nullable = false)
    private Integer term;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "interest_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal interestRate;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepaymentSchedule> repaymentSchedules = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        setType("LOAN");
        if (startDate != null && term != null) {
            endDate = startDate.plusMonths(term);
        }
        if (startDate != null && endDate != null) {
            term = (int) (endDate.toEpochDay() - startDate.toEpochDay()) / 30;
        }
    }

    public void addRepaymentSchedule(RepaymentSchedule schedule) {
        schedule.setLoan(this);
        this.repaymentSchedules.add(schedule);
    }
}

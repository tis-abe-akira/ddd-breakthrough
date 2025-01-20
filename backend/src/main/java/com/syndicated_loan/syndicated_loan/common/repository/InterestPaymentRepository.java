package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.InterestPayment;
import com.syndicated_loan.syndicated_loan.common.entity.Loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InterestPaymentRepository extends TransactionRepository<InterestPayment> {
    List<InterestPayment> findByLoan(Loan loan);
    List<InterestPayment> findByInterestRateGreaterThan(BigDecimal rate);
    List<InterestPayment> findByPaymentAmountGreaterThan(BigDecimal amount);
    List<InterestPayment> findByInterestStartDateBetween(LocalDate startDate, LocalDate endDate);
    List<InterestPayment> findByLoanAndInterestStartDateBetween(Loan loan, LocalDate startDate, LocalDate endDate);
}

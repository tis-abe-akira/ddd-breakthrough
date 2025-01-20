package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.PrincipalPayment;
import com.syndicated_loan.syndicated_loan.common.entity.Loan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrincipalPaymentRepository extends TransactionRepository<PrincipalPayment> {
    List<PrincipalPayment> findByLoan(Loan loan);
    List<PrincipalPayment> findByPaymentAmountGreaterThan(BigDecimal amount);
    List<PrincipalPayment> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<PrincipalPayment> findByLoanAndDateBetween(Loan loan, LocalDateTime startDate, LocalDateTime endDate);
    List<PrincipalPayment> findByLoanOrderByDateAsc(Loan loan);
}

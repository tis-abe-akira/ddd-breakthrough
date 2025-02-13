package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.RepaymentSchedule;
import com.syndicated_loan.syndicated_loan.common.entity.Loan;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByLoan(Loan loan);
    List<RepaymentSchedule> findByLoanAndScheduledDateBetween(Loan loan, LocalDate startDate, LocalDate endDate);
    List<RepaymentSchedule> findByScheduledDateAndStatus(LocalDate date, RepaymentSchedule.PaymentStatus status);
}
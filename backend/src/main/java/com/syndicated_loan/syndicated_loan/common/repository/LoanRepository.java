package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.Loan;
import com.syndicated_loan.syndicated_loan.common.entity.Borrower;
import com.syndicated_loan.syndicated_loan.common.entity.Facility;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByBorrower(Borrower borrower);
    List<Loan> findByFacility(Facility facility);
    List<Loan> findByTotalAmountGreaterThan(BigDecimal amount);
    List<Loan> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
    List<Loan> findByEndDateAfter(LocalDate date);
    List<Loan> findByBorrowerAndEndDateAfter(Borrower borrower, LocalDate date);
}

package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.Investor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestorRepository extends JpaRepository<Investor, Long> {
    Optional<Investor> findByName(String name);
    List<Investor> findByNameContaining(String namePattern);
    List<Investor> findByType(String type);
    List<Investor> findByInvestmentCapacityGreaterThan(BigDecimal amount);
    List<Investor> findByCurrentInvestmentsLessThan(BigDecimal amount);
    List<Investor> findByTypeAndInvestmentCapacityGreaterThan(String type, BigDecimal amount);
}

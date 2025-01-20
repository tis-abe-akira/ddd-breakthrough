package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.FacilityInvestment;
import com.syndicated_loan.syndicated_loan.common.entity.Investor;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FacilityInvestmentRepository extends TransactionRepository<FacilityInvestment> {
    List<FacilityInvestment> findByInvestor(Investor investor);
    List<FacilityInvestment> findByInvestmentAmountGreaterThan(BigDecimal amount);
}

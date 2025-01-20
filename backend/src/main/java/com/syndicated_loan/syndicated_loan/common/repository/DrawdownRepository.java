package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.Drawdown;
import com.syndicated_loan.syndicated_loan.common.entity.Facility;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DrawdownRepository extends TransactionRepository<Drawdown> {
    List<Drawdown> findByRelatedFacility(Facility facility);
    List<Drawdown> findByDrawdownAmountGreaterThan(BigDecimal amount);
    List<Drawdown> findByRelatedFacilityAndDrawdownAmountGreaterThan(Facility facility, BigDecimal amount);
}

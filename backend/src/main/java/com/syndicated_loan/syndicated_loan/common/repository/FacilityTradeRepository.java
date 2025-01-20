package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.FacilityTrade;
import com.syndicated_loan.syndicated_loan.common.entity.Investor;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FacilityTradeRepository extends TransactionRepository<FacilityTrade> {
    List<FacilityTrade> findBySeller(Investor seller);
    List<FacilityTrade> findByBuyer(Investor buyer);
    List<FacilityTrade> findByTradeAmountGreaterThan(BigDecimal amount);
    List<FacilityTrade> findBySellerOrBuyer(Investor seller, Investor buyer);
}

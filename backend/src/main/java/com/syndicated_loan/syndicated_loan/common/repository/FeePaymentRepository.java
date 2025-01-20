package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.FeePayment;
import com.syndicated_loan.syndicated_loan.common.entity.Facility;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FeePaymentRepository extends TransactionRepository<FeePayment> {
    List<FeePayment> findByFacility(Facility facility);
    List<FeePayment> findByFeeType(String feeType);
    List<FeePayment> findByPaymentAmountGreaterThan(BigDecimal amount);
    List<FeePayment> findByFacilityAndFeeType(Facility facility, String feeType);
}

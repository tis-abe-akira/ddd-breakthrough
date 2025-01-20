package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.Facility;
import com.syndicated_loan.syndicated_loan.common.entity.Syndicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    List<Facility> findBySyndicate(Syndicate syndicate);
    List<Facility> findByTotalAmountGreaterThan(BigDecimal amount);
    List<Facility> findByEndDateAfter(LocalDate date);
    List<Facility> findByAvailableAmountGreaterThan(BigDecimal amount);
}

package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.AmountPie;
import com.syndicated_loan.syndicated_loan.common.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AmountPieRepository extends JpaRepository<AmountPie, Long> {
    Optional<AmountPie> findByTransaction(Transaction transaction);

    @Query("SELECT ap FROM AmountPie ap JOIN ap.amounts amounts WHERE KEY(amounts) = :investorId")
    List<AmountPie> findByInvestorId(@Param("investorId") Long investorId);

    @Query("SELECT ap FROM AmountPie ap JOIN ap.amounts amounts WHERE VALUE(amounts) >= :minAmount")
    List<AmountPie> findByMinimumAmount(@Param("minAmount") BigDecimal minAmount);

    @Query("SELECT ap FROM AmountPie ap JOIN ap.amounts amounts " +
           "WHERE KEY(amounts) = :investorId AND VALUE(amounts) >= :minAmount")
    List<AmountPie> findByInvestorIdAndMinimumAmount(
        @Param("investorId") Long investorId,
        @Param("minAmount") BigDecimal minAmount
    );

    @Query("SELECT SUM(VALUE(amounts)) FROM AmountPie ap JOIN ap.amounts amounts " +
           "WHERE KEY(amounts) = :investorId")
    BigDecimal sumAmountsByInvestorId(@Param("investorId") Long investorId);
}

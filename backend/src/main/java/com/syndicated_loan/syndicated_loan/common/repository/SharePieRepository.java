package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.SharePie;
import com.syndicated_loan.syndicated_loan.common.entity.Position;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SharePieRepository extends JpaRepository<SharePie, Long> {
    Optional<SharePie> findByPosition(Position position);

    @Query("SELECT sp FROM SharePie sp JOIN sp.shares shares WHERE KEY(shares) = :investorId")
    List<SharePie> findByInvestorId(@Param("investorId") Long investorId);

    @Query("SELECT sp FROM SharePie sp JOIN sp.shares shares WHERE VALUE(shares) >= :minShare")
    List<SharePie> findByMinimumShare(@Param("minShare") BigDecimal minShare);

    @Query("SELECT sp FROM SharePie sp JOIN sp.shares shares " +
           "WHERE KEY(shares) = :investorId AND VALUE(shares) >= :minShare")
    List<SharePie> findByInvestorIdAndMinimumShare(
        @Param("investorId") Long investorId,
        @Param("minShare") BigDecimal minShare
    );
}

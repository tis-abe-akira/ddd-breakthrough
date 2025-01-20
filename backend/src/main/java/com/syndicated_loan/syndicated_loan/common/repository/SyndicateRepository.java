package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.Syndicate;
import com.syndicated_loan.syndicated_loan.common.entity.Investor;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SyndicateRepository extends JpaRepository<Syndicate, Long> {
    List<Syndicate> findByLeadBank(Investor leadBank);
    
    @Query("SELECT s FROM Syndicate s JOIN s.members m WHERE m = :member")
    List<Syndicate> findByMember(@Param("member") Investor member);
    
    List<Syndicate> findByTotalCommitmentGreaterThan(BigDecimal amount);
    
    @Query("SELECT s FROM Syndicate s WHERE SIZE(s.members) >= :minMembers")
    List<Syndicate> findByMinimumMembers(@Param("minMembers") int minMembers);
    
    @Query("SELECT s FROM Syndicate s WHERE s.leadBank = :leadBank AND SIZE(s.members) >= :minMembers")
    List<Syndicate> findByLeadBankAndMinimumMembers(
        @Param("leadBank") Investor leadBank,
        @Param("minMembers") int minMembers
    );
}

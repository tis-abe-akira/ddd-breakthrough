package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.Borrower;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    Optional<Borrower> findByName(String name);
    List<Borrower> findByNameContaining(String namePattern);
    List<Borrower> findByIndustry(String industry);
    List<Borrower> findByCompanyType(String companyType);
    List<Borrower> findByCreditRating(String creditRating);
    List<Borrower> findByIndustryAndCreditRating(String industry, String creditRating);
}

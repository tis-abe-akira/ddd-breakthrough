package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Borrower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "credit_rating")
    private String creditRating;

    @Column(name = "financial_statements")
    @Lob
    private String financialStatements;

    @Column(name = "contact_information")
    private String contactInformation;

    @Column(name = "company_type", nullable = false)
    private String companyType;

    @Column(nullable = false)
    private String industry;

    @Version
    private Long version;
}

package com.syndicated_loan.syndicated_loan.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowerDto {
    private Long id;
    private String name;
    private String creditRating;
    private String financialStatements;
    private String contactInformation;
    private String companyType;
    private String industry;
    private Long version;
}

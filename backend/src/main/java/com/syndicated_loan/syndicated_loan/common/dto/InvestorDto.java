package com.syndicated_loan.syndicated_loan.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestorDto {
    private Long id;
    private String name;
    private String type;
    private BigDecimal investmentCapacity;
    private BigDecimal currentInvestments;
    private Long version;
}

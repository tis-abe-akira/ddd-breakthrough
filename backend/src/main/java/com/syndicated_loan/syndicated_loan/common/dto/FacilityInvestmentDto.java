package com.syndicated_loan.syndicated_loan.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class FacilityInvestmentDto extends TransactionDto {
    private Long investorId;
    // private BigDecimal investmentAmount;

    // レスポンス用の追加フィールド
    private InvestorDto investor;
    private BigDecimal investmentShare; // 投資シェア（パーセンテージ）
    private String investmentStatus; // 投資のステータス（COMMITTED, FUNDED など）
}


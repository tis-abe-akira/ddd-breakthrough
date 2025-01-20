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
public class DrawdownDto extends TransactionDto {
    private BigDecimal drawdownAmount;
    private Long relatedFacilityId;

    // レスポンス用の追加フィールド
    private FacilityDto relatedFacility;
    private BigDecimal remainingFacilityAmount; // ドローダウン後のファシリティ残額
    private String drawdownPurpose; // ドローダウンの目的
    private String drawdownStatus; // ドローダウンのステータス（REQUESTED, APPROVED, EXECUTED など）
    private BigDecimal utilizationRate; // ファシリティの利用率（ドローダウン後）
}

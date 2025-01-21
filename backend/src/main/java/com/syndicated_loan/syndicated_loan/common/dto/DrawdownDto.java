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
    private AmountPieDto newAmountPie; // AmountPie生成情報を追加

    // レスポンス用の追加フィールド
    private FacilityDto relatedFacility;
    private BigDecimal remainingFacilityAmount; // ドローダウン後のファシリティ残額
    private String drawdownPurpose; // ドローダウンの目的
    private String drawdownStatus; // ドローダウンのステータス（REQUESTED, APPROVED, EXECUTED など）
    private BigDecimal utilizationRate; // ファシリティの利用率（ドローダウン後）
}

/*

 date 2025-01-27T00:00:00
 processedDate

 amount 999
 positionId
 amountPieId
 status

    private Long id;
    private String type;
    private LocalDateTime date;
    private BigDecimal amount;
    private Long relatedPositionId;
    private Long amountPieId; //TODO: share pie で分ける場合には未セットで良い？
    private String status;
    private LocalDateTime processedDate;
    private Long version;

 */

package com.syndicated_loan.syndicated_loan.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)  // これ重要！
public class FeePaymentDto extends TransactionDto {
    private String feeType;
    private BigDecimal paymentAmount;
    private Long facilityId;

    // レスポンス用の追加フィールド
    private FacilityDto facility;
    private String feeDescription; // 手数料の説明
    private String paymentPeriod; // 支払い対象期間
    private String calculationMethod; // 計算方法の説明
    private BigDecimal feeRate; // 手数料率（該当する場合）
    private String paymentStatus; // 支払いのステータス（SCHEDULED, PAID, OVERDUE など）
}

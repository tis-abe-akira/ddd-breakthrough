package com.syndicated_loan.syndicated_loan.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@SuperBuilder
public class InterestPaymentDto extends TransactionDto {
    private BigDecimal interestRate;
    private BigDecimal paymentAmount;
    private LocalDate interestStartDate;
    private LocalDate interestEndDate;
    private Long loanId;

    // レスポンス用の追加フィールド
    private LoanDto loan;
    private Integer daysInPeriod; // 利息計算期間の日数
    private BigDecimal baseAmount; // 利息計算の基準となる金額
    private String calculationMethod; // 利息計算方法（例：実日数/360、30/360など）
    private String paymentStatus; // 支払いのステータス（SCHEDULED, PAID, OVERDUE など）
    private BigDecimal penaltyRate; // 延滞利息率（該当する場合）
    private BigDecimal penaltyAmount; // 延滞利息額（該当する場合）
}

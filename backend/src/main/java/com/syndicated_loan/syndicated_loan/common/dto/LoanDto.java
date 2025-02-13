package com.syndicated_loan.syndicated_loan.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDto {
    private Long id;
    private String type;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private Long borrowerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal interestRate;
    private Long facilityId;
    private Long sharePieId;
    private Long version;
    private Integer term; // 追加！期間（月数）

    // レスポンス用の追加フィールド
    private BorrowerDto borrower;
    private FacilityDto facility;
    private SharePieDto sharePie;
    private BigDecimal remainingAmount; // 残額
    private BigDecimal repaidAmount; // 返済済み金額
    private LocalDate nextPaymentDate; // 次回支払日
    private BigDecimal nextPaymentAmount; // 次回支払額
}

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
public class FacilityDto {
    private Long id;
    // private String type; // "FACILITY" がEntityの@PrePersistでセットされる
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private BigDecimal availableAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal interestRate;
    private Long syndicateId;
    private Long sharePieId;
    private Long version;

    // レスポンス用の追加フィールド
    private SyndicateDto syndicate;
    private SharePieDto sharePie;
    private BigDecimal utilizationRate; // 利用率（availableAmount / totalAmount）
}

package com.syndicated_loan.syndicated_loan.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmountPieDto {
    private Long id;
    private Long transactionId;
    @Builder.Default
    private Map<Long, BigDecimal> amounts = new HashMap<>();
    private Long version;

    // レスポンス用の追加フィールド
    @Builder.Default
    private Map<String, BigDecimal> investorAmounts = new HashMap<>(); // 投資家名をキーとしたマップ
    private BigDecimal totalAmount; // 合計金額
}

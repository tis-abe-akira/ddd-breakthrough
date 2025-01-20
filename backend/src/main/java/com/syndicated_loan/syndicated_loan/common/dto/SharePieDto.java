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
public class SharePieDto {
    private Long id;
    private Long positionId;
    @Builder.Default
    private Map<Long, BigDecimal> shares = new HashMap<>();
    private Long version;

    // レスポンス用の追加フィールド
    @Builder.Default
    private Map<String, BigDecimal> investorShares = new HashMap<>(); // 投資家名をキーとしたマップ
}

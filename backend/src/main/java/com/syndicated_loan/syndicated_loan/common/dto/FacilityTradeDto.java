package com.syndicated_loan.syndicated_loan.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@SuperBuilder
public class FacilityTradeDto extends TransactionDto {
    private Long sellerId;
    private Long buyerId;
    private BigDecimal tradeAmount;

    // レスポンス用の追加フィールド
    private InvestorDto seller;
    private InvestorDto buyer;
    private BigDecimal tradeShare; // 取引シェア（パーセンテージ）
    private String tradeStatus; // 取引のステータス（PENDING, COMPLETED, CANCELLED など）
    private BigDecimal pricePerShare; // シェアあたりの価格（額面に対するプレミアム/ディスカウント）
}

package com.syndicated_loan.syndicated_loan.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@SuperBuilder
public class TransactionDto {
    private Long id;
    private String type;
    private LocalDateTime date;
    private BigDecimal amount;
    private Long relatedPositionId;
    private Long amountPieId;
    private String status;
    private LocalDateTime processedDate;
    private Long version;

    // レスポンス用の追加フィールド
    private String positionType; // FACILITY or LOAN
    private String positionReference; // 関連するポジションの参照情報（ファシリティ名やローン番号など）
    private AmountPieDto amountPie;
}

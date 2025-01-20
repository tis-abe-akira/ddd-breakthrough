package com.syndicated_loan.syndicated_loan.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyndicateDto {
    private Long id;
    private Long leadBankId;
    @Builder.Default
    private Set<Long> memberIds = new HashSet<>();
    private BigDecimal totalCommitment;
    private Long version;

    // レスポンス用の追加フィールド
    private InvestorDto leadBank;
    @Builder.Default
    private Set<InvestorDto> members = new HashSet<>();
}

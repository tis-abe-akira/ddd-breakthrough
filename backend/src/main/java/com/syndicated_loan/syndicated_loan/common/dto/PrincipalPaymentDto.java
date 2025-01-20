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
public class PrincipalPaymentDto extends TransactionDto {
    private Long loanId;
    private BigDecimal paymentAmount;
    private BigDecimal repaymentRate;
    private BigDecimal remainingBalance;
    private LoanDto loan;
}

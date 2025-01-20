package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.PrincipalPaymentDto;
import com.syndicated_loan.syndicated_loan.common.entity.PrincipalPayment;
import com.syndicated_loan.syndicated_loan.common.entity.Loan;
import com.syndicated_loan.syndicated_loan.common.repository.PrincipalPaymentRepository;
import com.syndicated_loan.syndicated_loan.common.repository.LoanRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PrincipalPaymentService 
    extends TransactionService<PrincipalPayment, PrincipalPaymentDto, PrincipalPaymentRepository> {

    private final LoanService loanService;
    private final LoanRepository loanRepository;

    public PrincipalPaymentService(
            PrincipalPaymentRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            LoanService loanService,
            LoanRepository loanRepository) {
        super(repository, amountPieService, positionService);
        this.loanService = loanService;
        this.loanRepository = loanRepository;
    }

    @Override
    public PrincipalPayment toEntity(PrincipalPaymentDto dto) {
        PrincipalPayment entity = new PrincipalPayment();
        entity.setId(dto.getId());
        entity.setType("PRINCIPAL_PAYMENT");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getAmount());

        // 関連するローンの設定
        Loan loan = loanService.findById(dto.getLoanId())
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        entity.setLoan(loan);

        // 関連するPositionとAmountPieの設定
        setBaseProperties(entity, dto);

        entity.setPaymentAmount(dto.getPaymentAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    @Override
    public PrincipalPaymentDto toDto(PrincipalPayment entity) {
        PrincipalPaymentDto dto = PrincipalPaymentDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .date(entity.getDate())
                .amount(entity.getAmount())
                .relatedPositionId(entity.getRelatedPosition().getId())
                .amountPieId(entity.getAmountPie() != null ? entity.getAmountPie().getId() : null)
                .loanId(entity.getLoan().getId())
                .paymentAmount(entity.getPaymentAmount())
                .version(entity.getVersion())
                .build();

        // レスポンス用の追加情報
        setBaseDtoProperties(dto, entity);
        dto.setLoan(loanService.toDto(entity.getLoan()));

        // 返済率の計算
        if (entity.getLoan().getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal repaymentRate = entity.getPaymentAmount()
                    .divide(entity.getLoan().getTotalAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            dto.setRepaymentRate(repaymentRate);
        }

        // 残高の計算
        List<PrincipalPayment> previousPayments = repository.findByLoanOrderByDateAsc(entity.getLoan());
        BigDecimal totalPaid = previousPayments.stream()
                .filter(payment -> payment.getDate().isBefore(entity.getDate()) || 
                                 payment.getDate().equals(entity.getDate()))
                .map(PrincipalPayment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setRemainingBalance(entity.getLoan().getTotalAmount().subtract(totalPaid));

        return dto;
    }

    // 追加の検索メソッド
    public List<PrincipalPaymentDto> findByLoan(Long loanId) {
        Loan loan = loanService.findById(loanId)
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        return repository.findByLoan(loan).stream()
                .map(this::toDto)
                .toList();
    }

    public List<PrincipalPaymentDto> findByPaymentAmountGreaterThan(BigDecimal amount) {
        return repository.findByPaymentAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    public List<PrincipalPaymentDto> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByDateBetween(startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    public List<PrincipalPaymentDto> findByLoanAndDateBetween(Long loanId, LocalDateTime startDate, LocalDateTime endDate) {
        Loan loan = loanService.findById(loanId)
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        return repository.findByLoanAndDateBetween(loan, startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    // 元本返済の実行（ローン残高も更新）
    @Transactional
    public PrincipalPaymentDto executePrincipalPayment(Long principalPaymentId) {
        PrincipalPayment principalPayment = repository.findById(principalPaymentId)
                .orElseThrow(() -> new BusinessException("Principal payment not found", "PRINCIPAL_PAYMENT_NOT_FOUND"));

        // 支払い済みの場合はエラー
        if ("EXECUTED".equals(principalPayment.getStatus())) {
            throw new BusinessException("Principal payment already executed", "PRINCIPAL_PAYMENT_ALREADY_EXECUTED");
        }

        // 返済額がローン残高を超えていないかチェック
        Loan loan = principalPayment.getLoan();
        List<PrincipalPayment> previousPayments = repository.findByLoanOrderByDateAsc(loan);
        BigDecimal totalPaid = previousPayments.stream()
                .filter(payment -> "EXECUTED".equals(payment.getStatus()))
                .map(PrincipalPayment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingBalance = loan.getTotalAmount().subtract(totalPaid);

        if (principalPayment.getPaymentAmount().compareTo(remainingBalance) > 0) {
            throw new BusinessException("Payment amount exceeds remaining balance", "PAYMENT_EXCEEDS_BALANCE");
        }

        // ステータスを更新
        principalPayment.setStatus("EXECUTED");
        principalPayment.setProcessedDate(java.time.LocalDateTime.now());

        // ローン残高を更新
        loan.setAmount(remainingBalance.subtract(principalPayment.getPaymentAmount()));
        loanRepository.save(loan);

        return toDto(repository.save(principalPayment));
    }

    // 返済金額の更新（実行前のみ可能）
    @Transactional
    public PrincipalPaymentDto updatePaymentAmount(Long principalPaymentId, BigDecimal newAmount) {
        PrincipalPayment principalPayment = repository.findById(principalPaymentId)
                .orElseThrow(() -> new BusinessException("Principal payment not found", "PRINCIPAL_PAYMENT_NOT_FOUND"));

        if ("EXECUTED".equals(principalPayment.getStatus())) {
            throw new BusinessException("Cannot update executed principal payment", "PRINCIPAL_PAYMENT_ALREADY_EXECUTED");
        }

        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be positive", "INVALID_PAYMENT_AMOUNT");
        }

        // 返済額がローン残高を超えていないかチェック
        Loan loan = principalPayment.getLoan();
        List<PrincipalPayment> previousPayments = repository.findByLoanOrderByDateAsc(loan);
        BigDecimal totalPaid = previousPayments.stream()
                .filter(payment -> "EXECUTED".equals(payment.getStatus()))
                .map(PrincipalPayment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingBalance = loan.getTotalAmount().subtract(totalPaid);

        if (newAmount.compareTo(remainingBalance) > 0) {
            throw new BusinessException("Payment amount exceeds remaining balance", "PAYMENT_EXCEEDS_BALANCE");
        }

        principalPayment.setPaymentAmount(newAmount);
        principalPayment.setAmount(newAmount); // 取引金額も更新

        return toDto(repository.save(principalPayment));
    }
}

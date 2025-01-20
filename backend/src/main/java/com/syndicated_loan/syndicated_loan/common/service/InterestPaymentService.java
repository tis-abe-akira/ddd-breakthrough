package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.InterestPaymentDto;
import com.syndicated_loan.syndicated_loan.common.entity.InterestPayment;
import com.syndicated_loan.syndicated_loan.common.entity.Loan;
import com.syndicated_loan.syndicated_loan.common.repository.InterestPaymentRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class InterestPaymentService 
    extends TransactionService<InterestPayment, InterestPaymentDto, InterestPaymentRepository> {

    private final LoanService loanService;

    public InterestPaymentService(
            InterestPaymentRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            LoanService loanService) {
        super(repository, amountPieService, positionService);
        this.loanService = loanService;
    }

    @Override
    public InterestPayment toEntity(InterestPaymentDto dto) {
        InterestPayment entity = new InterestPayment();
        entity.setId(dto.getId());
        entity.setType("INTEREST_PAYMENT");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getAmount());

        // 関連するローンの設定
        Loan loan = loanService.findById(dto.getLoanId())
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        entity.setLoan(loan);

        // 関連するPositionとAmountPieの設定
        setBaseProperties(entity, dto);

        entity.setInterestRate(dto.getInterestRate());
        entity.setPaymentAmount(dto.getPaymentAmount());
        entity.setInterestStartDate(dto.getInterestStartDate());
        entity.setInterestEndDate(dto.getInterestEndDate());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    @Override
    public InterestPaymentDto toDto(InterestPayment entity) {
        InterestPaymentDto dto = InterestPaymentDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .date(entity.getDate())
                .amount(entity.getAmount())
                .relatedPositionId(entity.getRelatedPosition().getId())
                .amountPieId(entity.getAmountPie() != null ? entity.getAmountPie().getId() : null)
                .loanId(entity.getLoan().getId())
                .interestRate(entity.getInterestRate())
                .paymentAmount(entity.getPaymentAmount())
                .interestStartDate(entity.getInterestStartDate())
                .interestEndDate(entity.getInterestEndDate())
                .version(entity.getVersion())
                .build();

        // レスポンス用の追加情報
        setBaseDtoProperties(dto, entity);
        dto.setLoan(loanService.toDto(entity.getLoan()));

        // 利息計算期間の日数
        dto.setDaysInPeriod((int) ChronoUnit.DAYS.between(entity.getInterestStartDate(), entity.getInterestEndDate()));

        // 利息計算の基準となる金額（ローン残高）
        dto.setBaseAmount(entity.getLoan().getAmount());

        // 利息計算方法
        dto.setCalculationMethod("ACTUAL/360");

        return dto;
    }

    // 追加の検索メソッド
    public List<InterestPaymentDto> findByLoan(Long loanId) {
        Loan loan = loanService.findById(loanId)
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        return repository.findByLoan(loan).stream()
                .map(this::toDto)
                .toList();
    }

    public List<InterestPaymentDto> findByInterestRateGreaterThan(BigDecimal rate) {
        return repository.findByInterestRateGreaterThan(rate).stream()
                .map(this::toDto)
                .toList();
    }

    public List<InterestPaymentDto> findByPaymentAmountGreaterThan(BigDecimal amount) {
        return repository.findByPaymentAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    public List<InterestPaymentDto> findByInterestStartDateBetween(LocalDate startDate, LocalDate endDate) {
        return repository.findByInterestStartDateBetween(startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    public List<InterestPaymentDto> findByLoanAndInterestStartDateBetween(Long loanId, LocalDate startDate, LocalDate endDate) {
        Loan loan = loanService.findById(loanId)
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        return repository.findByLoanAndInterestStartDateBetween(loan, startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    // 利息支払いの実行
    @Transactional
    public InterestPaymentDto executeInterestPayment(Long interestPaymentId) {
        InterestPayment interestPayment = repository.findById(interestPaymentId)
                .orElseThrow(() -> new BusinessException("Interest payment not found", "INTEREST_PAYMENT_NOT_FOUND"));

        // 支払い済みの場合はエラー
        if ("EXECUTED".equals(interestPayment.getStatus())) {
            throw new BusinessException("Interest payment already executed", "INTEREST_PAYMENT_ALREADY_EXECUTED");
        }

        // ステータスを更新
        interestPayment.setStatus("EXECUTED");
        interestPayment.setProcessedDate(java.time.LocalDateTime.now());

        return toDto(repository.save(interestPayment));
    }

    // 利息金額の計算と更新（実行前のみ可能）
    @Transactional
    public InterestPaymentDto calculateAndUpdatePaymentAmount(Long interestPaymentId) {
        InterestPayment interestPayment = repository.findById(interestPaymentId)
                .orElseThrow(() -> new BusinessException("Interest payment not found", "INTEREST_PAYMENT_NOT_FOUND"));

        if ("EXECUTED".equals(interestPayment.getStatus())) {
            throw new BusinessException("Cannot update executed interest payment", "INTEREST_PAYMENT_ALREADY_EXECUTED");
        }

        // 利息期間の日数
        long daysInPeriod = ChronoUnit.DAYS.between(interestPayment.getInterestStartDate(), interestPayment.getInterestEndDate());

        // 利息計算（ACTUAL/360方式）
        BigDecimal newAmount = interestPayment.getLoan().getAmount()
                .multiply(interestPayment.getInterestRate())
                .multiply(BigDecimal.valueOf(daysInPeriod))
                .divide(BigDecimal.valueOf(360), 4, RoundingMode.HALF_UP);

        interestPayment.setPaymentAmount(newAmount);
        interestPayment.setAmount(newAmount); // 取引金額も更新

        return toDto(repository.save(interestPayment));
    }
}

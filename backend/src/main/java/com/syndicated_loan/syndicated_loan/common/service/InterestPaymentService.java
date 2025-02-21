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

/**
 * シンジケートローンにおける利息支払い（InterestPayment）を管理するサービスクラス。
 * 利息の計算（ACTUAL/360方式）、支払い処理、期間管理などの機能を提供します。
 * 利息期間の日数に基づいて正確な利息計算を行い、支払額を管理します。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class InterestPaymentService 
    extends TransactionService<InterestPayment, InterestPaymentDto, InterestPaymentRepository> {

    private final LoanService loanService;
    private final InvestorService investorService;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param amountPieService 金額配分サービス
     * @param positionService ポジションサービス
     * @param investorService 投資家サービス
     * @param loanService ローンサービス
     */
    public InterestPaymentService(
            InterestPaymentRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            InvestorService investorService,
            LoanService loanService) {
        super(repository, amountPieService, positionService, investorService);
        this.loanService = loanService;
        this.investorService = investorService;
    }

    /**
     * DTOをエンティティに変換します。
     * 関連するローンの検証、利息期間の設定などを行います。
     * 
     * @param dto 変換元のDTO
     * @return 変換された利息支払いエンティティ
     * @throws BusinessException ローンが存在しない場合（LOAN_NOT_FOUND）
     */
    @Override
    public InterestPayment toEntity(InterestPaymentDto dto) {
        InterestPayment entity = new InterestPayment();
        entity.setId(dto.getId());
        entity.setType("INTEREST_PAYMENT");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getAmount());

        Loan loan = loanService.findById(dto.getLoanId())
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        entity.setLoan(loan);

        setBaseProperties(entity, dto);
        entity.setInterestRate(dto.getInterestRate());
        entity.setPaymentAmount(dto.getPaymentAmount());
        entity.setInterestStartDate(dto.getInterestStartDate());
        entity.setInterestEndDate(dto.getInterestEndDate());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    /**
     * エンティティをDTOに変換します。
     * 利息期間の日数計算、計算方式の設定などの追加情報も設定します。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換された利息支払いDTO
     */
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

        setBaseDtoProperties(dto, entity);
        dto.setLoan(loanService.toDto(entity.getLoan()));

        dto.setDaysInPeriod((int) ChronoUnit.DAYS.between(entity.getInterestStartDate(), entity.getInterestEndDate()));
        dto.setBaseAmount(entity.getLoan().getAmount());
        dto.setCalculationMethod("ACTUAL/360");

        return dto;
    }

    /**
     * 指定されたローンに関連する全ての利息支払いを検索します。
     * 
     * @param loanId ローンID
     * @return 利息支払いのDTOリスト
     * @throws BusinessException ローンが存在しない場合（LOAN_NOT_FOUND）
     */
    public List<InterestPaymentDto> findByLoan(Long loanId) {
        Loan loan = loanService.findById(loanId)
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        return repository.findByLoan(loan).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された利率より高い利息支払いを検索します。
     * 
     * @param rate 基準となる利率
     * @return 条件を満たす利息支払いのDTOリスト
     */
    public List<InterestPaymentDto> findByInterestRateGreaterThan(BigDecimal rate) {
        return repository.findByInterestRateGreaterThan(rate).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された金額より大きい利息支払いを検索します。
     * 
     * @param amount 基準となる金額
     * @return 条件を満たす利息支払いのDTOリスト
     */
    public List<InterestPaymentDto> findByPaymentAmountGreaterThan(BigDecimal amount) {
        return repository.findByPaymentAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された期間内に開始する利息支払いを検索します。
     * 
     * @param startDate 期間開始日
     * @param endDate 期間終了日
     * @return 条件を満たす利息支払いのDTOリスト
     */
    public List<InterestPaymentDto> findByInterestStartDateBetween(LocalDate startDate, LocalDate endDate) {
        return repository.findByInterestStartDateBetween(startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定されたローンの指定された期間内に開始する利息支払いを検索します。
     * 
     * @param loanId ローンID
     * @param startDate 期間開始日
     * @param endDate 期間終了日
     * @return 条件を満たす利息支払いのDTOリスト
     * @throws BusinessException ローンが存在しない場合（LOAN_NOT_FOUND）
     */
    public List<InterestPaymentDto> findByLoanAndInterestStartDateBetween(Long loanId, LocalDate startDate, LocalDate endDate) {
        Loan loan = loanService.findById(loanId)
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        return repository.findByLoanAndInterestStartDateBetween(loan, startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 利息支払いを実行します。
     * 
     * @param interestPaymentId 利息支払いID
     * @return 実行された利息支払いのDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - 利息支払いが存在しない場合（INTEREST_PAYMENT_NOT_FOUND）
     *                          - すでに実行済みの場合（INTEREST_PAYMENT_ALREADY_EXECUTED）
     */
    @Transactional
    public InterestPaymentDto executeInterestPayment(Long interestPaymentId) {
        InterestPayment interestPayment = repository.findById(interestPaymentId)
                .orElseThrow(() -> new BusinessException("Interest payment not found", "INTEREST_PAYMENT_NOT_FOUND"));

        if ("EXECUTED".equals(interestPayment.getStatus())) {
            throw new BusinessException("Interest payment already executed", "INTEREST_PAYMENT_ALREADY_EXECUTED");
        }

        interestPayment.setStatus("EXECUTED");
        interestPayment.setProcessedDate(java.time.LocalDateTime.now());

        return toDto(repository.save(interestPayment));
    }

    /**
     * 利息支払い金額を計算し更新します。
     * ACTUAL/360方式で利息を計算します（実際の日数 / 360日）。
     * 実行済みの支払いは更新できません。
     * 
     * @param interestPaymentId 利息支払いID
     * @return 更新された利息支払いのDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - 利息支払いが存在しない場合（INTEREST_PAYMENT_NOT_FOUND）
     *                          - すでに実行済みの場合（INTEREST_PAYMENT_ALREADY_EXECUTED）
     */
    @Transactional
    public InterestPaymentDto calculateAndUpdatePaymentAmount(Long interestPaymentId) {
        InterestPayment interestPayment = repository.findById(interestPaymentId)
                .orElseThrow(() -> new BusinessException("Interest payment not found", "INTEREST_PAYMENT_NOT_FOUND"));

        if ("EXECUTED".equals(interestPayment.getStatus())) {
            throw new BusinessException("Cannot update executed interest payment", "INTEREST_PAYMENT_ALREADY_EXECUTED");
        }

        long daysInPeriod = ChronoUnit.DAYS.between(interestPayment.getInterestStartDate(), interestPayment.getInterestEndDate());

        BigDecimal newAmount = interestPayment.getLoan().getAmount()
                .multiply(interestPayment.getInterestRate())
                .multiply(BigDecimal.valueOf(daysInPeriod))
                .divide(BigDecimal.valueOf(360), 4, RoundingMode.HALF_UP);

        interestPayment.setPaymentAmount(newAmount);
        interestPayment.setAmount(newAmount);

        return toDto(repository.save(interestPayment));
    }
}

package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.PrincipalPaymentDto;
import com.syndicated_loan.syndicated_loan.common.dto.AmountPieDto;
import com.syndicated_loan.syndicated_loan.common.entity.PrincipalPayment;
import com.syndicated_loan.syndicated_loan.common.entity.Loan;
import com.syndicated_loan.syndicated_loan.common.repository.PrincipalPaymentRepository;
import com.syndicated_loan.syndicated_loan.common.repository.LoanRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * シンジケートローンにおける元本返済（PrincipalPayment）を管理するサービスクラス。
 * ローンの元本返済処理、返済額の検証、残高管理、返済率の計算などの機能を提供します。
 * 返済による投資家の投資額への影響も管理します。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class PrincipalPaymentService
        extends TransactionService<PrincipalPayment, PrincipalPaymentDto, PrincipalPaymentRepository> {

    private final LoanService loanService;
    private final LoanRepository loanRepository;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param amountPieService 金額配分サービス
     * @param positionService ポジションサービス
     * @param loanService ローンサービス
     * @param loanRepository ローンリポジトリ
     * @param investorService 投資家サービス
     */
    public PrincipalPaymentService(
            PrincipalPaymentRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            LoanService loanService,
            LoanRepository loanRepository,
            InvestorService investorService) {
        super(repository, amountPieService, positionService, investorService);
        this.loanService = loanService;
        this.loanRepository = loanRepository;
    }

    /**
     * DTOをエンティティに変換します。
     * 関連するローンの検証も行います。
     * 
     * @param dto 変換元のDTO
     * @return 変換された元本返済エンティティ
     * @throws BusinessException ローンが存在しない場合（LOAN_NOT_FOUND）
     */
    @Override
    public PrincipalPayment toEntity(PrincipalPaymentDto dto) {
        PrincipalPayment entity = new PrincipalPayment();
        entity.setId(dto.getId());
        entity.setType("PRINCIPAL_PAYMENT");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getAmount());

        Loan loan = loanService.findById(dto.getLoanId())
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        entity.setLoan(loan);

        setBaseProperties(entity, dto);
        entity.setPaymentAmount(dto.getPaymentAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    /**
     * エンティティをDTOに変換します。
     * 返済率の計算や残高情報も設定します。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換された元本返済DTO（返済率と残高情報を含む）
     */
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

        setBaseDtoProperties(dto, entity);
        dto.setLoan(loanService.toDto(entity.getLoan()));

        if (entity.getLoan().getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal repaymentRate = entity.getPaymentAmount()
                    .divide(entity.getLoan().getTotalAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            dto.setRepaymentRate(repaymentRate);
        }

        List<PrincipalPayment> previousPayments = repository.findByLoanOrderByDateAsc(entity.getLoan());
        BigDecimal totalPaid = previousPayments.stream()
                .filter(payment -> payment.getDate().isBefore(entity.getDate()) ||
                        payment.getDate().equals(entity.getDate()))
                .map(PrincipalPayment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setRemainingBalance(entity.getLoan().getTotalAmount().subtract(totalPaid));

        return dto;
    }

    /**
     * 指定されたローンの元本返済を検索します。
     * 
     * @param loanId ローンID
     * @return 元本返済のDTOリスト
     * @throws BusinessException ローンが存在しない場合（LOAN_NOT_FOUND）
     */
    public List<PrincipalPaymentDto> findByLoan(Long loanId) {
        Loan loan = loanService.findById(loanId)
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        return repository.findByLoan(loan).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された金額より大きい返済を検索します。
     * 
     * @param amount 基準となる金額
     * @return 条件を満たす元本返済のDTOリスト
     */
    public List<PrincipalPaymentDto> findByPaymentAmountGreaterThan(BigDecimal amount) {
        return repository.findByPaymentAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された期間内の返済を検索します。
     * 
     * @param startDate 期間開始日時
     * @param endDate 期間終了日時
     * @return 条件を満たす元本返済のDTOリスト
     */
    public List<PrincipalPaymentDto> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByDateBetween(startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定されたローンの指定された期間内の返済を検索します。
     * 
     * @param loanId ローンID
     * @param startDate 期間開始日時
     * @param endDate 期間終了日時
     * @return 条件を満たす元本返済のDTOリスト
     * @throws BusinessException ローンが存在しない場合（LOAN_NOT_FOUND）
     */
    public List<PrincipalPaymentDto> findByLoanAndDateBetween(Long loanId, LocalDateTime startDate,
            LocalDateTime endDate) {
        Loan loan = loanService.findById(loanId)
                .map(loanService::toEntity)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        return repository.findByLoanAndDateBetween(loan, startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 元本返済を実行し、ローン残高を更新します。
     * 投資家の現在の投資額も減額します。
     * 
     * @param principalPaymentId 元本返済ID
     * @return 実行された元本返済のDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - 元本返済が存在しない場合（PRINCIPAL_PAYMENT_NOT_FOUND）
     *                          - すでに実行済みの場合（PRINCIPAL_PAYMENT_ALREADY_EXECUTED）
     *                          - 返済額が残高を超える場合（PAYMENT_EXCEEDS_BALANCE）
     */
    @Transactional
    public PrincipalPaymentDto executePrincipalPayment(Long principalPaymentId) {
        PrincipalPayment principalPayment = repository.findById(principalPaymentId)
                .orElseThrow(() -> new BusinessException("Principal payment not found", "PRINCIPAL_PAYMENT_NOT_FOUND"));

        if ("EXECUTED".equals(principalPayment.getStatus())) {
            throw new BusinessException("Principal payment already executed", "PRINCIPAL_PAYMENT_ALREADY_EXECUTED");
        }

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

        principalPayment.setStatus("EXECUTED");
        principalPayment.setProcessedDate(java.time.LocalDateTime.now());

        loan.setAmount(remainingBalance.subtract(principalPayment.getPaymentAmount()));
        loanRepository.save(loan);

        AmountPieDto amountPieDto = amountPieService.toDto(principalPayment.getAmountPie());
        updateInvestorCurrentInvestments(amountPieDto, BigDecimal.valueOf(-1));

        return toDto(repository.save(principalPayment));
    }

    /**
     * 元本返済金額を更新します。
     * 実行済みの返済は更新できず、返済額は残高を超えることはできません。
     * 
     * @param principalPaymentId 元本返済ID
     * @param newAmount 新しい返済金額
     * @return 更新された元本返済のDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - 元本返済が存在しない場合（PRINCIPAL_PAYMENT_NOT_FOUND）
     *                          - すでに実行済みの場合（PRINCIPAL_PAYMENT_ALREADY_EXECUTED）
     *                          - 金額が0以下の場合（INVALID_PAYMENT_AMOUNT）
     *                          - 返済額が残高を超える場合（PAYMENT_EXCEEDS_BALANCE）
     */
    @Transactional
    public PrincipalPaymentDto updatePaymentAmount(Long principalPaymentId, BigDecimal newAmount) {
        PrincipalPayment principalPayment = repository.findById(principalPaymentId)
                .orElseThrow(() -> new BusinessException("Principal payment not found", "PRINCIPAL_PAYMENT_NOT_FOUND"));

        if ("EXECUTED".equals(principalPayment.getStatus())) {
            throw new BusinessException("Cannot update executed principal payment",
                    "PRINCIPAL_PAYMENT_ALREADY_EXECUTED");
        }

        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be positive", "INVALID_PAYMENT_AMOUNT");
        }

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
        principalPayment.setAmount(newAmount);

        return toDto(repository.save(principalPayment));
    }
}

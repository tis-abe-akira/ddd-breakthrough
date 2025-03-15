package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.InterestPaymentDto;
import com.syndicated_loan.syndicated_loan.common.dto.AmountPieDto;
import com.syndicated_loan.syndicated_loan.common.entity.InterestPayment;
import com.syndicated_loan.syndicated_loan.common.entity.Loan;
import com.syndicated_loan.syndicated_loan.common.entity.AmountPie;
import com.syndicated_loan.syndicated_loan.common.entity.RepaymentSchedule;
import com.syndicated_loan.syndicated_loan.common.entity.Drawdown;
import com.syndicated_loan.syndicated_loan.common.repository.InterestPaymentRepository;
import com.syndicated_loan.syndicated_loan.common.repository.RepaymentScheduleRepository;
import com.syndicated_loan.syndicated_loan.common.repository.DrawdownRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@Transactional(readOnly = true)
public class InterestPaymentService 
    extends TransactionService<InterestPayment, InterestPaymentDto, InterestPaymentRepository> {

    private final LoanService loanService;
    private final InvestorService investorService;
    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final DrawdownRepository drawdownRepository;

    public InterestPaymentService(
            InterestPaymentRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            InvestorService investorService,
            LoanService loanService,
            RepaymentScheduleRepository repaymentScheduleRepository,
            DrawdownRepository drawdownRepository) {
        super(repository, amountPieService, positionService, investorService);
        this.loanService = loanService;
        this.investorService = investorService;
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.drawdownRepository = drawdownRepository;
    }

    // 返済スケジュールから利息支払い情報を取得
    private RepaymentSchedule findInterestSchedule(Long loanId, LocalDate date) {
        return repaymentScheduleRepository.findByScheduledDateAndStatus(date, RepaymentSchedule.PaymentStatus.SCHEDULED)
            .stream()
            .filter(schedule -> schedule.getLoan().getId().equals(loanId))
            .filter(schedule -> schedule.getPaymentType() == RepaymentSchedule.PaymentType.INTEREST)
            .findFirst()
            .orElseThrow(() -> new BusinessException("Interest schedule not found", "INTEREST_SCHEDULE_NOT_FOUND"));
    }

    // ドローダウンのAmountPieを取得
    private AmountPie getDrawdownAmountPie(Loan loan) {
        List<Drawdown> drawdowns = drawdownRepository.findByRelatedPosition(loan);
        if (drawdowns.isEmpty()) {
            throw new BusinessException("Drawdown not found for loan", "DRAWDOWN_NOT_FOUND");
        }
        return drawdowns.get(0).getAmountPie();
    }

    // 利息の配分計算
    private Map<Long, BigDecimal> calculateInterestDistribution(
        BigDecimal totalInterest,
        AmountPie drawdownAmountPie
    ) {
        Map<Long, BigDecimal> distribution = new HashMap<>();
        BigDecimal totalAmount = drawdownAmountPie.getAmounts().values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        drawdownAmountPie.getAmounts().forEach((investorId, amount) -> {
            // 各投資家の配分比率に基づいて利息を計算
            BigDecimal ratio = amount.divide(totalAmount, 10, RoundingMode.HALF_UP);
            BigDecimal interestShare = totalInterest.multiply(ratio)
                .setScale(4, RoundingMode.HALF_UP);
            distribution.put(investorId, interestShare);
        });

        return distribution;
    }

    @Override
    public InterestPayment toEntity(InterestPaymentDto dto) {
        InterestPayment entity = new InterestPayment();
        entity.setId(dto.getId());
        entity.setType("INTEREST_PAYMENT");
        entity.setDate(dto.getDate());

        // ローンの設定（関連する Position としても設定）
        Loan loan = loanService.findById(dto.getLoanId())
            .map(loanService::toEntity)
            .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));
        entity.setLoan(loan);
        entity.setRelatedPosition(loan);  // ここを追加

        // 返済スケジュールから利息情報を取得
        RepaymentSchedule schedule = findInterestSchedule(dto.getLoanId(), 
            dto.getDate().toLocalDate());
        
        // スケジュールの金額を設定
        entity.setAmount(schedule.getInterestAmount());
        entity.setPaymentAmount(schedule.getInterestAmount());
        
        // 期間の設定
        entity.setInterestStartDate(schedule.getScheduledDate());
        entity.setInterestEndDate(schedule.getScheduledDate());
        
        // ローンの金利を設定
        entity.setInterestRate(loan.getInterestRate());

        // ドローダウンのAmountPieから配分を計算
        AmountPie drawdownAmountPie = getDrawdownAmountPie(loan);
        Map<Long, BigDecimal> distribution = calculateInterestDistribution(
            schedule.getInterestAmount(), 
            drawdownAmountPie
        );

        // 新しいAmountPieを作成
        AmountPieDto amountPieDto = new AmountPieDto();
        amountPieDto.setAmounts(distribution);
        AmountPieDto savedAmountPie = amountPieService.create(amountPieDto);

        // AmountPieを設定
        entity.setAmountPie(amountPieService.toEntity(savedAmountPie));

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

    // 配分結果の取得
    public Map<String, Object> getDistributionResult(Long interestPaymentId) {
        InterestPayment interestPayment = repository.findById(interestPaymentId)
                .orElseThrow(() -> new BusinessException("Interest payment not found", "INTEREST_PAYMENT_NOT_FOUND"));

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", interestPayment.getAmount());
        result.put("paymentDate", interestPayment.getDate());
        result.put("status", interestPayment.getStatus());

        if (interestPayment.getAmountPie() != null) {
            Map<Long, BigDecimal> distribution = interestPayment.getAmountPie().getAmounts();
            result.put("distribution", distribution);
        }

        return result;
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

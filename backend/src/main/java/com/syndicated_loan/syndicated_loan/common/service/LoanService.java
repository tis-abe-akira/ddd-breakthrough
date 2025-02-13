package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.LoanDto;
import com.syndicated_loan.syndicated_loan.common.dto.SharePieDto;
import com.syndicated_loan.syndicated_loan.common.entity.Loan;
import com.syndicated_loan.syndicated_loan.common.entity.Borrower;
import com.syndicated_loan.syndicated_loan.common.entity.Facility;
import com.syndicated_loan.syndicated_loan.common.entity.SharePie;
import com.syndicated_loan.syndicated_loan.common.entity.RepaymentSchedule;
import com.syndicated_loan.syndicated_loan.common.repository.LoanRepository;
import com.syndicated_loan.syndicated_loan.common.repository.RepaymentScheduleRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.time.temporal.ChronoUnit;
import java.math.RoundingMode;

@Slf4j
@Service
@Transactional
public class LoanService extends AbstractBaseService<Loan, Long, LoanDto, LoanRepository> {

    private final BorrowerService borrowerService;
    private final FacilityService facilityService;
    private final SharePieService sharePieService;
    private final RepaymentScheduleRepository repaymentScheduleRepository;

    public LoanService(
            LoanRepository repository,
            BorrowerService borrowerService,
            FacilityService facilityService,
            SharePieService sharePieService,
            RepaymentScheduleRepository repaymentScheduleRepository) {
        super(repository);
        this.borrowerService = borrowerService;
        this.facilityService = facilityService;
        this.sharePieService = sharePieService;
        this.repaymentScheduleRepository = repaymentScheduleRepository;
    }

    @Override
    protected void setEntityId(Loan entity, Long id) {
        entity.setId(id);
    }

    @Override
    public Loan toEntity(LoanDto dto) {
        Loan entity = new Loan();
        entity.setId(dto.getId());
        entity.setType("LOAN");
        entity.setAmount(dto.getAmount());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setTerm(dto.getTerm());  // termも設定！
        entity.setInterestRate(dto.getInterestRate());

        // 借り手の設定
        Borrower borrower = borrowerService.findById(dto.getBorrowerId())
                .map(borrowerService::toEntity)
                .orElseThrow(() -> new BusinessException("Borrower not found", "BORROWER_NOT_FOUND"));
        entity.setBorrower(borrower);

        // ファシリティの設定
        if (dto.getFacilityId() != null) {
            Facility facility = facilityService.findById(dto.getFacilityId())
                    .map(facilityService::toEntity)
                    .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
            entity.setFacility(facility);
        }

        // シェアパイの設定を修正
        if (dto.getSharePieId() != null) {
            SharePie sharePie = sharePieService.findById(dto.getSharePieId())
                    .map(sharePieService::toEntity)
                    .orElseThrow(() -> new BusinessException("SharePie not found", "SHARE_PIE_NOT_FOUND"));
            entity.setSharePie(sharePie);
        }

        // availableAmountの設定を追加（初期値は設定時の金額と同じ）
        entity.setAvailableAmount(dto.getTotalAmount());

        entity.setVersion(dto.getVersion());
        return entity;
    }

    @Override
    public LoanDto toDto(Loan entity) {
        LoanDto dto = LoanDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .totalAmount(entity.getTotalAmount())
                .borrowerId(entity.getBorrower().getId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .term(entity.getTerm())  // termを追加！
                .interestRate(entity.getInterestRate())
                .facilityId(entity.getFacility() != null ? entity.getFacility().getId() : null)
                .sharePieId(entity.getSharePie() != null ? entity.getSharePie().getId() : null)
                .version(entity.getVersion())
                .build();

        // レスポンス用の追加情報
        dto.setBorrower(borrowerService.toDto(entity.getBorrower()));
        if (entity.getFacility() != null) {
            dto.setFacility(facilityService.toDto(entity.getFacility()));
        }
        if (entity.getSharePie() != null) {
            dto.setSharePie(sharePieService.toDto(entity.getSharePie()));
        }

        // TODO: 残額、返済済み金額、次回支払日、次回支払額の計算
        // これらの値は取引履歴から計算する必要があり、TransactionServiceが必要

        return dto;
    }

    // 追加の検索メソッド
    public List<LoanDto> findByBorrower(Long borrowerId) {
        Borrower borrower = borrowerService.findById(borrowerId)
                .map(borrowerService::toEntity)
                .orElseThrow(() -> new BusinessException("Borrower not found", "BORROWER_NOT_FOUND"));
        return repository.findByBorrower(borrower).stream()
                .map(this::toDto)
                .toList();
    }

    public List<LoanDto> findByFacility(Long facilityId) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByFacility(facility).stream()
                .map(this::toDto)
                .toList();
    }

    public List<LoanDto> findByTotalAmountGreaterThan(BigDecimal amount) {
        return repository.findByTotalAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    public List<LoanDto> findByStartDateBetween(LocalDate startDate, LocalDate endDate) {
        return repository.findByStartDateBetween(startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    public List<LoanDto> findByEndDateAfter(LocalDate date) {
        return repository.findByEndDateAfter(date).stream()
                .map(this::toDto)
                .toList();
    }

    // シェアパイの更新
    @Transactional
    public LoanDto updateSharePie(Long loanId, SharePieDto sharePieDto) {
        Loan loan = repository.findById(loanId)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));

        SharePie sharePie = sharePieService.toEntity(sharePieDto);
        loan.setSharePie(sharePie);

        return toDto(repository.save(loan));
    }

    public void generateRepaymentSchedules(Loan loan) {
        // 元本一括返済の場合
        RepaymentSchedule principalSchedule = new RepaymentSchedule();
        principalSchedule.setLoan(loan);
        principalSchedule.setScheduledDate(loan.getEndDate());
        principalSchedule.setPrincipalAmount(loan.getTotalAmount());
        principalSchedule.setPaymentType(RepaymentSchedule.PaymentType.PRINCIPAL);
        loan.addRepaymentSchedule(principalSchedule);

        // 利息は3ヶ月ごとに支払い
        LocalDate currentDate = loan.getStartDate();
        while (currentDate.isBefore(loan.getEndDate())) {
            LocalDate nextPaymentDate = currentDate.plusMonths(3);
            if (nextPaymentDate.isAfter(loan.getEndDate())) {
                nextPaymentDate = loan.getEndDate();
            }

            RepaymentSchedule interestSchedule = new RepaymentSchedule();
            interestSchedule.setLoan(loan);
            interestSchedule.setScheduledDate(nextPaymentDate);
            // 年利を日割り計算して3ヶ月分の利息を計算
            BigDecimal daysInPeriod = BigDecimal.valueOf(ChronoUnit.DAYS.between(currentDate, nextPaymentDate));
            BigDecimal yearlyInterest = loan.getTotalAmount()
                .multiply(loan.getInterestRate())
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            BigDecimal periodInterest = yearlyInterest
                .multiply(daysInPeriod)
                .divide(BigDecimal.valueOf(365), 4, RoundingMode.HALF_UP);
            
            interestSchedule.setInterestAmount(periodInterest);
            interestSchedule.setPaymentType(RepaymentSchedule.PaymentType.INTEREST);
            loan.addRepaymentSchedule(interestSchedule);

            currentDate = nextPaymentDate;
        }

        repository.save(loan);
    }
}

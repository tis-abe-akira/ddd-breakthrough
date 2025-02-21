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

/**
 * シンジケートローンにおけるローン（Loan）を管理するサービスクラス。
 * ローンの基本情報管理、返済スケジュールの生成、シェア配分の設定などの機能を提供します。
 * 元本一括返済と3ヶ月ごとの利息支払いスケジュールを自動生成します。
 */
@Slf4j
@Service
@Transactional
public class LoanService extends AbstractBaseService<Loan, Long, LoanDto, LoanRepository> {

    private final BorrowerService borrowerService;
    private final FacilityService facilityService;
    private final SharePieService sharePieService;
    private final RepaymentScheduleRepository repaymentScheduleRepository;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param borrowerService 借入人サービス
     * @param facilityService ファシリティサービス
     * @param sharePieService シェア配分サービス
     * @param repaymentScheduleRepository 返済スケジュールリポジトリ
     */
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

    /**
     * DTOをエンティティに変換します。
     * 借入人、ファシリティ、シェア配分の検証と設定も行います。
     * 
     * @param dto 変換元のDTO
     * @return 変換されたローンエンティティ
     * @throws BusinessException 以下の場合に発生:
     *                          - 借入人が存在しない場合（BORROWER_NOT_FOUND）
     *                          - ファシリティが存在しない場合（FACILITY_NOT_FOUND）
     *                          - シェア配分が存在しない場合（SHARE_PIE_NOT_FOUND）
     */
    @Override
    public Loan toEntity(LoanDto dto) {
        Loan entity = new Loan();
        entity.setId(dto.getId());
        entity.setType("LOAN");
        entity.setAmount(dto.getAmount());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setTerm(dto.getTerm());
        entity.setInterestRate(dto.getInterestRate());

        Borrower borrower = borrowerService.findById(dto.getBorrowerId())
                .map(borrowerService::toEntity)
                .orElseThrow(() -> new BusinessException("Borrower not found", "BORROWER_NOT_FOUND"));
        entity.setBorrower(borrower);

        if (dto.getFacilityId() != null) {
            Facility facility = facilityService.findById(dto.getFacilityId())
                    .map(facilityService::toEntity)
                    .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
            entity.setFacility(facility);
        }

        if (dto.getSharePieId() != null) {
            SharePie sharePie = sharePieService.findById(dto.getSharePieId())
                    .map(sharePieService::toEntity)
                    .orElseThrow(() -> new BusinessException("SharePie not found", "SHARE_PIE_NOT_FOUND"));
            entity.setSharePie(sharePie);
        }

        entity.setAvailableAmount(dto.getTotalAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    /**
     * エンティティをDTOに変換します。
     * 関連する借入人、ファシリティ、シェア配分の情報も設定します。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換されたローンDTO
     */
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
                .term(entity.getTerm())
                .interestRate(entity.getInterestRate())
                .facilityId(entity.getFacility() != null ? entity.getFacility().getId() : null)
                .sharePieId(entity.getSharePie() != null ? entity.getSharePie().getId() : null)
                .version(entity.getVersion())
                .build();

        dto.setBorrower(borrowerService.toDto(entity.getBorrower()));
        if (entity.getFacility() != null) {
            dto.setFacility(facilityService.toDto(entity.getFacility()));
        }
        if (entity.getSharePie() != null) {
            dto.setSharePie(sharePieService.toDto(entity.getSharePie()));
        }

        return dto;
    }

    /**
     * 指定された借入人のローンを検索します。
     * 
     * @param borrowerId 借入人ID
     * @return ローンのDTOリスト
     * @throws BusinessException 借入人が存在しない場合（BORROWER_NOT_FOUND）
     */
    public List<LoanDto> findByBorrower(Long borrowerId) {
        Borrower borrower = borrowerService.findById(borrowerId)
                .map(borrowerService::toEntity)
                .orElseThrow(() -> new BusinessException("Borrower not found", "BORROWER_NOT_FOUND"));
        return repository.findByBorrower(borrower).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定されたファシリティのローンを検索します。
     * 
     * @param facilityId ファシリティID
     * @return ローンのDTOリスト
     * @throws BusinessException ファシリティが存在しない場合（FACILITY_NOT_FOUND）
     */
    public List<LoanDto> findByFacility(Long facilityId) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByFacility(facility).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された金額より大きい総額を持つローンを検索します。
     * 
     * @param amount 基準となる金額
     * @return 条件を満たすローンのDTOリスト
     */
    public List<LoanDto> findByTotalAmountGreaterThan(BigDecimal amount) {
        return repository.findByTotalAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された期間内に開始するローンを検索します。
     * 
     * @param startDate 期間開始日
     * @param endDate 期間終了日
     * @return 条件を満たすローンのDTOリスト
     */
    public List<LoanDto> findByStartDateBetween(LocalDate startDate, LocalDate endDate) {
        return repository.findByStartDateBetween(startDate, endDate).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された日付より後に終了するローンを検索します。
     * 
     * @param date 基準となる日付
     * @return 条件を満たすローンのDTOリスト
     */
    public List<LoanDto> findByEndDateAfter(LocalDate date) {
        return repository.findByEndDateAfter(date).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * ローンのシェア配分を更新します。
     * 
     * @param loanId ローンID
     * @param sharePieDto 新しいシェア配分DTO
     * @return 更新されたローンのDTO
     * @throws BusinessException ローンが存在しない場合（LOAN_NOT_FOUND）
     */
    @Transactional
    public LoanDto updateSharePie(Long loanId, SharePieDto sharePieDto) {
        Loan loan = repository.findById(loanId)
                .orElseThrow(() -> new BusinessException("Loan not found", "LOAN_NOT_FOUND"));

        SharePie sharePie = sharePieService.toEntity(sharePieDto);
        loan.setSharePie(sharePie);

        return toDto(repository.save(loan));
    }

    /**
     * 返済スケジュールを生成します。
     * 元本は満期一括返済、利息は3ヶ月ごとの支払いスケジュールを生成します。
     * 利息は実日数（Actual）/365日で計算します。
     * 
     * @param loan スケジュールを生成するローン
     */
    public void generateRepaymentSchedules(Loan loan) {
        RepaymentSchedule principalSchedule = new RepaymentSchedule();
        principalSchedule.setLoan(loan);
        principalSchedule.setScheduledDate(loan.getEndDate());
        principalSchedule.setPrincipalAmount(loan.getTotalAmount());
        principalSchedule.setPaymentType(RepaymentSchedule.PaymentType.PRINCIPAL);
        loan.addRepaymentSchedule(principalSchedule);

        LocalDate currentDate = loan.getStartDate();
        while (currentDate.isBefore(loan.getEndDate())) {
            LocalDate nextPaymentDate = currentDate.plusMonths(3);
            if (nextPaymentDate.isAfter(loan.getEndDate())) {
                nextPaymentDate = loan.getEndDate();
            }

            RepaymentSchedule interestSchedule = new RepaymentSchedule();
            interestSchedule.setLoan(loan);
            interestSchedule.setScheduledDate(nextPaymentDate);

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

package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.DrawdownDto;
import com.syndicated_loan.syndicated_loan.common.dto.LoanDto;
import com.syndicated_loan.syndicated_loan.common.dto.AmountPieDto;
import com.syndicated_loan.syndicated_loan.common.entity.AmountPie;
import com.syndicated_loan.syndicated_loan.common.entity.Drawdown;
import com.syndicated_loan.syndicated_loan.common.entity.Facility;
import com.syndicated_loan.syndicated_loan.common.entity.Loan;
import com.syndicated_loan.syndicated_loan.common.repository.DrawdownRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

/**
 * シンジケートローンにおけるドローダウン（融資実行）を管理するサービスクラス。
 * ドローダウンの作成、実行、金額更新、および関連する融資（Loan）の生成を担当します。
 * ファシリティの利用可能額の管理や投資家への配分も処理します。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class DrawdownService
        extends TransactionService<Drawdown, DrawdownDto, DrawdownRepository> {

    private final FacilityService facilityService;
    private final LoanService loanService;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param amountPieService 金額配分サービス
     * @param positionService ポジションサービス
     * @param facilityService ファシリティサービス
     * @param loanService ローンサービス
     * @param investorService 投資家サービス
     */
    public DrawdownService(
            DrawdownRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            FacilityService facilityService,
            LoanService loanService,
            InvestorService investorService) {
        super(repository, amountPieService, positionService, investorService);
        this.facilityService = facilityService;
        this.loanService = loanService;
    }

    /**
     * DTOをエンティティに変換します。
     * ドローダウンの基本情報設定に加えて、関連するファシリティの検証、
     * 新規ローンの作成と永続化も行います。
     * 
     * @param dto 変換元のDTO
     * @return 変換されたドローダウンエンティティ
     * @throws BusinessException ファシリティが存在しない場合
     */
    @Override
    public Drawdown toEntity(DrawdownDto dto) {
        Drawdown entity = new Drawdown();
        entity.setId(dto.getId());
        entity.setType("DRAWDOWN");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getDrawdownAmount());
    
        Facility facility = facilityService.findById(dto.getRelatedFacilityId())
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        entity.setRelatedFacility(facility);
    
        LoanDto loanDto = LoanDto.builder()
            .amount(dto.getDrawdownAmount())
            .totalAmount(dto.getDrawdownAmount())
            .borrowerId(facility.getBorrower().getId())
            .facilityId(facility.getId())
            .startDate(LocalDate.now())
            .endDate(facility.getEndDate())
            .term((int) ChronoUnit.MONTHS.between(LocalDate.now(), facility.getEndDate()))
            .interestRate(facility.getInterestRate())
            .build();
    
        LoanDto savedLoan = loanService.create(loanDto);
        entity.setRelatedPosition(loanService.toEntity(savedLoan));
    
        if (dto.getAmountPieId() != null) {
            AmountPie amountPie = amountPieService.findById(dto.getAmountPieId())
                .map(amountPieService::toEntity)
                .orElseThrow(() -> new BusinessException("AmountPie not found", "AMOUNT_PIE_NOT_FOUND"));
            entity.setAmountPie(amountPie);
        }
    
        entity.setDrawdownAmount(dto.getDrawdownAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    /**
     * エンティティをDTOに変換します。
     * ドローダウンの基本情報に加えて、ファシリティの残額や利用率などの
     * 計算された情報も設定します。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換されたドローダウンDTO
     */
    @Override
    public DrawdownDto toDto(Drawdown entity) {
        DrawdownDto dto = DrawdownDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .date(entity.getDate())
                .processedDate(entity.getProcessedDate())
                .amount(entity.getAmount())
                .relatedPositionId(entity.getRelatedPosition().getId())
                .amountPieId(entity.getAmountPie() != null ? entity.getAmountPie().getId() : null)
                .relatedFacilityId(entity.getRelatedFacility().getId())
                .drawdownAmount(entity.getDrawdownAmount())
                .version(entity.getVersion())
                .build();

        setBaseDtoProperties(dto, entity);
        dto.setRelatedFacility(facilityService.toDto(entity.getRelatedFacility()));

        if (entity.getAmountPie() != null) {
            dto.setAmountPie(amountPieService.toDto(entity.getAmountPie()));
        }

        Facility facility = entity.getRelatedFacility();
        dto.setRemainingFacilityAmount(facility.getAvailableAmount());
        if (facility.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilizationRate = BigDecimal.ONE
                    .subtract(facility.getAvailableAmount()
                            .divide(facility.getTotalAmount(), 4, RoundingMode.HALF_UP))
                    .multiply(new BigDecimal("100"));
            dto.setUtilizationRate(utilizationRate);
        }

        return dto;
    }

    /**
     * 指定されたファシリティに関連するドローダウンを検索します。
     * 
     * @param facilityId ファシリティID
     * @return ドローダウンのDTOリスト
     * @throws BusinessException ファシリティが存在しない場合
     */
    public List<DrawdownDto> findByRelatedFacility(Long facilityId) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByRelatedFacility(facility).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された金額より大きいドローダウンを検索します。
     * 
     * @param amount 基準となる金額
     * @return 条件を満たすドローダウンのDTOリスト
     */
    public List<DrawdownDto> findByDrawdownAmountGreaterThan(BigDecimal amount) {
        return repository.findByDrawdownAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定されたファシリティに関連し、かつ指定された金額より大きいドローダウンを検索します。
     * 
     * @param facilityId ファシリティID
     * @param amount 基準となる金額
     * @return 条件を満たすドローダウンのDTOリスト
     * @throws BusinessException ファシリティが存在しない場合
     */
    public List<DrawdownDto> findByRelatedFacilityAndDrawdownAmountGreaterThan(Long facilityId, BigDecimal amount) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByRelatedFacilityAndDrawdownAmountGreaterThan(facility, amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * ドローダウンを実行します。
     * ファシリティの利用可能額を更新し、投資家の現在の投資額も更新します。
     * 
     * @param drawdownId ドローダウンID
     * @return 更新されたドローダウンのDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - ドローダウンが存在しない場合（DRAWDOWN_NOT_FOUND）
     *                          - 利用可能額が不足している場合（INSUFFICIENT_AVAILABLE_AMOUNT）
     */
    @Transactional
    public DrawdownDto executeDrawdown(Long drawdownId) {
        Drawdown drawdown = repository.findById(drawdownId)
                .orElseThrow(() -> new BusinessException("Drawdown not found", "DRAWDOWN_NOT_FOUND"));

        Facility facility = drawdown.getRelatedFacility();
        BigDecimal newAvailableAmount = facility.getAvailableAmount().subtract(drawdown.getDrawdownAmount());

        if (newAvailableAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Insufficient available amount", "INSUFFICIENT_AVAILABLE_AMOUNT");
        }

        facilityService.updateAvailableAmount(facility.getId(), newAvailableAmount);

        AmountPieDto amountPieDto = amountPieService.toDto(drawdown.getAmountPie());
        updateInvestorCurrentInvestments(amountPieDto, BigDecimal.ONE);

        drawdown.setStatus("EXECUTED");
        drawdown.setProcessedDate(java.time.LocalDateTime.now());

        return toDto(repository.save(drawdown));
    }

    /**
     * ドローダウン金額を更新します。
     * 実行済みのドローダウンは更新できません。
     * 
     * @param drawdownId ドローダウンID
     * @param newAmount 新しいドローダウン金額
     * @return 更新されたドローダウンのDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - ドローダウンが存在しない場合（DRAWDOWN_NOT_FOUND）
     *                          - すでに実行済みの場合（DRAWDOWN_ALREADY_EXECUTED）
     *                          - 金額が0以下の場合（INVALID_DRAWDOWN_AMOUNT）
     *                          - 利用可能額が不足している場合（INSUFFICIENT_AVAILABLE_AMOUNT）
     */
    @Transactional
    public DrawdownDto updateDrawdownAmount(Long drawdownId, BigDecimal newAmount) {
        Drawdown drawdown = repository.findById(drawdownId)
                .orElseThrow(() -> new BusinessException("Drawdown not found", "DRAWDOWN_NOT_FOUND"));

        if ("EXECUTED".equals(drawdown.getStatus())) {
            throw new BusinessException("Cannot update executed drawdown", "DRAWDOWN_ALREADY_EXECUTED");
        }

        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Drawdown amount must be positive", "INVALID_DRAWDOWN_AMOUNT");
        }

        Facility facility = drawdown.getRelatedFacility();
        if (newAmount.compareTo(facility.getAvailableAmount()) > 0) {
            throw new BusinessException("Drawdown amount exceeds available amount", "INSUFFICIENT_AVAILABLE_AMOUNT");
        }

        drawdown.setDrawdownAmount(newAmount);
        drawdown.setAmount(newAmount);

        return toDto(repository.save(drawdown));
    }

    /**
     * ドローダウンを作成し、関連する返済スケジュールを生成します。
     * 
     * @param dto 作成するドローダウンのDTO
     * @return 作成されたドローダウンのDTO
     * @throws BusinessException AmountPieの作成に失敗した場合
     */
    @Override
    @Transactional
    public DrawdownDto create(DrawdownDto dto) {
        if (dto.getAmountPie() != null) {
            var amountPie = amountPieService.create(dto.getAmountPie());
            dto.setAmountPieId(amountPie.getId());
        }

        DrawdownDto createdDto = super.create(dto);

        Drawdown drawdown = repository.findById(createdDto.getId())
            .orElseThrow(() -> new BusinessException("Drawdown not found", "DRAWDOWN_NOT_FOUND"));
        loanService.generateRepaymentSchedules((Loan)drawdown.getRelatedPosition());

        return createdDto;
    }

    /**
     * ドローダウンを更新し、必要に応じてAmountPieも更新します。
     * 
     * @param id 更新対象のドローダウンID
     * @param dto 更新内容を含むDTO
     * @return 更新されたドローダウンのDTO
     * @throws BusinessException ドローダウンが存在しない場合
     */
    @Override
    @Transactional
    public DrawdownDto update(Long id, DrawdownDto dto) {
        if (dto.getAmountPie() != null) {
            Drawdown existingDrawdown = repository.findById(id)
                    .orElseThrow(() -> new BusinessException("Drawdown not found", "DRAWDOWN_NOT_FOUND"));

            if (existingDrawdown.getAmountPie() != null) {
                var updatedAmountPie = amountPieService.update(
                        existingDrawdown.getAmountPie().getId(),
                        dto.getAmountPie());
                dto.setAmountPieId(updatedAmountPie.getId());
            } else {
                var newAmountPie = amountPieService.create(dto.getAmountPie());
                dto.setAmountPieId(newAmountPie.getId());
            }
        }

        return super.update(id, dto);
    }
}

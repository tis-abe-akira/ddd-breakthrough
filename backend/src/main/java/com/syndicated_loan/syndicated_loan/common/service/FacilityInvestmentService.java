package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.FacilityInvestmentDto;
import com.syndicated_loan.syndicated_loan.common.entity.FacilityInvestment;
import com.syndicated_loan.syndicated_loan.common.entity.Investor;
import com.syndicated_loan.syndicated_loan.common.repository.FacilityInvestmentRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * シンジケートローンにおけるファシリティへの投資（FacilityInvestment）を管理するサービスクラス。
 * 投資家のファシリティへの参加、投資額の管理、シェア計算などの機能を提供します。
 * SharePieを利用して投資金額を自動計算する機能も備えています。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class FacilityInvestmentService 
    extends TransactionService<FacilityInvestment, FacilityInvestmentDto, FacilityInvestmentRepository> {

    private final InvestorService investorService;
    private final FacilityService facilityService;
    private final SharePieService sharePieService;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param amountPieService 金額配分サービス
     * @param positionService ポジションサービス
     * @param investorService 投資家サービス
     * @param facilityService ファシリティサービス
     * @param sharePieService シェア配分サービス
     */
    public FacilityInvestmentService(
            FacilityInvestmentRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            InvestorService investorService,
            FacilityService facilityService,
            SharePieService sharePieService) {
        super(repository, amountPieService, positionService, investorService);
        this.investorService = investorService;
        this.facilityService = facilityService;
        this.sharePieService = sharePieService;
    }

    /**
     * DTOをエンティティに変換します。
     * SharePieを利用して投資金額を自動計算し、関連する投資家情報も設定します。
     * 
     * @param dto 変換元のDTO
     * @return 変換されたファシリティ投資エンティティ
     * @throws BusinessException 以下の場合に発生:
     *                          - 投資家が存在しない場合（INVESTOR_NOT_FOUND）
     *                          - 投資金額の計算に失敗した場合
     */
    @Override
    public FacilityInvestment toEntity(FacilityInvestmentDto dto) {
        System.out.println("***** FacilityInvestmentService.toEntity *****");
        FacilityInvestment entity = new FacilityInvestment();
        entity.setId(dto.getId());
        entity.setType("FACILITY_INVESTMENT");
        entity.setDate(dto.getDate());
        entity.setProcessedDate(dto.getProcessedDate());

        BigDecimal calculatedAmount = calculateInvestmentAmount(dto.getRelatedPositionId(), dto.getInvestorId());
        entity.setAmount(calculatedAmount);
        entity.setInvestmentAmount(calculatedAmount);

        Investor investor = investorService.findById(dto.getInvestorId())
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Investor not found", "INVESTOR_NOT_FOUND"));
        entity.setInvestor(investor);

        setBaseProperties(entity, dto);
        entity.setVersion(dto.getVersion());
        return entity;
    }

    /**
     * 投資金額を計算します。
     * ファシリティの総額と投資家のシェア比率から投資金額を算出します。
     * 
     * @param facilityId ファシリティID
     * @param investorId 投資家ID
     * @return 計算された投資金額
     * @throws BusinessException 以下の場合に発生:
     *                          - ファシリティが存在しない場合（FACILITY_NOT_FOUND）
     *                          - SharePieが設定されていない場合（SHARE_PIE_NOT_FOUND）
     */
    private BigDecimal calculateInvestmentAmount(Long facilityId, Long investorId) {
        var facility = facilityService.findById(facilityId)
            .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));

        if (facility.getSharePieId() == null) {
            throw new BusinessException("Facility has no SharePie", "SHARE_PIE_NOT_FOUND");
        }

        BigDecimal investorShare = sharePieService.getInvestorShare(facility.getSharePieId(), investorId);

        return facility.getTotalAmount()
            .multiply(investorShare)
            .divide(new BigDecimal("100"), 0, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * エンティティをDTOに変換します。
     * 投資シェアの計算や関連する投資家情報も設定します。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換されたファシリティ投資DTO
     */
    @Override
    public FacilityInvestmentDto toDto(FacilityInvestment entity) {
        FacilityInvestmentDto dto = FacilityInvestmentDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .date(entity.getDate())
                .processedDate(entity.getProcessedDate())
                .amount(entity.getAmount())
                .relatedPositionId(entity.getRelatedPosition().getId())
                .amountPieId(entity.getAmountPie() != null ? entity.getAmountPie().getId() : null)
                .investorId(entity.getInvestor().getId())
                .version(entity.getVersion())
                .build();

        setBaseDtoProperties(dto, entity);
        dto.setInvestor(investorService.toDto(entity.getInvestor()));

        if (entity.getRelatedPosition() != null && 
            entity.getRelatedPosition().getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal investmentShare = entity.getInvestmentAmount()
                    .divide(entity.getRelatedPosition().getAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            dto.setInvestmentShare(investmentShare);
        }

        return dto;
    }

    /**
     * 指定された投資家に関連する全ての投資を検索します。
     * 
     * @param investorId 投資家ID
     * @return 投資家の投資のDTOリスト
     * @throws BusinessException 投資家が存在しない場合（INVESTOR_NOT_FOUND）
     */
    public List<FacilityInvestmentDto> findByInvestor(Long investorId) {
        Investor investor = investorService.findById(investorId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Investor not found", "INVESTOR_NOT_FOUND"));
        return repository.findByInvestor(investor).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された金額より大きい投資を検索します。
     * 
     * @param amount 基準となる金額
     * @return 条件を満たす投資のDTOリスト
     */
    public List<FacilityInvestmentDto> findByInvestmentAmountGreaterThan(BigDecimal amount) {
        return repository.findByInvestmentAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 投資額を更新します。
     * 
     * @param investmentId 投資ID
     * @param newAmount 新しい投資金額
     * @return 更新された投資のDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - 投資が存在しない場合（INVESTMENT_NOT_FOUND）
     *                          - 金額が0以下の場合（INVALID_INVESTMENT_AMOUNT）
     */
    @Transactional
    public FacilityInvestmentDto updateInvestmentAmount(Long investmentId, BigDecimal newAmount) {
        FacilityInvestment investment = repository.findById(investmentId)
                .orElseThrow(() -> new BusinessException("Investment not found", "INVESTMENT_NOT_FOUND"));

        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Investment amount must be positive", "INVALID_INVESTMENT_AMOUNT");
        }

        investment.setInvestmentAmount(newAmount);
        investment.setAmount(newAmount);

        return toDto(repository.save(investment));
    }

    /**
     * 新規投資を作成し、即座に実行済み状態に設定します。
     * 
     * @param dto 作成する投資のDTO
     * @return 作成された投資のDTO
     */
    @Override
    @Transactional
    public FacilityInvestmentDto create(FacilityInvestmentDto dto) {
        FacilityInvestment entity = toEntity(dto);
        setBaseProperties(entity, dto);
        entity.setStatus("EXECUTED");
        entity.setProcessedDate(java.time.LocalDateTime.now());
        entity = repository.save(entity);
        return toDto(entity);
    }
}

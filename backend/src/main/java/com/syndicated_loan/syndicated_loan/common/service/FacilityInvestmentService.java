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

@Slf4j
@Service
@Transactional(readOnly = true)
public class FacilityInvestmentService 
    extends TransactionService<FacilityInvestment, FacilityInvestmentDto, FacilityInvestmentRepository> {

    private final InvestorService investorService;
    private final FacilityService facilityService;
    private final SharePieService sharePieService;

    public FacilityInvestmentService(
            FacilityInvestmentRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            InvestorService investorService,
            FacilityService facilityService,
            SharePieService sharePieService) {
        super(repository, amountPieService, positionService);
        this.investorService = investorService;
        this.facilityService = facilityService;
        this.sharePieService = sharePieService;
    }

    @Override
    public FacilityInvestment toEntity(FacilityInvestmentDto dto) {

        System.out.println("***** FacilityInvestmentService.toEntity *****");
        FacilityInvestment entity = new FacilityInvestment();
        entity.setId(dto.getId());
        entity.setType("FACILITY_INVESTMENT");
        entity.setDate(dto.getDate());
        entity.setProcessedDate(dto.getProcessedDate());
        // 金額はシェアパイを通じて算出する
        BigDecimal calculatedAmount = calculateInvestmentAmount(dto.getRelatedPositionId(), dto.getInvestorId());
        entity.setAmount(calculatedAmount);
        entity.setInvestmentAmount(calculatedAmount);

        // entity.setAmount(dto.getAmount());
        // entity.setInvestmentAmount(dto.getAmount());

        // 投資家の設定
        Investor investor = investorService.findById(dto.getInvestorId())
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Investor not found", "INVESTOR_NOT_FOUND"));
        entity.setInvestor(investor);

        // 関連するPositionとAmountPieの設定
        setBaseProperties(entity, dto);

        // entity.setInvestmentAmount(dto.getInvestmentAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    private BigDecimal calculateInvestmentAmount(Long facilityId, Long investorId) {
        // Facilityの取得
        var facility = facilityService.findById(facilityId)
            .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));

        // SharePieが設定されていない場合はエラー
        if (facility.getSharePieId() == null) {
            throw new BusinessException("Facility has no SharePie", "SHARE_PIE_NOT_FOUND");
        }

        // 投資家の比率を取得
        BigDecimal investorShare = sharePieService.getInvestorShare(facility.getSharePieId(), investorId);

        // 投資金額の計算 (Facilityの総額 × 投資家の比率)
        return facility.getTotalAmount()
            .multiply(investorShare)
            .divide(new BigDecimal("100"), 0, BigDecimal.ROUND_HALF_UP);
    }

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
                // .investmentAmount(entity.getInvestmentAmount())
                .version(entity.getVersion())
                .build();

        // レスポンス用の追加情報
        setBaseDtoProperties(dto, entity);
        dto.setInvestor(investorService.toDto(entity.getInvestor()));

        // 投資シェアの計算
        if (entity.getRelatedPosition() != null && 
            entity.getRelatedPosition().getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal investmentShare = entity.getInvestmentAmount()
                    .divide(entity.getRelatedPosition().getAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            dto.setInvestmentShare(investmentShare);
        }

        return dto;
    }

    // 追加の検索メソッド
    public List<FacilityInvestmentDto> findByInvestor(Long investorId) {
        Investor investor = investorService.findById(investorId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Investor not found", "INVESTOR_NOT_FOUND"));
        return repository.findByInvestor(investor).stream()
                .map(this::toDto)
                .toList();
    }

    public List<FacilityInvestmentDto> findByInvestmentAmountGreaterThan(BigDecimal amount) {
        return repository.findByInvestmentAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    // 投資額の更新
    @Transactional
    public FacilityInvestmentDto updateInvestmentAmount(Long investmentId, BigDecimal newAmount) {
        FacilityInvestment investment = repository.findById(investmentId)
                .orElseThrow(() -> new BusinessException("Investment not found", "INVESTMENT_NOT_FOUND"));

        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Investment amount must be positive", "INVALID_INVESTMENT_AMOUNT");
        }

        investment.setInvestmentAmount(newAmount);
        investment.setAmount(newAmount); // 取引金額も更新

        return toDto(repository.save(investment));
    }

    @Override
    @Transactional
    public FacilityInvestmentDto create(FacilityInvestmentDto dto) {
        FacilityInvestment entity = toEntity(dto);
        setBaseProperties(entity, dto);
        entity.setStatus("EXECUTED");  // ここでステータスを直接セット！
        entity.setProcessedDate(java.time.LocalDateTime.now());
        entity = repository.save(entity);
        return toDto(entity);
    }
}

package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.FacilityTradeDto;
import com.syndicated_loan.syndicated_loan.common.entity.FacilityTrade;
import com.syndicated_loan.syndicated_loan.common.entity.Investor;
import com.syndicated_loan.syndicated_loan.common.repository.FacilityTradeRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * シンジケートローンにおけるファシリティの売買取引（FacilityTrade）を管理するサービスクラス。
 * 投資家間のファシリティ持分の売買処理、取引金額の管理、取引シェアの計算などの機能を提供します。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class FacilityTradeService 
    extends TransactionService<FacilityTrade, FacilityTradeDto, FacilityTradeRepository> {

    private final InvestorService investorService;
    private final FacilityService facilityService;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param amountPieService 金額配分サービス
     * @param positionService ポジションサービス
     * @param investorService 投資家サービス
     * @param facilityService ファシリティサービス
     */
    public FacilityTradeService(
            FacilityTradeRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            InvestorService investorService,
            FacilityService facilityService) {
        super(repository, amountPieService, positionService, investorService);
        this.investorService = investorService;
        this.facilityService = facilityService;
    }

    /**
     * DTOをエンティティに変換します。
     * 売り手・買い手の投資家情報の検証と設定も行います。
     * 
     * @param dto 変換元のDTO
     * @return 変換されたファシリティ取引エンティティ
     * @throws BusinessException 以下の場合に発生:
     *                          - 売り手が存在しない場合（SELLER_NOT_FOUND）
     *                          - 買い手が存在しない場合（BUYER_NOT_FOUND）
     */
    @Override
    public FacilityTrade toEntity(FacilityTradeDto dto) {
        FacilityTrade entity = new FacilityTrade();
        entity.setId(dto.getId());
        entity.setType("FACILITY_TRADE");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getAmount());

        Investor seller = investorService.findById(dto.getSellerId())
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Seller not found", "SELLER_NOT_FOUND"));
        entity.setSeller(seller);

        Investor buyer = investorService.findById(dto.getBuyerId())
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Buyer not found", "BUYER_NOT_FOUND"));
        entity.setBuyer(buyer);

        setBaseProperties(entity, dto);
        entity.setTradeAmount(dto.getTradeAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    /**
     * エンティティをDTOに変換します。
     * 取引シェアの計算や関連する投資家情報の設定も行います。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換されたファシリティ取引DTO
     */
    @Override
    public FacilityTradeDto toDto(FacilityTrade entity) {
        FacilityTradeDto dto = FacilityTradeDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .date(entity.getDate())
                .amount(entity.getAmount())
                .relatedPositionId(entity.getRelatedPosition().getId())
                .amountPieId(entity.getAmountPie() != null ? entity.getAmountPie().getId() : null)
                .sellerId(entity.getSeller().getId())
                .buyerId(entity.getBuyer().getId())
                .tradeAmount(entity.getTradeAmount())
                .version(entity.getVersion())
                .build();

        setBaseDtoProperties(dto, entity);
        dto.setSeller(investorService.toDto(entity.getSeller()));
        dto.setBuyer(investorService.toDto(entity.getBuyer()));

        if (entity.getRelatedPosition() != null && 
            entity.getRelatedPosition().getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tradeShare = entity.getTradeAmount()
                    .divide(entity.getRelatedPosition().getAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            dto.setTradeShare(tradeShare);
        }

        return dto;
    }

    /**
     * 指定された売り手の全ての取引を検索します。
     * 
     * @param sellerId 売り手の投資家ID
     * @return 売り手の取引のDTOリスト
     * @throws BusinessException 売り手が存在しない場合（SELLER_NOT_FOUND）
     */
    public List<FacilityTradeDto> findBySeller(Long sellerId) {
        Investor seller = investorService.findById(sellerId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Seller not found", "SELLER_NOT_FOUND"));
        return repository.findBySeller(seller).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された買い手の全ての取引を検索します。
     * 
     * @param buyerId 買い手の投資家ID
     * @return 買い手の取引のDTOリスト
     * @throws BusinessException 買い手が存在しない場合（BUYER_NOT_FOUND）
     */
    public List<FacilityTradeDto> findByBuyer(Long buyerId) {
        Investor buyer = investorService.findById(buyerId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Buyer not found", "BUYER_NOT_FOUND"));
        return repository.findByBuyer(buyer).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された金額より大きい取引を検索します。
     * 
     * @param amount 基準となる金額
     * @return 条件を満たす取引のDTOリスト
     */
    public List<FacilityTradeDto> findByTradeAmountGreaterThan(BigDecimal amount) {
        return repository.findByTradeAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された投資家が売り手または買い手として関与した全ての取引を検索します。
     * 
     * @param investorId 投資家ID
     * @return 投資家が関与した取引のDTOリスト
     * @throws BusinessException 投資家が存在しない場合（INVESTOR_NOT_FOUND）
     */
    public List<FacilityTradeDto> findBySellerOrBuyer(Long investorId) {
        Investor investor = investorService.findById(investorId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Investor not found", "INVESTOR_NOT_FOUND"));
        return repository.findBySellerOrBuyer(investor, investor).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 取引金額を更新します。
     * 
     * @param tradeId 取引ID
     * @param newAmount 新しい取引金額
     * @return 更新された取引のDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - 取引が存在しない場合（TRADE_NOT_FOUND）
     *                          - 金額が0以下の場合（INVALID_TRADE_AMOUNT）
     */
    @Transactional
    public FacilityTradeDto updateTradeAmount(Long tradeId, BigDecimal newAmount) {
        FacilityTrade trade = repository.findById(tradeId)
                .orElseThrow(() -> new BusinessException("Trade not found", "TRADE_NOT_FOUND"));

        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Trade amount must be positive", "INVALID_TRADE_AMOUNT");
        }

        trade.setTradeAmount(newAmount);
        trade.setAmount(newAmount);

        return toDto(repository.save(trade));
    }
}

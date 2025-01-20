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

@Slf4j
@Service
@Transactional(readOnly = true)
public class FacilityTradeService 
    extends TransactionService<FacilityTrade, FacilityTradeDto, FacilityTradeRepository> {

    private final InvestorService investorService;
    private final FacilityService facilityService;

    public FacilityTradeService(
            FacilityTradeRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            InvestorService investorService,
            FacilityService facilityService) {
        super(repository, amountPieService, positionService);
        this.investorService = investorService;
        this.facilityService = facilityService;
    }

    @Override
    public FacilityTrade toEntity(FacilityTradeDto dto) {
        FacilityTrade entity = new FacilityTrade();
        entity.setId(dto.getId());
        entity.setType("FACILITY_TRADE");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getAmount());

        // 売り手の設定
        Investor seller = investorService.findById(dto.getSellerId())
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Seller not found", "SELLER_NOT_FOUND"));
        entity.setSeller(seller);

        // 買い手の設定
        Investor buyer = investorService.findById(dto.getBuyerId())
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Buyer not found", "BUYER_NOT_FOUND"));
        entity.setBuyer(buyer);

        // 関連するPositionとAmountPieの設定
        setBaseProperties(entity, dto);

        entity.setTradeAmount(dto.getTradeAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

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

        // レスポンス用の追加情報
        setBaseDtoProperties(dto, entity);
        dto.setSeller(investorService.toDto(entity.getSeller()));
        dto.setBuyer(investorService.toDto(entity.getBuyer()));

        // 取引シェアの計算
        if (entity.getRelatedPosition() != null && 
            entity.getRelatedPosition().getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tradeShare = entity.getTradeAmount()
                    .divide(entity.getRelatedPosition().getAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            dto.setTradeShare(tradeShare);
        }

        return dto;
    }

    // 追加の検索メソッド
    public List<FacilityTradeDto> findBySeller(Long sellerId) {
        Investor seller = investorService.findById(sellerId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Seller not found", "SELLER_NOT_FOUND"));
        return repository.findBySeller(seller).stream()
                .map(this::toDto)
                .toList();
    }

    public List<FacilityTradeDto> findByBuyer(Long buyerId) {
        Investor buyer = investorService.findById(buyerId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Buyer not found", "BUYER_NOT_FOUND"));
        return repository.findByBuyer(buyer).stream()
                .map(this::toDto)
                .toList();
    }

    public List<FacilityTradeDto> findByTradeAmountGreaterThan(BigDecimal amount) {
        return repository.findByTradeAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    public List<FacilityTradeDto> findBySellerOrBuyer(Long investorId) {
        Investor investor = investorService.findById(investorId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Investor not found", "INVESTOR_NOT_FOUND"));
        return repository.findBySellerOrBuyer(investor, investor).stream()
                .map(this::toDto)
                .toList();
    }

    // 取引金額の更新
    @Transactional
    public FacilityTradeDto updateTradeAmount(Long tradeId, BigDecimal newAmount) {
        FacilityTrade trade = repository.findById(tradeId)
                .orElseThrow(() -> new BusinessException("Trade not found", "TRADE_NOT_FOUND"));

        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Trade amount must be positive", "INVALID_TRADE_AMOUNT");
        }

        trade.setTradeAmount(newAmount);
        trade.setAmount(newAmount); // 取引金額も更新

        return toDto(repository.save(trade));
    }
}

package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.DrawdownDto;
import com.syndicated_loan.syndicated_loan.common.entity.Drawdown;
import com.syndicated_loan.syndicated_loan.common.entity.Facility;
import com.syndicated_loan.syndicated_loan.common.repository.DrawdownRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DrawdownService 
    extends TransactionService<Drawdown, DrawdownDto, DrawdownRepository> {

    private final FacilityService facilityService;

    public DrawdownService(
            DrawdownRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            FacilityService facilityService) {
        super(repository, amountPieService, positionService);
        this.facilityService = facilityService;
    }

    @Override
    public Drawdown toEntity(DrawdownDto dto) {
        Drawdown entity = new Drawdown();
        entity.setId(dto.getId());
        entity.setType("DRAWDOWN");
        entity.setDate(dto.getDate());
        entity.setProcessedDate(dto.getProcessedDate());
        entity.setAmount(dto.getDrawdownAmount());

        // 関連するファシリティの設定
        Facility facility = facilityService.findById(dto.getRelatedFacilityId())
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        entity.setRelatedFacility(facility);

        // 関連するPositionとAmountPieの設定
        setBaseProperties(entity, dto);

        entity.setDrawdownAmount(dto.getDrawdownAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

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

        // レスポンス用の追加情報
        setBaseDtoProperties(dto, entity);
        dto.setRelatedFacility(facilityService.toDto(entity.getRelatedFacility()));

        // 残額と利用率の計算
        Facility facility = entity.getRelatedFacility();
        dto.setRemainingFacilityAmount(facility.getAvailableAmount());
        if (facility.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilizationRate = BigDecimal.ONE
                    .subtract(facility.getAvailableAmount().divide(facility.getTotalAmount(), 4, BigDecimal.ROUND_HALF_UP))
                    .multiply(new BigDecimal("100"));
            dto.setUtilizationRate(utilizationRate);
        }

        return dto;
    }

    // 追加の検索メソッド
    public List<DrawdownDto> findByRelatedFacility(Long facilityId) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByRelatedFacility(facility).stream()
                .map(this::toDto)
                .toList();
    }

    public List<DrawdownDto> findByDrawdownAmountGreaterThan(BigDecimal amount) {
        return repository.findByDrawdownAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    public List<DrawdownDto> findByRelatedFacilityAndDrawdownAmountGreaterThan(Long facilityId, BigDecimal amount) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByRelatedFacilityAndDrawdownAmountGreaterThan(facility, amount).stream()
                .map(this::toDto)
                .toList();
    }

    // ドローダウン実行（ファシリティの利用可能額も更新）
    @Transactional
    public DrawdownDto executeDrawdown(Long drawdownId) {
        Drawdown drawdown = repository.findById(drawdownId)
                .orElseThrow(() -> new BusinessException("Drawdown not found", "DRAWDOWN_NOT_FOUND"));

        Facility facility = drawdown.getRelatedFacility();
        BigDecimal newAvailableAmount = facility.getAvailableAmount().subtract(drawdown.getDrawdownAmount());

        if (newAvailableAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Insufficient available amount", "INSUFFICIENT_AVAILABLE_AMOUNT");
        }

        // ファシリティの利用可能額を更新
        facilityService.updateAvailableAmount(facility.getId(), newAvailableAmount);

        // ドローダウンのステータスを更新
        drawdown.setStatus("EXECUTED");
        drawdown.setProcessedDate(java.time.LocalDateTime.now());

        return toDto(repository.save(drawdown));
    }

    // ドローダウン金額の更新（実行前のみ可能）
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
        drawdown.setAmount(newAmount); // 取引金額も更新

        return toDto(repository.save(drawdown));
    }

    @Override
    @Transactional
    public DrawdownDto create(DrawdownDto dto) {
        // AmountPieの生成
        if (dto.getAmountPie() != null) {
            var amountPie = amountPieService.create(dto.getAmountPie());
            dto.setAmountPieId(amountPie.getId());
        }

        // 基底クラスのcreateを呼び出し
        return super.create(dto);
    }
}

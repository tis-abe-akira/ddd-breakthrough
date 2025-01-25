package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.FeePaymentDto;
import com.syndicated_loan.syndicated_loan.common.dto.AmountPieDto;
import com.syndicated_loan.syndicated_loan.common.entity.FeePayment;
import com.syndicated_loan.syndicated_loan.common.entity.Facility;
import com.syndicated_loan.syndicated_loan.common.entity.SharePie;
import com.syndicated_loan.syndicated_loan.common.repository.FeePaymentRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@Transactional(readOnly = true)
public class FeePaymentService 
    extends TransactionService<FeePayment, FeePaymentDto, FeePaymentRepository> {

    private final FacilityService facilityService;
    private final SharePieService sharePieService;  // 追加！

    public FeePaymentService(
            FeePaymentRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            FacilityService facilityService,
            SharePieService sharePieService) {  // 追加！
        super(repository, amountPieService, positionService);
        this.facilityService = facilityService;
        this.sharePieService = sharePieService;  // 追加！
    }

    @Override
    public FeePayment toEntity(FeePaymentDto dto) {
        FeePayment entity = new FeePayment();
        entity.setId(dto.getId());
        entity.setType("FEE_PAYMENT");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getPaymentAmount());  // ここ追加！

        // 関連するファシリティの設定
        Facility facility = facilityService.findById(dto.getFacilityId())
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        entity.setFacility(facility);

        // 関連するPositionとAmountPieの設定
        setBaseProperties(entity, dto);

        entity.setFeeType(dto.getFeeType());
        entity.setPaymentAmount(dto.getPaymentAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    @Override
    public FeePaymentDto toDto(FeePayment entity) {
        FeePaymentDto dto = FeePaymentDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .date(entity.getDate())
                .amount(entity.getAmount())
                .relatedPositionId(entity.getRelatedPosition().getId())
                .amountPieId(entity.getAmountPie() != null ? entity.getAmountPie().getId() : null)
                .facilityId(entity.getFacility().getId())
                .feeType(entity.getFeeType())
                .paymentAmount(entity.getPaymentAmount())
                .version(entity.getVersion())
                .build();

        // レスポンス用の追加情報
        setBaseDtoProperties(dto, entity);
        dto.setFacility(facilityService.toDto(entity.getFacility()));

        // 手数料率の計算（年率）
        if (entity.getFacility().getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal feeRate = entity.getPaymentAmount()
                    .divide(entity.getFacility().getTotalAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            dto.setFeeRate(feeRate);
        }

        return dto;
    }

    // 追加の検索メソッド
    public List<FeePaymentDto> findByFacility(Long facilityId) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByFacility(facility).stream()
                .map(this::toDto)
                .toList();
    }

    public List<FeePaymentDto> findByFeeType(String feeType) {
        return repository.findByFeeType(feeType).stream()
                .map(this::toDto)
                .toList();
    }

    public List<FeePaymentDto> findByPaymentAmountGreaterThan(BigDecimal amount) {
        return repository.findByPaymentAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    public List<FeePaymentDto> findByFacilityAndFeeType(Long facilityId, String feeType) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByFacilityAndFeeType(facility, feeType).stream()
                .map(this::toDto)
                .toList();
    }

    // 手数料支払いの実行
    @Transactional
    public FeePaymentDto executeFeePayment(Long feePaymentId) {
        FeePayment feePayment = repository.findById(feePaymentId)
                .orElseThrow(() -> new BusinessException("Fee payment not found", "FEE_PAYMENT_NOT_FOUND"));

        // 支払い済みの場合はエラー
        if ("EXECUTED".equals(feePayment.getStatus())) {
            throw new BusinessException("Fee payment already executed", "FEE_PAYMENT_ALREADY_EXECUTED");
        }

        // Facilityからシェアパイを取得
        Facility facility = feePayment.getFacility();
        SharePie sharePie = facility.getSharePie();
        if (sharePie == null) {
            throw new BusinessException("Facility has no SharePie", "SHARE_PIE_NOT_FOUND");
        }

        // 新しい AmountPie の作成
        AmountPieDto amountPieDto = createAmountPieFromSharePie(
            sharePie,
            feePayment.getPaymentAmount()
        );
        
        // AmountPie を保存して FeePayment に設定
        AmountPieDto savedAmountPie = amountPieService.create(amountPieDto);
        feePayment.setAmountPie(amountPieService.toEntity(savedAmountPie));

        // ステータスを更新
        feePayment.setStatus("EXECUTED");
        feePayment.setProcessedDate(java.time.LocalDateTime.now());

        return toDto(repository.save(feePayment));
    }

    // SharePie から AmountPie を生成するヘルパーメソッド
    private AmountPieDto createAmountPieFromSharePie(SharePie sharePie, BigDecimal totalAmount) {
        Map<Long, BigDecimal> amounts = new HashMap<>();
        
        sharePie.getShares().forEach((investorId, sharePercentage) -> {
            BigDecimal amount = totalAmount
                .multiply(sharePercentage)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            amounts.put(investorId, amount);
        });

        return AmountPieDto.builder()
                .amounts(amounts)
                .build();
    }

    // 手数料金額の更新（実行前のみ可能）
    @Transactional
    public FeePaymentDto updatePaymentAmount(Long feePaymentId, BigDecimal newAmount) {
        FeePayment feePayment = repository.findById(feePaymentId)
                .orElseThrow(() -> new BusinessException("Fee payment not found", "FEE_PAYMENT_NOT_FOUND"));

        if ("EXECUTED".equals(feePayment.getStatus())) {
            throw new BusinessException("Cannot update executed fee payment", "FEE_PAYMENT_ALREADY_EXECUTED");
        }

        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Fee payment amount must be positive", "INVALID_FEE_PAYMENT_AMOUNT");
        }

        feePayment.setPaymentAmount(newAmount);
        feePayment.setAmount(newAmount); // 取引金額も更新

        return toDto(repository.save(feePayment));
    }
}

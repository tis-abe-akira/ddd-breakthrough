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

/**
 * シンジケートローンにおける手数料支払い（FeePayment）を管理するサービスクラス。
 * 各種手数料の支払い処理、金額の管理、シェアに基づく配分計算などの機能を提供します。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class FeePaymentService 
    extends TransactionService<FeePayment, FeePaymentDto, FeePaymentRepository> {

    private final FacilityService facilityService;
    private final SharePieService sharePieService;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param amountPieService 金額配分サービス
     * @param positionService ポジションサービス
     * @param facilityService ファシリティサービス
     * @param sharePieService シェア配分サービス
     * @param investorService 投資家サービス
     */
    public FeePaymentService(
            FeePaymentRepository repository,
            AmountPieService amountPieService,
            PositionService positionService,
            FacilityService facilityService,
            SharePieService sharePieService,
            InvestorService investorService) {
        super(repository, amountPieService, positionService, investorService);
        this.facilityService = facilityService;
        this.sharePieService = sharePieService;
    }

    /**
     * DTOをエンティティに変換します。
     * 手数料支払いの基本情報設定に加えて、関連するファシリティの検証も行います。
     * 
     * @param dto 変換元のDTO
     * @return 変換された手数料支払いエンティティ
     * @throws BusinessException ファシリティが存在しない場合（FACILITY_NOT_FOUND）
     */
    @Override
    public FeePayment toEntity(FeePaymentDto dto) {
        FeePayment entity = new FeePayment();
        entity.setId(dto.getId());
        entity.setType("FEE_PAYMENT");
        entity.setDate(dto.getDate());
        entity.setAmount(dto.getPaymentAmount());

        Facility facility = facilityService.findById(dto.getFacilityId())
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        entity.setFacility(facility);

        setBaseProperties(entity, dto);
        entity.setFeeType(dto.getFeeType());
        entity.setPaymentAmount(dto.getPaymentAmount());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    /**
     * エンティティをDTOに変換します。
     * 手数料率の計算など、追加情報も設定します。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換された手数料支払いDTO
     */
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

        setBaseDtoProperties(dto, entity);
        dto.setFacility(facilityService.toDto(entity.getFacility()));

        if (entity.getFacility().getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal feeRate = entity.getPaymentAmount()
                    .divide(entity.getFacility().getTotalAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            dto.setFeeRate(feeRate);
        }

        return dto;
    }

    /**
     * 指定されたファシリティに関連する全ての手数料支払いを検索します。
     * 
     * @param facilityId ファシリティID
     * @return 手数料支払いのDTOリスト
     * @throws BusinessException ファシリティが存在しない場合（FACILITY_NOT_FOUND）
     */
    public List<FeePaymentDto> findByFacility(Long facilityId) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByFacility(facility).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された手数料タイプの支払いを検索します。
     * 
     * @param feeType 手数料タイプ
     * @return 手数料支払いのDTOリスト
     */
    public List<FeePaymentDto> findByFeeType(String feeType) {
        return repository.findByFeeType(feeType).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された金額より大きい手数料支払いを検索します。
     * 
     * @param amount 基準となる金額
     * @return 条件を満たす手数料支払いのDTOリスト
     */
    public List<FeePaymentDto> findByPaymentAmountGreaterThan(BigDecimal amount) {
        return repository.findByPaymentAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定されたファシリティと手数料タイプの支払いを検索します。
     * 
     * @param facilityId ファシリティID
     * @param feeType 手数料タイプ
     * @return 条件を満たす手数料支払いのDTOリスト
     * @throws BusinessException ファシリティが存在しない場合（FACILITY_NOT_FOUND）
     */
    public List<FeePaymentDto> findByFacilityAndFeeType(Long facilityId, String feeType) {
        Facility facility = facilityService.findById(facilityId)
                .map(facilityService::toEntity)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
        return repository.findByFacilityAndFeeType(facility, feeType).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 手数料支払いを実行し、シェアに基づいて配分を行います。
     * 
     * @param feePaymentId 手数料支払いID
     * @return 実行された手数料支払いのDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - 手数料支払いが存在しない場合（FEE_PAYMENT_NOT_FOUND）
     *                          - すでに実行済みの場合（FEE_PAYMENT_ALREADY_EXECUTED）
     *                          - シェア配分が設定されていない場合（SHARE_PIE_NOT_FOUND）
     */
    @Transactional
    public FeePaymentDto executeFeePayment(Long feePaymentId) {
        FeePayment feePayment = repository.findById(feePaymentId)
                .orElseThrow(() -> new BusinessException("Fee payment not found", "FEE_PAYMENT_NOT_FOUND"));

        if ("EXECUTED".equals(feePayment.getStatus())) {
            throw new BusinessException("Fee payment already executed", "FEE_PAYMENT_ALREADY_EXECUTED");
        }

        Facility facility = feePayment.getFacility();
        SharePie sharePie = facility.getSharePie();
        if (sharePie == null) {
            throw new BusinessException("Facility has no SharePie", "SHARE_PIE_NOT_FOUND");
        }

        AmountPieDto amountPieDto = createAmountPieFromSharePie(
            sharePie,
            feePayment.getPaymentAmount()
        );
        
        AmountPieDto savedAmountPie = amountPieService.create(amountPieDto);
        feePayment.setAmountPie(amountPieService.toEntity(savedAmountPie));

        feePayment.setStatus("EXECUTED");
        feePayment.setProcessedDate(java.time.LocalDateTime.now());

        return toDto(repository.save(feePayment));
    }

    /**
     * SharePieの配分比率に基づいてAmountPieを生成します。
     * 
     * @param sharePie シェア配分情報
     * @param totalAmount 総支払額
     * @return 生成されたAmountPieのDTO
     */
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

    /**
     * 手数料支払い金額を更新します。
     * 実行済みの支払いは更新できません。
     * 
     * @param feePaymentId 手数料支払いID
     * @param newAmount 新しい支払金額
     * @return 更新された手数料支払いのDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - 手数料支払いが存在しない場合（FEE_PAYMENT_NOT_FOUND）
     *                          - すでに実行済みの場合（FEE_PAYMENT_ALREADY_EXECUTED）
     *                          - 金額が0以下の場合（INVALID_FEE_PAYMENT_AMOUNT）
     */
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
        feePayment.setAmount(newAmount);

        return toDto(repository.save(feePayment));
    }
}

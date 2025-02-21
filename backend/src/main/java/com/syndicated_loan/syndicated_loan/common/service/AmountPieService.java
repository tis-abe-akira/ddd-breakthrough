package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.AmountPieDto;
import com.syndicated_loan.syndicated_loan.common.entity.AmountPie;
import com.syndicated_loan.syndicated_loan.common.repository.AmountPieRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * シンジケートローンにおける金額配分（Amount Pie）を管理するサービスクラス。
 * 各投資家への配分金額の管理、検証、集計などの機能を提供します。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AmountPieService extends AbstractBaseService<AmountPie, Long, AmountPieDto, AmountPieRepository> {

    private final InvestorService investorService;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param investorService 投資家サービスインスタンス（投資家情報の取得に使用）
     */
    public AmountPieService(AmountPieRepository repository, InvestorService investorService) {
        super(repository);
        this.investorService = investorService;
    }

    @Override
    protected void setEntityId(AmountPie entity, Long id) {
        entity.setId(id);
    }

    /**
     * DTOをエンティティに変換します。
     * 金額配分の妥当性チェックも実施します。
     * 
     * @param dto 変換元のDTO
     * @return 変換されたエンティティ
     * @throws BusinessException 金額配分が無効な場合
     */
    @Override
    public AmountPie toEntity(AmountPieDto dto) {
        AmountPie entity = new AmountPie();
        entity.setId(dto.getId());
        entity.setAmounts(new HashMap<>(dto.getAmounts()));
        entity.setVersion(dto.getVersion());
        validateAmounts(entity.getAmounts());
        return entity;
    }

    /**
     * エンティティをDTOに変換します。
     * 投資家名による金額マップの作成や合計金額の計算も行います。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換されたDTO（投資家名による金額マップと合計金額を含む）
     */
    @Override
    public AmountPieDto toDto(AmountPie entity) {
        AmountPieDto dto = AmountPieDto.builder()
                .id(entity.getId())
                .amounts(new HashMap<>(entity.getAmounts()))
                .version(entity.getVersion())
                .build();

        // レスポンス用の追加情報
        Map<String, BigDecimal> investorAmounts = new HashMap<>();
        entity.getAmounts().forEach((investorId, amount) -> {
            investorService.findById(investorId).ifPresent(investor ->
                investorAmounts.put(investor.getName(), amount));
        });
        dto.setInvestorAmounts(investorAmounts);

        // 合計金額の計算
        BigDecimal totalAmount = entity.getAmounts().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        dto.setTotalAmount(totalAmount);

        return dto;
    }

    /**
     * 金額配分の妥当性を検証します。
     * 
     * @param amounts 検証対象の金額配分マップ（投資家ID -> 配分金額）
     * @throws BusinessException 以下の場合に発生:
     *                          - 金額マップがnullまたは空の場合（EMPTY_AMOUNTS）
     *                          - 金額が0以下の場合（INVALID_AMOUNT）
     */
    private void validateAmounts(Map<Long, BigDecimal> amounts) {
        if (amounts == null || amounts.isEmpty()) {
            throw new BusinessException("Amounts cannot be empty", "EMPTY_AMOUNTS");
        }

        // 全ての金額が正の数であることを確認
        amounts.values().forEach(amount -> {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Amount must be positive", "INVALID_AMOUNT");
            }
        });
    }

    /**
     * 特定の投資家の配分金額を取得します。
     * 
     * @param amountPieId 金額配分ID
     * @param investorId  投資家ID
     * @return 投資家の配分金額（配分が存在しない場合は0）
     * @throws BusinessException 金額配分が存在しない場合（AMOUNT_PIE_NOT_FOUND）
     */
    public BigDecimal getInvestorAmount(Long amountPieId, Long investorId) {
        return findById(amountPieId)
                .map(dto -> dto.getAmounts().getOrDefault(investorId, BigDecimal.ZERO))
                .orElseThrow(() -> new BusinessException("AmountPie not found", "AMOUNT_PIE_NOT_FOUND"));
    }

    /**
     * 金額配分を更新します。
     * 
     * @param amountPieId 更新対象の金額配分ID
     * @param newAmounts  新しい金額配分マップ（投資家ID -> 配分金額）
     * @return 更新された金額配分のDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - 金額配分が存在しない場合（AMOUNT_PIE_NOT_FOUND）
     *                          - 新しい金額配分が無効な場合（EMPTY_AMOUNTS, INVALID_AMOUNT）
     */
    @Transactional
    public AmountPieDto updateAmounts(Long amountPieId, Map<Long, BigDecimal> newAmounts) {
        AmountPie amountPie = repository.findById(amountPieId)
                .orElseThrow(() -> new BusinessException("AmountPie not found", "AMOUNT_PIE_NOT_FOUND"));

        validateAmounts(newAmounts);
        amountPie.setAmounts(newAmounts);

        return toDto(repository.save(amountPie));
    }

    /**
     * 指定された最小金額以上の配分を持つ全ての金額配分を検索します。
     * 
     * @param minAmount 最小金額
     * @return 条件を満たす金額配分のDTOリスト
     */
    public List<AmountPieDto> findByMinimumAmount(BigDecimal minAmount) {
        return repository.findByMinimumAmount(minAmount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 特定の投資家が参加している全ての金額配分を検索します。
     * 
     * @param investorId 投資家ID
     * @return 投資家が参加している金額配分のDTOリスト
     */
    public List<AmountPieDto> findByInvestorId(Long investorId) {
        return repository.findByInvestorId(investorId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 特定の投資家の全ての配分金額の合計を計算します。
     * 
     * @param investorId 投資家ID
     * @return 投資家の総配分金額
     */
    public BigDecimal sumAmountsByInvestorId(Long investorId) {
        return repository.sumAmountsByInvestorId(investorId);
    }
}

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
 * AmountPie（金額配分）に関する操作を提供するサービスクラス。
 * 
 * <p>
 * AmountPieは投資家IDと具体的な金額のマッピングを管理し、ドローダウンや
 * 支払いなどの取引における金額配分を表現します。SharePieがパーセント単位の
 * 配分を管理するのに対し、AmountPieは実際の金額単位の配分を管理します。
 * </p>
 * 
 * <p>
 * このサービスでは、金額配分の作成、更新、取得の他、投資家単位の金額取得や
 * 特定条件での検索機能を提供します。また、投資家ごとの総額計算も可能です。
 * </p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AmountPieService extends AbstractBaseService<AmountPie, Long, AmountPieDto, AmountPieRepository> {

    private final InvestorService investorService;

    public AmountPieService(AmountPieRepository repository, InvestorService investorService) {
        super(repository);
        this.investorService = investorService;
    }

    @Override
    protected void setEntityId(AmountPie entity, Long id) {
        entity.setId(id);
    }

    @Override
    public AmountPie toEntity(AmountPieDto dto) {
        AmountPie entity = new AmountPie();
        entity.setId(dto.getId());
        entity.setAmounts(new HashMap<>(dto.getAmounts()));
        entity.setVersion(dto.getVersion());
        validateAmounts(entity.getAmounts());
        return entity;
    }

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
            investorService.findById(investorId).ifPresent(investor -> investorAmounts.put(investor.getName(), amount));
        });
        dto.setInvestorAmounts(investorAmounts);

        // 合計金額の計算
        BigDecimal totalAmount = entity.getAmounts().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
        dto.setTotalAmount(totalAmount);

        return dto;
    }

    // 金額の検証
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

    // 投資家の金額を取得
    public BigDecimal getInvestorAmount(Long amountPieId, Long investorId) {
        return findById(amountPieId)
                .map(dto -> dto.getAmounts().getOrDefault(investorId, BigDecimal.ZERO))
                .orElseThrow(() -> new BusinessException("AmountPie not found", "AMOUNT_PIE_NOT_FOUND"));
    }

    // 金額の更新
    @Transactional
    public AmountPieDto updateAmounts(Long amountPieId, Map<Long, BigDecimal> newAmounts) {
        AmountPie amountPie = repository.findById(amountPieId)
                .orElseThrow(() -> new BusinessException("AmountPie not found", "AMOUNT_PIE_NOT_FOUND"));

        validateAmounts(newAmounts);
        amountPie.setAmounts(newAmounts);

        return toDto(repository.save(amountPie));
    }

    // 最小金額以上の投資家を検索
    public List<AmountPieDto> findByMinimumAmount(BigDecimal minAmount) {
        return repository.findByMinimumAmount(minAmount).stream()
                .map(this::toDto)
                .toList();
    }

    // 特定の投資家が参加しているAmountPieを検索
    public List<AmountPieDto> findByInvestorId(Long investorId) {
        return repository.findByInvestorId(investorId).stream()
                .map(this::toDto)
                .toList();
    }

    // 投資家の総額を計算
    public BigDecimal sumAmountsByInvestorId(Long investorId) {
        return repository.sumAmountsByInvestorId(investorId);
    }
}

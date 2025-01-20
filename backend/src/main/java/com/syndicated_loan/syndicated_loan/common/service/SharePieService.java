package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.SharePieDto;
import com.syndicated_loan.syndicated_loan.common.entity.SharePie;
import com.syndicated_loan.syndicated_loan.common.repository.SharePieRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SharePieService extends AbstractBaseService<SharePie, Long, SharePieDto, SharePieRepository> {

    private final InvestorService investorService;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.0000");

    public SharePieService(SharePieRepository repository, InvestorService investorService) {
        super(repository);
        this.investorService = investorService;
    }

    @Override
    protected void setEntityId(SharePie entity, Long id) {
        entity.setId(id);
    }

    @Override
    public SharePie toEntity(SharePieDto dto) {
        SharePie entity = new SharePie();
        entity.setId(dto.getId());
        entity.setShares(new HashMap<>(dto.getShares()));
        entity.setVersion(dto.getVersion());
        validateShares(entity.getShares());
        return entity;
    }

    @Override
    public SharePieDto toDto(SharePie entity) {
        SharePieDto dto = SharePieDto.builder()
                .id(entity.getId())
                .shares(new HashMap<>(entity.getShares()))
                .version(entity.getVersion())
                .build();

        // レスポンス用の追加情報
        Map<String, BigDecimal> investorShares = new HashMap<>();
        entity.getShares().forEach((investorId, share) -> {
            investorService.findById(investorId).ifPresent(investor ->
                investorShares.put(investor.getName(), share));
        });
        dto.setInvestorShares(investorShares);

        return dto;
    }

    // シェアの検証
    private void validateShares(Map<Long, BigDecimal> shares) {
        if (shares == null || shares.isEmpty()) {
            throw new BusinessException("Shares cannot be empty", "EMPTY_SHARES");
        }

        // 全てのシェアが正の数であることを確認
        shares.values().forEach(share -> {
            if (share.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Share must be positive", "INVALID_SHARE");
            }
        });

        // シェアの合計が100%であることを確認
        BigDecimal total = shares.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        if (total.compareTo(ONE_HUNDRED) != 0) {
            throw new BusinessException(
                "Total shares must be 100%, but was: " + total, 
                "INVALID_TOTAL_SHARE"
            );
        }
    }

    // 投資家のシェアを取得
    public BigDecimal getInvestorShare(Long sharePieId, Long investorId) {
        return findById(sharePieId)
                .map(dto -> dto.getShares().getOrDefault(investorId, BigDecimal.ZERO))
                .orElseThrow(() -> new BusinessException("SharePie not found", "SHARE_PIE_NOT_FOUND"));
    }

    // シェアの更新
    @Transactional
    public SharePieDto updateShares(Long sharePieId, Map<Long, BigDecimal> newShares) {
        SharePie sharePie = repository.findById(sharePieId)
                .orElseThrow(() -> new BusinessException("SharePie not found", "SHARE_PIE_NOT_FOUND"));

        validateShares(newShares);
        sharePie.setShares(newShares);

        return toDto(repository.save(sharePie));
    }

    // 最小シェア以上の投資家を検索
    public List<SharePieDto> findByMinimumShare(BigDecimal minShare) {
        return repository.findByMinimumShare(minShare).stream()
                .map(this::toDto)
                .toList();
    }

    // 特定の投資家が参加しているシェアパイを検索
    public List<SharePieDto> findByInvestorId(Long investorId) {
        return repository.findByInvestorId(investorId).stream()
                .map(this::toDto)
                .toList();
    }
}

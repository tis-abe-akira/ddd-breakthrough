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

/**
 * シンジケートローンにおける出資比率（SharePie）を管理するサービスクラス。
 * 投資家ごとの出資比率の管理、合計100%の検証、シェア配分の更新などの機能を提供します。
 * すべての比率は小数点4桁まで管理され、合計が厳密に100%となることを保証します。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SharePieService extends AbstractBaseService<SharePie, Long, SharePieDto, SharePieRepository> {

    private final InvestorService investorService;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.0000");

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param investorService 投資家サービス
     */
    public SharePieService(SharePieRepository repository, InvestorService investorService) {
        super(repository);
        this.investorService = investorService;
    }

    @Override
    protected void setEntityId(SharePie entity, Long id) {
        entity.setId(id);
    }

    /**
     * DTOをエンティティに変換します。
     * シェアの妥当性検証も行います。
     * 
     * @param dto 変換元のDTO
     * @return 変換されたシェアパイエンティティ
     * @throws BusinessException シェアが無効な場合（EMPTY_SHARES, INVALID_SHARE, INVALID_TOTAL_SHARE）
     */
    @Override
    public SharePie toEntity(SharePieDto dto) {
        SharePie entity = new SharePie();
        entity.setId(dto.getId());
        entity.setShares(new HashMap<>(dto.getShares()));
        entity.setVersion(dto.getVersion());
        validateShares(entity.getShares());
        return entity;
    }

    /**
     * エンティティをDTOに変換します。
     * 投資家名による比率マップも設定します。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換されたシェアパイDTO
     */
    @Override
    public SharePieDto toDto(SharePie entity) {
        SharePieDto dto = SharePieDto.builder()
                .id(entity.getId())
                .shares(new HashMap<>(entity.getShares()))
                .version(entity.getVersion())
                .build();

        Map<String, BigDecimal> investorShares = new HashMap<>();
        entity.getShares().forEach((investorId, share) -> {
            investorService.findById(investorId).ifPresent(investor ->
                investorShares.put(investor.getName(), share));
        });
        dto.setInvestorShares(investorShares);

        return dto;
    }

    /**
     * シェアの妥当性を検証します。
     * すべてのシェアが正の数で、合計が厳密に100%となることを確認します。
     * 
     * @param shares 検証対象のシェアマップ（投資家ID -> シェア比率）
     * @throws BusinessException 以下の場合に発生:
     *                          - シェアマップがnullまたは空の場合（EMPTY_SHARES）
     *                          - シェアが0以下の場合（INVALID_SHARE）
     *                          - 合計が100%でない場合（INVALID_TOTAL_SHARE）
     */
    private void validateShares(Map<Long, BigDecimal> shares) {
        if (shares == null || shares.isEmpty()) {
            throw new BusinessException("Shares cannot be empty", "EMPTY_SHARES");
        }

        shares.values().forEach(share -> {
            if (share.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Share must be positive", "INVALID_SHARE");
            }
        });

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

    /**
     * 特定の投資家のシェアを取得します。
     * 
     * @param sharePieId シェアパイID
     * @param investorId 投資家ID
     * @return 投資家のシェア比率（参加していない場合は0）
     * @throws BusinessException シェアパイが存在しない場合（SHARE_PIE_NOT_FOUND）
     */
    public BigDecimal getInvestorShare(Long sharePieId, Long investorId) {
        return findById(sharePieId)
                .map(dto -> dto.getShares().getOrDefault(investorId, BigDecimal.ZERO))
                .orElseThrow(() -> new BusinessException("SharePie not found", "SHARE_PIE_NOT_FOUND"));
    }

    /**
     * シェア配分を更新します。
     * 新しい配分の妥当性も検証します。
     * 
     * @param sharePieId シェアパイID
     * @param newShares 新しいシェアマップ（投資家ID -> シェア比率）
     * @return 更新されたシェアパイのDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - シェアパイが存在しない場合（SHARE_PIE_NOT_FOUND）
     *                          - 新しいシェアが無効な場合（各種検証エラー）
     */
    @Transactional
    public SharePieDto updateShares(Long sharePieId, Map<Long, BigDecimal> newShares) {
        SharePie sharePie = repository.findById(sharePieId)
                .orElseThrow(() -> new BusinessException("SharePie not found", "SHARE_PIE_NOT_FOUND"));

        validateShares(newShares);
        sharePie.setShares(newShares);

        return toDto(repository.save(sharePie));
    }

    /**
     * 指定された最小シェア以上の比率を持つシェアパイを検索します。
     * 
     * @param minShare 最小シェア比率
     * @return 条件を満たすシェアパイのDTOリスト
     */
    public List<SharePieDto> findByMinimumShare(BigDecimal minShare) {
        return repository.findByMinimumShare(minShare).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された投資家が参加しているシェアパイを検索します。
     * 
     * @param investorId 投資家ID
     * @return 投資家が参加しているシェアパイのDTOリスト
     */
    public List<SharePieDto> findByInvestorId(Long investorId) {
        return repository.findByInvestorId(investorId).stream()
                .map(this::toDto)
                .toList();
    }
}

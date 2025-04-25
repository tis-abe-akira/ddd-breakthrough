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
 * SharePie（シェア配分）に関する操作を提供するサービスクラス。
 * ローンやファシリティにおける投資家の配分比率を管理します。
 * 
 * <p>
 * シェア配分は、投資家IDと配分比率（パーセント値）のマッピングで表され、
 * 常に合計が100%になるよう検証されます。このサービスでは、シェア配分の作成、
 * 更新、取得の他、投資家単位の配分取得や特定条件での検索機能も提供します。
 * </p>
 * 
 * <p>
 * シェア配分はシンジケートローンにおいて重要な概念であり、各投資家がローン
 * やファシリティにどの程度参加しているかを示す指標となります。
 * </p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SharePieService extends AbstractBaseService<SharePie, Long, SharePieDto, SharePieRepository> {

    /**
     * 投資家サービス
     */
    private final InvestorService investorService;

    /**
     * 100パーセントを表す定数
     */
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.0000");

    /**
     * コンストラクタ
     *
     * @param repository      SharePieリポジトリ
     * @param investorService 投資家サービス
     */
    public SharePieService(SharePieRepository repository, InvestorService investorService) {
        super(repository);
        this.investorService = investorService;
    }

    /**
     * エンティティにIDを設定します
     *
     * @param entity エンティティ
     * @param id     設定するID
     */
    @Override
    protected void setEntityId(SharePie entity, Long id) {
        entity.setId(id);
    }

    /**
     * DTOからエンティティへ変換します
     *
     * @param dto 変換するDTO
     * @return 変換されたエンティティ
     * @throws BusinessException シェアのバリデーションに失敗した場合
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
     * エンティティからDTOへ変換します
     *
     * @param entity 変換するエンティティ
     * @return 変換されたDTO
     */
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
            investorService.findById(investorId).ifPresent(investor -> investorShares.put(investor.getName(), share));
        });
        dto.setInvestorShares(investorShares);

        return dto;
    }

    /**
     * シェア配分をバリデーションします
     *
     * @param shares バリデーション対象のシェア配分
     * @throws BusinessException バリデーションに失敗した場合
     */
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
                    "INVALID_TOTAL_SHARE");
        }
    }

    /**
     * 投資家のシェアを取得します
     *
     * @param sharePieId SharePieのID
     * @param investorId 投資家ID
     * @return 投資家のシェア比率
     * @throws BusinessException SharePieが見つからない場合
     */
    public BigDecimal getInvestorShare(Long sharePieId, Long investorId) {
        return findById(sharePieId)
                .map(dto -> dto.getShares().getOrDefault(investorId, BigDecimal.ZERO))
                .orElseThrow(() -> new BusinessException("SharePie not found", "SHARE_PIE_NOT_FOUND"));
    }

    /**
     * シェア配分を更新します
     *
     * @param sharePieId SharePieのID
     * @param newShares  新しいシェア配分
     * @return 更新されたSharePieDTO
     * @throws BusinessException SharePieが見つからない場合、またはバリデーションに失敗した場合
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
     * 最小シェア以上の投資家を含むSharePieを検索します
     *
     * @param minShare 最小シェア比率
     * @return SharePieDTOのリスト
     */
    public List<SharePieDto> findByMinimumShare(BigDecimal minShare) {
        return repository.findByMinimumShare(minShare).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 特定の投資家が参加しているSharePieを検索します
     *
     * @param investorId 投資家ID
     * @return SharePieDTOのリスト
     */
    public List<SharePieDto> findByInvestorId(Long investorId) {
        return repository.findByInvestorId(investorId).stream()
                .map(this::toDto)
                .toList();
    }
}

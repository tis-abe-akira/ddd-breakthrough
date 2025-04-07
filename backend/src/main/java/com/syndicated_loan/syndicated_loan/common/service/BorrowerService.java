package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.BorrowerDto;
import com.syndicated_loan.syndicated_loan.common.entity.Borrower;
import com.syndicated_loan.syndicated_loan.common.repository.BorrowerRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 借入人に関する操作を提供するサービスクラス。
 * 借入人の作成、検索、更新などの機能を実装します。
 */
@Service
public class BorrowerService extends AbstractBaseService<Borrower, Long, BorrowerDto, BorrowerRepository> {

    /**
     * コンストラクタ
     *
     * @param repository 借入人リポジトリ
     */
    public BorrowerService(BorrowerRepository repository) {
        super(repository);
    }

    /**
     * DTOからエンティティへ変換します
     *
     * @param dto 変換するDTO
     * @return 変換されたエンティティ
     */
    @Override
    public Borrower toEntity(BorrowerDto dto) {
        Borrower entity = new Borrower();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setCreditRating(dto.getCreditRating());
        entity.setFinancialStatements(dto.getFinancialStatements());
        entity.setContactInformation(dto.getContactInformation());
        entity.setCompanyType(dto.getCompanyType());
        entity.setIndustry(dto.getIndustry());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    /**
     * エンティティからDTOへ変換します
     *
     * @param entity 変換するエンティティ
     * @return 変換されたDTO
     */
    @Override
    public BorrowerDto toDto(Borrower entity) {
        return BorrowerDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .creditRating(entity.getCreditRating())
                .financialStatements(entity.getFinancialStatements())
                .contactInformation(entity.getContactInformation())
                .companyType(entity.getCompanyType())
                .industry(entity.getIndustry())
                .version(entity.getVersion())
                .build();
    }

    /**
     * エンティティにIDを設定します
     *
     * @param entity エンティティ
     * @param id     設定するID
     */
    @Override
    protected void setEntityId(Borrower entity, Long id) {
        entity.setId(id);
    }

    /**
     * 借入人を更新します。バージョン情報も適切に処理します。
     *
     * @param id  更新対象の借入人ID
     * @param dto 更新情報を含むDTO
     * @return 更新された借入人DTO
     * @throws IllegalArgumentException                    DTOのIDとパスのIDが一致しない場合
     * @throws jakarta.persistence.EntityNotFoundException 借入人が存在しない場合
     */
    @Override
    @Transactional
    public BorrowerDto update(Long id, BorrowerDto dto) {
        // ID確認 - 入力DTOとURLパラメータのIDが一致しない場合はエラー
        if (dto.getId() != null && !dto.getId().equals(id)) {
            throw new IllegalArgumentException("ID mismatch: DTO ID and path ID must be the same");
        }

        // 既存エンティティの取得 - 存在確認
        Borrower existingEntity = repository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Entity not found with id: " + id));

        // DTO→エンティティ変換
        Borrower entity = toEntity(dto);

        // 必須項目の設定 - IDと作成日時は維持
        entity.setId(id);
        entity.setCreatedAt(existingEntity.getCreatedAt());

        // バージョン情報の引き継ぎ（DTOからversionが来ていない場合用）
        if (entity.getVersion() == null) {
            entity.setVersion(existingEntity.getVersion());
        }

        // 保存処理
        Borrower savedEntity = repository.save(entity);

        // エンティティ→DTO変換して返却
        return toDto(savedEntity);
    }

    /**
     * 条件に基づいて借入人を検索します
     *
     * @param name         名前（部分一致）
     * @param creditRating 信用格付け
     * @param industry     業種
     * @return 検索結果の借入人DTOリスト
     */
    @Transactional(readOnly = true)
    public List<BorrowerDto> search(String name, String creditRating, String industry) {
        List<Borrower> results;

        if (name != null && !name.isEmpty()) {
            results = repository.findByNameContaining(name);
        } else if (creditRating != null && !creditRating.isEmpty()) {
            results = repository.findByCreditRating(creditRating);
        } else if (industry != null && !industry.isEmpty()) {
            results = repository.findByIndustry(industry);
        } else {
            results = repository.findAll();
        }

        return results.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

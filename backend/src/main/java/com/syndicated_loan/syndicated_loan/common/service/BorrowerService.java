package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.BorrowerDto;
import com.syndicated_loan.syndicated_loan.common.entity.Borrower;
import com.syndicated_loan.syndicated_loan.common.repository.BorrowerRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowerService extends AbstractBaseService<Borrower, BorrowerDto, Long, BorrowerRepository> {

    public BorrowerService(BorrowerRepository repository) {
        super(repository);
    }

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

    @Override
    protected void setEntityId(Borrower entity, Long id) {
        entity.setId(id);
    }

    /**
     * 更新処理のオーバーライド - バージョン情報を適切に処理
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
     * 借入人の検索機能
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

package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.BorrowerDto;
import com.syndicated_loan.syndicated_loan.common.entity.Borrower;
import com.syndicated_loan.syndicated_loan.common.repository.BorrowerRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * シンジケートローンにおける借入人（Borrower）を管理するサービスクラス。
 * 借入人の基本情報、信用格付け、財務諸表などの管理機能を提供します。
 */
@Service
@Transactional(readOnly = true)
public class BorrowerService extends AbstractBaseService<Borrower, Long, BorrowerDto, BorrowerRepository> {

    /**
     * コンストラクタ
     * 
     * @param repository 借入人リポジトリインスタンス
     */
    public BorrowerService(BorrowerRepository repository) {
        super(repository);
    }

    /**
     * DTOをエンティティに変換します。
     * 借入人の基本情報、信用情報、財務情報などを設定します。
     * 
     * @param dto 変換元のDTO
     * @return 変換された借入人エンティティ
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
     * エンティティをDTOに変換します。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換された借入人DTO
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

    @Override
    protected void setEntityId(Borrower entity, Long id) {
        entity.setId(id);
    }

    /**
     * 指定された条件に基づいて借入人を検索します。
     * 各検索条件はnullの場合、その条件での絞り込みは行われません。
     * 
     * @param name 借入人名（部分一致）
     * @param creditRating 信用格付け（完全一致）
     * @param industry 業種（完全一致）
     * @return 条件に合致する借入人のDTOリスト
     */
    public List<BorrowerDto> search(String name, String creditRating, String industry) {
        return repository.findAll().stream()
                .filter(borrower -> name == null || borrower.getName().contains(name))
                .filter(borrower -> creditRating == null || borrower.getCreditRating().equals(creditRating))
                .filter(borrower -> industry == null || borrower.getIndustry().equals(industry))
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

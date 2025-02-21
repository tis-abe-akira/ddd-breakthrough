package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.SyndicateDto;
import com.syndicated_loan.syndicated_loan.common.entity.Syndicate;
import com.syndicated_loan.syndicated_loan.common.entity.Investor;
import com.syndicated_loan.syndicated_loan.common.repository.SyndicateRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * シンジケートローンにおける投資家団（Syndicate）を管理するサービスクラス。
 * リード銀行とメンバー銀行の管理、総コミットメント額の管理などの機能を提供します。
 * シンジケートの結成や参加メンバーの追加・削除なども行えます。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SyndicateService extends AbstractBaseService<Syndicate, Long, SyndicateDto, SyndicateRepository> {

    private final InvestorService investorService;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param investorService 投資家サービス
     */
    public SyndicateService(SyndicateRepository repository, InvestorService investorService) {
        super(repository);
        this.investorService = investorService;
    }

    @Override
    protected void setEntityId(Syndicate entity, Long id) {
        entity.setId(id);
    }

    /**
     * DTOをエンティティに変換します。
     * リード銀行とメンバー銀行の存在確認も行います。
     * 
     * @param dto 変換元のDTO
     * @return 変換されたシンジケートエンティティ
     * @throws BusinessException 以下の場合に発生:
     *                          - リード銀行が存在しない場合（LEAD_BANK_NOT_FOUND）
     *                          - メンバー銀行が存在しない場合（MEMBER_NOT_FOUND）
     */
    @Override
    public Syndicate toEntity(SyndicateDto dto) {
        Syndicate entity = new Syndicate();
        entity.setId(dto.getId());

        Investor leadBank = investorService.findById(dto.getLeadBankId())
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Lead bank not found", "LEAD_BANK_NOT_FOUND"));
        entity.setLeadBank(leadBank);

        Set<Investor> members = dto.getMemberIds().stream()
                .map(id -> investorService.findById(id)
                        .map(investorService::toEntity)
                        .orElseThrow(() -> new BusinessException("Member not found: " + id, "MEMBER_NOT_FOUND")))
                .collect(Collectors.toSet());
        entity.setMembers(members);

        entity.setTotalCommitment(dto.getTotalCommitment());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    /**
     * エンティティをDTOに変換します。
     * リード銀行とメンバー銀行の詳細情報も設定します。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換されたシンジケートDTO
     */
    @Override
    public SyndicateDto toDto(Syndicate entity) {
        SyndicateDto dto = SyndicateDto.builder()
                .id(entity.getId())
                .leadBankId(entity.getLeadBank().getId())
                .memberIds(entity.getMembers().stream()
                        .map(Investor::getId)
                        .collect(Collectors.toSet()))
                .totalCommitment(entity.getTotalCommitment())
                .version(entity.getVersion())
                .build();

        dto.setLeadBank(investorService.toDto(entity.getLeadBank()));
        dto.setMembers(entity.getMembers().stream()
                .map(investorService::toDto)
                .collect(Collectors.toSet()));

        return dto;
    }

    /**
     * 指定されたリード銀行が主幹事を務めるシンジケートを検索します。
     * 
     * @param leadBankId リード銀行ID
     * @return シンジケートのDTOリスト
     * @throws BusinessException リード銀行が存在しない場合（LEAD_BANK_NOT_FOUND）
     */
    public List<SyndicateDto> findByLeadBank(Long leadBankId) {
        Investor leadBank = investorService.findById(leadBankId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Lead bank not found", "LEAD_BANK_NOT_FOUND"));
        return repository.findByLeadBank(leadBank).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された投資家がメンバーとして参加しているシンジケートを検索します。
     * 
     * @param memberId メンバー銀行ID
     * @return シンジケートのDTOリスト
     * @throws BusinessException メンバー銀行が存在しない場合（MEMBER_NOT_FOUND）
     */
    public List<SyndicateDto> findByMember(Long memberId) {
        Investor member = investorService.findById(memberId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Member not found", "MEMBER_NOT_FOUND"));
        return repository.findByMember(member).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された金額より大きい総コミットメント額を持つシンジケートを検索します。
     * 
     * @param amount 基準となる金額
     * @return 条件を満たすシンジケートのDTOリスト
     */
    public List<SyndicateDto> findByTotalCommitmentGreaterThan(BigDecimal amount) {
        return repository.findByTotalCommitmentGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * シンジケートに新しいメンバーを追加します。
     * 
     * @param syndicateId シンジケートID
     * @param investorId 追加する投資家ID
     * @return 更新されたシンジケートのDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - シンジケートが存在しない場合（SYNDICATE_NOT_FOUND）
     *                          - 投資家が存在しない場合（INVESTOR_NOT_FOUND）
     */
    @Transactional
    public SyndicateDto addMember(Long syndicateId, Long investorId) {
        Syndicate syndicate = repository.findById(syndicateId)
                .orElseThrow(() -> new BusinessException("Syndicate not found", "SYNDICATE_NOT_FOUND"));

        Investor investor = investorService.findById(investorId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Investor not found", "INVESTOR_NOT_FOUND"));

        syndicate.getMembers().add(investor);
        return toDto(repository.save(syndicate));
    }

    /**
     * シンジケートからメンバーを削除します。
     * 
     * @param syndicateId シンジケートID
     * @param investorId 削除する投資家ID
     * @return 更新されたシンジケートのDTO
     * @throws BusinessException シンジケートが存在しない場合（SYNDICATE_NOT_FOUND）
     */
    @Transactional
    public SyndicateDto removeMember(Long syndicateId, Long investorId) {
        Syndicate syndicate = repository.findById(syndicateId)
                .orElseThrow(() -> new BusinessException("Syndicate not found", "SYNDICATE_NOT_FOUND"));

        syndicate.setMembers(syndicate.getMembers().stream()
                .filter(member -> !member.getId().equals(investorId))
                .collect(Collectors.toSet()));

        return toDto(repository.save(syndicate));
    }
}

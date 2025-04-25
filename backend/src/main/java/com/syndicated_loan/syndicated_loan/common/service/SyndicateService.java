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
 * シンジケート団（協調融資団）に関する操作を提供するサービスクラス。
 * シンジケート団の作成、更新、検索、メンバー管理などの機能を実装します。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SyndicateService extends AbstractBaseService<Syndicate, Long, SyndicateDto, SyndicateRepository> {

        /**
         * 投資家サービス
         */
        private final InvestorService investorService;

        /**
         * コンストラクタ
         *
         * @param repository      シンジケートリポジトリ
         * @param investorService 投資家サービス
         */
        public SyndicateService(SyndicateRepository repository, InvestorService investorService) {
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
        protected void setEntityId(Syndicate entity, Long id) {
                entity.setId(id);
        }

        /**
         * DTOからエンティティへ変換します
         *
         * @param dto 変換するDTO
         * @return 変換されたエンティティ
         * @throws BusinessException リード銀行またはメンバーが見つからない場合
         */
        @Override
        public Syndicate toEntity(SyndicateDto dto) {
                Syndicate entity = new Syndicate();
                entity.setId(dto.getId());

                // リード銀行の設定
                Investor leadBank = investorService.findById(dto.getLeadBankId())
                                .map(investorService::toEntity)
                                .orElseThrow(() -> new BusinessException("Lead bank not found", "LEAD_BANK_NOT_FOUND"));
                entity.setLeadBank(leadBank);

                // メンバーの設定
                Set<Investor> members = dto.getMemberIds().stream()
                                .map(id -> investorService.findById(id)
                                                .map(investorService::toEntity)
                                                .orElseThrow(() -> new BusinessException("Member not found: " + id,
                                                                "MEMBER_NOT_FOUND")))
                                .collect(Collectors.toSet());
                entity.setMembers(members);

                entity.setTotalCommitment(dto.getTotalCommitment());
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

                // レスポンス用の追加情報
                dto.setLeadBank(investorService.toDto(entity.getLeadBank()));
                dto.setMembers(entity.getMembers().stream()
                                .map(investorService::toDto)
                                .collect(Collectors.toSet()));

                return dto;
        }

        /**
         * リード銀行IDに基づいてシンジケート団を検索します
         *
         * @param leadBankId リード銀行ID
         * @return シンジケート団DTOのリスト
         * @throws BusinessException リード銀行が見つからない場合
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
         * メンバーIDに基づいてシンジケート団を検索します
         *
         * @param memberId メンバーID
         * @return シンジケート団DTOのリスト
         * @throws BusinessException メンバーが見つからない場合
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
         * 指定された金額より大きいコミットメント総額を持つシンジケート団を検索します
         *
         * @param amount 基準金額
         * @return シンジケート団DTOのリスト
         */
        public List<SyndicateDto> findByTotalCommitmentGreaterThan(BigDecimal amount) {
                return repository.findByTotalCommitmentGreaterThan(amount).stream()
                                .map(this::toDto)
                                .toList();
        }

        /**
         * シンジケート団にメンバーを追加します
         *
         * @param syndicateId シンジケート団ID
         * @param investorId  追加する投資家ID
         * @return 更新されたシンジケート団DTO
         * @throws BusinessException シンジケート団または投資家が見つからない場合
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
         * シンジケート団からメンバーを削除します
         *
         * @param syndicateId シンジケート団ID
         * @param investorId  削除する投資家ID
         * @return 更新されたシンジケート団DTO
         * @throws BusinessException シンジケート団が見つからない場合
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

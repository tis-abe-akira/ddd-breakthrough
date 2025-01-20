package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.SyndicateDto;
import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.entity.Syndicate;
import com.syndicated_loan.syndicated_loan.common.entity.Investor;
import com.syndicated_loan.syndicated_loan.common.repository.SyndicateRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SyndicateService extends AbstractBaseService<Syndicate, Long, SyndicateDto, SyndicateRepository> {

    private final InvestorService investorService;

    public SyndicateService(SyndicateRepository repository, InvestorService investorService) {
        super(repository);
        this.investorService = investorService;
    }

    @Override
    protected void setEntityId(Syndicate entity, Long id) {
        entity.setId(id);
    }

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
                        .orElseThrow(() -> new BusinessException("Member not found: " + id, "MEMBER_NOT_FOUND")))
                .collect(Collectors.toSet());
        entity.setMembers(members);

        entity.setTotalCommitment(dto.getTotalCommitment());
        entity.setVersion(dto.getVersion());
        return entity;
    }

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

    // 追加の検索メソッド
    public List<SyndicateDto> findByLeadBank(Long leadBankId) {
        Investor leadBank = investorService.findById(leadBankId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Lead bank not found", "LEAD_BANK_NOT_FOUND"));
        return repository.findByLeadBank(leadBank).stream()
                .map(this::toDto)
                .toList();
    }

    public List<SyndicateDto> findByMember(Long memberId) {
        Investor member = investorService.findById(memberId)
                .map(investorService::toEntity)
                .orElseThrow(() -> new BusinessException("Member not found", "MEMBER_NOT_FOUND"));
        return repository.findByMember(member).stream()
                .map(this::toDto)
                .toList();
    }

    public List<SyndicateDto> findByTotalCommitmentGreaterThan(BigDecimal amount) {
        return repository.findByTotalCommitmentGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    // メンバー管理
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

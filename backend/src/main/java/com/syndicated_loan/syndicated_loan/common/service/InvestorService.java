package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.entity.Investor;
import com.syndicated_loan.syndicated_loan.common.repository.InvestorRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * シンジケートローンにおける投資家（Investor）を管理するサービスクラス。
 * 投資家の基本情報、投資能力、現在の投資額などの管理機能を提供します。
 * 投資家タイプに基づく検索や投資能力に基づくフィルタリングなども行えます。
 */
@Service
@Transactional(readOnly = true)
public class InvestorService extends AbstractBaseService<Investor, Long, InvestorDto, InvestorRepository> {

    /**
     * コンストラクタ
     * 
     * @param repository 投資家リポジトリインスタンス
     */
    public InvestorService(InvestorRepository repository) {
        super(repository);
    }

    /**
     * DTOをエンティティに変換します。
     * 投資家の基本情報、投資能力、現在の投資額などを設定します。
     * 
     * @param dto 変換元のDTO
     * @return 変換された投資家エンティティ
     */
    @Override
    public Investor toEntity(InvestorDto dto) {
        Investor entity = new Investor();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setInvestmentCapacity(dto.getInvestmentCapacity());
        entity.setCurrentInvestments(dto.getCurrentInvestments());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    /**
     * エンティティをDTOに変換します。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換された投資家DTO
     */
    @Override
    public InvestorDto toDto(Investor entity) {
        return InvestorDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .investmentCapacity(entity.getInvestmentCapacity())
                .currentInvestments(entity.getCurrentInvestments())
                .version(entity.getVersion())
                .build();
    }

    @Override
    protected void setEntityId(Investor entity, Long id) {
        entity.setId(id);
    }

    /**
     * 指定された条件に基づいて投資家を検索します。
     * 各検索条件はnullの場合、その条件での絞り込みは行われません。
     * 
     * @param name 投資家名（部分一致）
     * @param type 投資家タイプ（完全一致）
     * @param minCapacity 最小投資能力
     * @return 条件に合致する投資家のDTOリスト
     */
    public List<InvestorDto> search(String name, String type, BigDecimal minCapacity) {
        return repository.findAll().stream()
                .filter(investor -> name == null || investor.getName().contains(name))
                .filter(investor -> type == null || investor.getType().equals(type))
                .filter(investor -> minCapacity == null ||
                        investor.getInvestmentCapacity().compareTo(minCapacity) >= 0)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 指定された名前の投資家を検索します。
     * 
     * @param name 投資家名（完全一致）
     * @return 投資家のDTO（存在しない場合は空のOptional）
     */
    public Optional<InvestorDto> findByName(String name) {
        return repository.findByName(name).map(this::toDto);
    }

    /**
     * 指定されたパターンを名前に含む投資家を検索します。
     * 
     * @param namePattern 検索パターン
     * @return 条件に合致する投資家のDTOリスト
     */
    public List<InvestorDto> findByNameContaining(String namePattern) {
        return repository.findByNameContaining(namePattern).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 指定されたタイプの投資家を検索します。
     * 
     * @param type 投資家タイプ
     * @return 指定されたタイプの投資家のDTOリスト
     */
    public List<InvestorDto> findByType(String type) {
        return repository.findByType(type).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 指定された金額より大きい投資能力を持つ投資家を検索します。
     * 
     * @param amount 基準となる投資能力
     * @return 条件を満たす投資家のDTOリスト
     */
    public List<InvestorDto> findByInvestmentCapacityGreaterThan(BigDecimal amount) {
        return repository.findByInvestmentCapacityGreaterThan(amount).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 指定された金額より少ない現在の投資額を持つ投資家を検索します。
     * 
     * @param amount 基準となる投資額
     * @return 条件を満たす投資家のDTOリスト
     */
    public List<InvestorDto> findByCurrentInvestmentsLessThan(BigDecimal amount) {
        return repository.findByCurrentInvestmentsLessThan(amount).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 指定されたタイプかつ指定された金額より大きい投資能力を持つ投資家を検索します。
     * 
     * @param type 投資家タイプ
     * @param amount 基準となる投資能力
     * @return 条件を満たす投資家のDTOリスト
     */
    public List<InvestorDto> findByTypeAndInvestmentCapacityGreaterThan(String type, BigDecimal amount) {
        return repository.findByTypeAndInvestmentCapacityGreaterThan(type, amount).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

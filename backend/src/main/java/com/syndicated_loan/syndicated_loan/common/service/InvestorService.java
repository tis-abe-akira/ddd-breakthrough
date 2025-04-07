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
 * 投資家に関する操作を提供するサービスクラス。
 * 投資家の作成、検索、更新などの機能を実装します。
 */
@Service
@Transactional(readOnly = true)
public class InvestorService extends AbstractBaseService<Investor, Long, InvestorDto, InvestorRepository> {

    /**
     * コンストラクタ
     *
     * @param repository 投資家リポジトリ
     */
    public InvestorService(InvestorRepository repository) {
        super(repository);
    }

    /**
     * DTOからエンティティへ変換します
     *
     * @param dto 変換するDTO
     * @return 変換されたエンティティ
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
     * エンティティからDTOへ変換します
     *
     * @param entity 変換するエンティティ
     * @return 変換されたDTO
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

    /**
     * エンティティにIDを設定します
     *
     * @param entity エンティティ
     * @param id     設定するID
     */
    @Override
    protected void setEntityId(Investor entity, Long id) {
        entity.setId(id);
    }

    /**
     * 条件に基づいて投資家を検索します
     *
     * @param name        名前（部分一致）
     * @param type        投資家タイプ
     * @param minCapacity 最小投資能力
     * @return 検索結果の投資家DTOリスト
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
     * 名前で投資家を検索します
     *
     * @param name 投資家名
     * @return 投資家DTO（存在しない場合はEmpty）
     */
    public Optional<InvestorDto> findByName(String name) {
        return repository.findByName(name).map(this::toDto);
    }

    /**
     * 名前の一部を含む投資家を検索します
     *
     * @param namePattern 検索パターン
     * @return 投資家DTOのリスト
     */
    public List<InvestorDto> findByNameContaining(String namePattern) {
        return repository.findByNameContaining(namePattern).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * タイプで投資家を検索します
     *
     * @param type 投資家タイプ
     * @return 投資家DTOのリスト
     */
    public List<InvestorDto> findByType(String type) {
        return repository.findByType(type).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 指定した金額より大きい投資能力を持つ投資家を検索します
     *
     * @param amount 基準金額
     * @return 投資家DTOのリスト
     */
    public List<InvestorDto> findByInvestmentCapacityGreaterThan(BigDecimal amount) {
        return repository.findByInvestmentCapacityGreaterThan(amount).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 指定した金額より少ない現在の投資額を持つ投資家を検索します
     *
     * @param amount 基準金額
     * @return 投資家DTOのリスト
     */
    public List<InvestorDto> findByCurrentInvestmentsLessThan(BigDecimal amount) {
        return repository.findByCurrentInvestmentsLessThan(amount).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * タイプと投資能力の条件に合致する投資家を検索します
     *
     * @param type   投資家タイプ
     * @param amount 最小投資能力
     * @return 投資家DTOのリスト
     */
    public List<InvestorDto> findByTypeAndInvestmentCapacityGreaterThan(String type, BigDecimal amount) {
        return repository.findByTypeAndInvestmentCapacityGreaterThan(type, amount).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

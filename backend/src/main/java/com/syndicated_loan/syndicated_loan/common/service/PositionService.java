package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.FacilityDto;
import com.syndicated_loan.syndicated_loan.common.dto.LoanDto;
import com.syndicated_loan.syndicated_loan.common.entity.Position;
import com.syndicated_loan.syndicated_loan.common.entity.Facility;
import com.syndicated_loan.syndicated_loan.common.entity.Loan;
import com.syndicated_loan.syndicated_loan.common.repository.PositionRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * シンジケートローンにおけるポジション（Position）を管理するサービスクラス。
 * ファシリティやローンなど、異なる種類のポジションを統一的に管理します。
 * 型安全な方法でポジションの変換と取得を提供します。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class PositionService extends AbstractBaseService<Position, Long, Object, PositionRepository> {

    private final FacilityService facilityService;
    private final LoanService loanService;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param facilityService ファシリティサービス
     * @param loanService ローンサービス
     */
    public PositionService(
            PositionRepository repository,
            FacilityService facilityService,
            LoanService loanService) {
        super(repository);
        this.facilityService = facilityService;
        this.loanService = loanService;
    }

    @Override
    protected void setEntityId(Position entity, Long id) {
        entity.setId(id);
    }

    /**
     * DTOをエンティティに変換します。
     * DTOの型に応じて適切なサービスに変換を委譲します。
     * 
     * @param dto 変換元のDTO（FacilityDtoまたはLoanDto）
     * @return 変換されたポジションエンティティ
     * @throws BusinessException サポートされていないDTO型の場合（INVALID_DTO_TYPE）
     */
    @Override
    public Position toEntity(Object dto) {
        if (dto instanceof FacilityDto) {
            return facilityService.toEntity((FacilityDto) dto);
        } else if (dto instanceof LoanDto) {
            return loanService.toEntity((LoanDto) dto);
        }
        throw new BusinessException("Unsupported DTO type", "INVALID_DTO_TYPE");
    }

    /**
     * エンティティをDTOに変換します。
     * エンティティの型に応じて適切なサービスに変換を委譲します。
     * 
     * @param entity 変換元のエンティティ（FacilityまたはLoan）
     * @return 変換されたDTO
     * @throws BusinessException サポートされていないエンティティ型の場合（INVALID_ENTITY_TYPE）
     */
    @Override
    public Object toDto(Position entity) {
        if (entity instanceof Facility) {
            return facilityService.toDto((Facility) entity);
        } else if (entity instanceof Loan) {
            return loanService.toDto((Loan) entity);
        }
        throw new BusinessException("Unsupported entity type", "INVALID_ENTITY_TYPE");
    }

    /**
     * IDを指定してファシリティを検索します。
     * 該当のポジションがファシリティでない場合は空のOptionalを返します。
     * 
     * @param id ポジションID
     * @return ファシリティのDTO（存在しないか、ファシリティでない場合は空のOptional）
     */
    public Optional<FacilityDto> findFacilityById(Long id) {
        return repository.findById(id)
                .filter(position -> position instanceof Facility)
                .map(position -> facilityService.toDto((Facility) position));
    }

    /**
     * IDを指定してローンを検索します。
     * 該当のポジションがローンでない場合は空のOptionalを返します。
     * 
     * @param id ポジションID
     * @return ローンのDTO（存在しないか、ローンでない場合は空のOptional）
     */
    public Optional<LoanDto> findLoanById(Long id) {
        return repository.findById(id)
                .filter(position -> position instanceof Loan)
                .map(position -> loanService.toDto((Loan) position));
    }
}

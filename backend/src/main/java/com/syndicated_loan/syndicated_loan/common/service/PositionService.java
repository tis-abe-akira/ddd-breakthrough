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
 * ポジション（融資ポジション）に関する操作を提供するサービスクラス。
 * ファシリティとローンを共通のポジションとして扱うための処理を実装します。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class PositionService extends AbstractBaseService<Position, Long, Object, PositionRepository> {

    /**
     * ファシリティサービス
     */
    private final FacilityService facilityService;

    /**
     * ローンサービス
     */
    private final LoanService loanService;

    /**
     * コンストラクタ
     *
     * @param repository      ポジションリポジトリ
     * @param facilityService ファシリティサービス
     * @param loanService     ローンサービス
     */
    public PositionService(
            PositionRepository repository,
            FacilityService facilityService,
            LoanService loanService) {
        super(repository);
        this.facilityService = facilityService;
        this.loanService = loanService;
    }

    /**
     * エンティティにIDを設定します
     *
     * @param entity エンティティ
     * @param id     設定するID
     */
    @Override
    protected void setEntityId(Position entity, Long id) {
        entity.setId(id);
    }

    /**
     * DTOからエンティティへ変換します。
     * DTOの型に応じて適切なサービスの変換メソッドを使用します。
     *
     * @param dto 変換するDTO（FacilityDtoまたはLoanDto）
     * @return 変換されたエンティティ
     * @throws BusinessException サポートされていないDTO型の場合
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
     * エンティティからDTOへ変換します。
     * エンティティの型に応じて適切なサービスの変換メソッドを使用します。
     *
     * @param entity 変換するエンティティ（FacilityまたはLoan）
     * @return 変換されたDTO
     * @throws BusinessException サポートされていないエンティティ型の場合
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
     * IDを指定してファシリティを検索します
     *
     * @param id ポジションID
     * @return ファシリティDTO（存在しない場合はEmpty）
     */
    public Optional<FacilityDto> findFacilityById(Long id) {
        return repository.findById(id)
                .filter(position -> position instanceof Facility)
                .map(position -> facilityService.toDto((Facility) position));
    }

    /**
     * IDを指定してローンを検索します
     *
     * @param id ポジションID
     * @return ローンDTO（存在しない場合はEmpty）
     */
    public Optional<LoanDto> findLoanById(Long id) {
        return repository.findById(id)
                .filter(position -> position instanceof Loan)
                .map(position -> loanService.toDto((Loan) position));
    }
}

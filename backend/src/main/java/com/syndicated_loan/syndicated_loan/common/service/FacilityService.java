package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.FacilityDto;
import com.syndicated_loan.syndicated_loan.common.dto.SharePieDto;
import com.syndicated_loan.syndicated_loan.common.entity.Facility;
import com.syndicated_loan.syndicated_loan.common.entity.SharePie;
import com.syndicated_loan.syndicated_loan.common.entity.Syndicate;
import com.syndicated_loan.syndicated_loan.common.entity.Borrower;
import com.syndicated_loan.syndicated_loan.common.repository.FacilityRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * シンジケートローンにおけるファシリティ（与信枠）を管理するサービスクラス。
 * ファシリティの作成、更新、利用可能額の管理、シェア配分の設定などの機能を提供します。
 * 借入人、シンジケート団との関連付けも管理します。
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class FacilityService extends AbstractBaseService<Facility, Long, FacilityDto, FacilityRepository> {

    private final SyndicateService syndicateService;
    private final SharePieService sharePieService;
    private final BorrowerService borrowerService;

    /**
     * コンストラクタ
     * 
     * @param repository リポジトリインスタンス
     * @param syndicateService シンジケート団サービス
     * @param sharePieService シェア配分サービス
     * @param borrowerService 借入人サービス
     */
    public FacilityService(
            FacilityRepository repository,
            SyndicateService syndicateService,
            SharePieService sharePieService,
            BorrowerService borrowerService) {
        super(repository);
        this.syndicateService = syndicateService;
        this.sharePieService = sharePieService;
        this.borrowerService = borrowerService;
    }

    @Override
    protected void setEntityId(Facility entity, Long id) {
        entity.setId(id);
    }

    /**
     * DTOをエンティティに変換します。
     * 必須項目のバリデーション、関連エンティティの設定、
     * 既定値の設定なども行います。
     * 
     * @param dto 変換元のDTO
     * @return 変換されたファシリティエンティティ
     * @throws BusinessException 以下の場合に発生:
     *                          - 必須項目が未設定の場合（各種_REQUIRED）
     *                          - 関連エンティティが存在しない場合（各種_NOT_FOUND）
     */
    @Override
    public Facility toEntity(FacilityDto dto) {
        Facility entity = new Facility();
        entity.setId(dto.getId());
        entity.setType("FACILITY");

        if (dto.getTotalAmount() == null) {
            throw new BusinessException("Total amount cannot be null", "TOTAL_AMOUNT_REQUIRED");
        }
        entity.setAmount(dto.getTotalAmount());
        entity.setTotalAmount(dto.getTotalAmount());

        if (dto.getAvailableAmount() == null) {
            entity.setAvailableAmount(dto.getTotalAmount());
        } else {
            entity.setAvailableAmount(dto.getAvailableAmount());
        }

        if (dto.getStartDate() == null) {
            entity.setStartDate(LocalDate.now());
        } else {
            entity.setStartDate(dto.getStartDate());
        }

        if (dto.getTerm() == null) {
            throw new BusinessException("Term cannot be null", "TERM_REQUIRED");
        }
        entity.setTerm(dto.getTerm());
        entity.setEndDate(entity.getStartDate().plusMonths(dto.getTerm()));

        if (dto.getInterestRate() == null) {
            throw new BusinessException("Interest rate cannot be null", "INTEREST_RATE_REQUIRED");
        }
        entity.setInterestRate(dto.getInterestRate());

        Syndicate syndicate = syndicateService.findById(dto.getSyndicateId())
                .map(syndicateService::toEntity)
                .orElseThrow(() -> new BusinessException("Syndicate not found", "SYNDICATE_NOT_FOUND"));
        entity.setSyndicate(syndicate);

        if (dto.getSharePieId() != null) {
            SharePie sharePie = sharePieService.getRepository().getReferenceById(dto.getSharePieId());
            entity.setSharePie(sharePie);
        }

        if (dto.getBorrowerId() == null && dto.getId() != null) {
            Facility existingFacility = repository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));
            dto.setBorrowerId(existingFacility.getBorrower().getId());
        }

        if (dto.getBorrowerId() == null) {
            throw new BusinessException("Borrower ID cannot be null", "BORROWER_REQUIRED");
        }
        Borrower borrower = borrowerService.findById(dto.getBorrowerId())
                .map(borrowerService::toEntity)
                .orElseThrow(() -> new BusinessException("Borrower not found", "BORROWER_NOT_FOUND"));
        entity.setBorrower(borrower);

        entity.setVersion(dto.getVersion());
        return entity;
    }

    /**
     * エンティティをDTOに変換します。
     * シンジケート団情報、シェア配分情報、利用率なども設定します。
     * 
     * @param entity 変換元のエンティティ
     * @return 変換されたファシリティDTO
     */
    @Override
    public FacilityDto toDto(Facility entity) {
        FacilityDto dto = FacilityDto.builder()
                .id(entity.getId())
                .totalAmount(entity.getTotalAmount())
                .availableAmount(entity.getAvailableAmount())
                .startDate(entity.getStartDate())
                .term(entity.getTerm())
                .endDate(entity.getEndDate())
                .interestRate(entity.getInterestRate())
                .syndicateId(entity.getSyndicate().getId())
                .sharePieId(entity.getSharePie() != null ? entity.getSharePie().getId() : null)
                .version(entity.getVersion())
                .build();

        dto.setSyndicate(syndicateService.toDto(entity.getSyndicate()));
        if (entity.getSharePie() != null) {
            dto.setSharePie(sharePieService.toDto(entity.getSharePie()));
        }

        if (entity.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilizationRate = entity.getAvailableAmount()
                    .divide(entity.getTotalAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            dto.setUtilizationRate(utilizationRate);
        }

        return dto;
    }

    /**
     * 指定されたシンジケート団に関連する全てのファシリティを検索します。
     * 
     * @param syndicateId シンジケート団ID
     * @return ファシリティのDTOリスト
     * @throws BusinessException シンジケート団が存在しない場合（SYNDICATE_NOT_FOUND）
     */
    public List<FacilityDto> findBySyndicate(Long syndicateId) {
        Syndicate syndicate = syndicateService.findById(syndicateId)
                .map(syndicateService::toEntity)
                .orElseThrow(() -> new BusinessException("Syndicate not found", "SYNDICATE_NOT_FOUND"));
        return repository.findBySyndicate(syndicate).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された総額より大きいファシリティを検索します。
     * 
     * @param amount 基準となる金額
     * @return 条件を満たすファシリティのDTOリスト
     */
    public List<FacilityDto> findByTotalAmountGreaterThan(BigDecimal amount) {
        return repository.findByTotalAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された日付より後に終了するファシリティを検索します。
     * 
     * @param date 基準となる日付
     * @return 条件を満たすファシリティのDTOリスト
     */
    public List<FacilityDto> findByEndDateAfter(LocalDate date) {
        return repository.findByEndDateAfter(date).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 指定された金額より大きい利用可能額を持つファシリティを検索します。
     * 
     * @param amount 基準となる金額
     * @return 条件を満たすファシリティのDTOリスト
     */
    public List<FacilityDto> findByAvailableAmountGreaterThan(BigDecimal amount) {
        return repository.findByAvailableAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * ファシリティの利用可能額を更新します。
     * 
     * @param facilityId ファシリティID
     * @param newAvailableAmount 新しい利用可能額
     * @return 更新されたファシリティのDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - ファシリティが存在しない場合（FACILITY_NOT_FOUND）
     *                          - 利用可能額が負の値の場合（INVALID_AVAILABLE_AMOUNT）
     *                          - 利用可能額が総額を超える場合（INVALID_AVAILABLE_AMOUNT）
     */
    @Transactional
    public FacilityDto updateAvailableAmount(Long facilityId, BigDecimal newAvailableAmount) {
        Facility facility = repository.findById(facilityId)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));

        if (newAvailableAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Available amount cannot be negative", "INVALID_AVAILABLE_AMOUNT");
        }

        if (newAvailableAmount.compareTo(facility.getTotalAmount()) > 0) {
            throw new BusinessException("Available amount cannot exceed total amount", "INVALID_AVAILABLE_AMOUNT");
        }

        facility.setAvailableAmount(newAvailableAmount);
        return toDto(repository.save(facility));
    }

    /**
     * ファシリティのシェア配分を更新します。
     * 
     * @param facilityId ファシリティID
     * @param sharePieDto 新しいシェア配分DTO
     * @return 更新されたファシリティのDTO
     * @throws BusinessException 以下の場合に発生:
     *                          - ファシリティが存在しない場合（FACILITY_NOT_FOUND）
     *                          - シェア配分が存在しない場合（SHARE_PIE_NOT_FOUND）
     */
    @Transactional
    public FacilityDto updateSharePie(Long facilityId, SharePieDto sharePieDto) {
        Facility facility = repository.findById(facilityId)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));

        SharePie sharePie;
        if (sharePieDto.getId() != null) {
            sharePie = sharePieService.findById(sharePieDto.getId())
                    .map(dto -> {
                        SharePie entity = sharePieService.toEntity(sharePieDto);
                        entity.setId(dto.getId());
                        return sharePieService.getRepository().save(entity);
                    })
                    .orElseThrow(() -> new BusinessException("SharePie not found", "SHARE_PIE_NOT_FOUND"));
        } else {
            sharePie = sharePieService.toEntity(sharePieDto);
            sharePie = sharePieService.getRepository().save(sharePie);
        }

        facility.setSharePie(sharePie);
        return toDto(repository.save(facility));
    }
}

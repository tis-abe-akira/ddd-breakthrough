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

@Slf4j
@Service
@Transactional(readOnly = true)
public class FacilityService extends AbstractBaseService<Facility, Long, FacilityDto, FacilityRepository> {

    private final SyndicateService syndicateService;
    private final SharePieService sharePieService;
    private final BorrowerService borrowerService;

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

    @Override
    public Facility toEntity(FacilityDto dto) {
        Facility entity = new Facility();
        entity.setId(dto.getId());
        entity.setType("FACILITY");

        // 必須項目のバリデーション
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

        // 日付関連の設定
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

        // シンジケート団の設定
        Syndicate syndicate = syndicateService.findById(dto.getSyndicateId())
                .map(syndicateService::toEntity)
                .orElseThrow(() -> new BusinessException("Syndicate not found", "SYNDICATE_NOT_FOUND"));
        entity.setSyndicate(syndicate);

        // シェアパイの設定
        if (dto.getSharePieId() != null) {
            SharePie sharePie = sharePieService.getRepository().findById(dto.getSharePieId())
                    .orElseThrow(() -> new BusinessException("SharePie not found", "SHARE_PIE_NOT_FOUND"));
            entity.setSharePie(sharePie);
        }

        // borrowerの設定
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

        // レスポンス用の追加情報
        dto.setSyndicate(syndicateService.toDto(entity.getSyndicate()));
        if (entity.getSharePie() != null) {
            dto.setSharePie(sharePieService.toDto(entity.getSharePie()));
        }

        // 利用率の計算
        if (entity.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal utilizationRate = entity.getAvailableAmount()
                    .divide(entity.getTotalAmount(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            dto.setUtilizationRate(utilizationRate);
        }

        return dto;
    }

    // 追加の検索メソッド
    public List<FacilityDto> findBySyndicate(Long syndicateId) {
        Syndicate syndicate = syndicateService.findById(syndicateId)
                .map(syndicateService::toEntity)
                .orElseThrow(() -> new BusinessException("Syndicate not found", "SYNDICATE_NOT_FOUND"));
        return repository.findBySyndicate(syndicate).stream()
                .map(this::toDto)
                .toList();
    }

    public List<FacilityDto> findByTotalAmountGreaterThan(BigDecimal amount) {
        return repository.findByTotalAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    public List<FacilityDto> findByEndDateAfter(LocalDate date) {
        return repository.findByEndDateAfter(date).stream()
                .map(this::toDto)
                .toList();
    }

    public List<FacilityDto> findByAvailableAmountGreaterThan(BigDecimal amount) {
        return repository.findByAvailableAmountGreaterThan(amount).stream()
                .map(this::toDto)
                .toList();
    }

    // ファシリティの利用可能額を更新
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

    // シェアパイの更新
    @Transactional
    public FacilityDto updateSharePie(Long facilityId, SharePieDto sharePieDto) {
        Facility facility = repository.findById(facilityId)
                .orElseThrow(() -> new BusinessException("Facility not found", "FACILITY_NOT_FOUND"));

        SharePie sharePie = sharePieService.toEntity(sharePieDto);
        facility.setSharePie(sharePie);

        return toDto(repository.save(facility));
    }
}

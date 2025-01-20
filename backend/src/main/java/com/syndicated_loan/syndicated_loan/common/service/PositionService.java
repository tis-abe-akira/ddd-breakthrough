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

@Slf4j
@Service
@Transactional(readOnly = true)
public class PositionService extends AbstractBaseService<Position, Long, Object, PositionRepository> {

    private final FacilityService facilityService;
    private final LoanService loanService;

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

    @Override
    public Position toEntity(Object dto) {
        if (dto instanceof FacilityDto) {
            return facilityService.toEntity((FacilityDto) dto);
        } else if (dto instanceof LoanDto) {
            return loanService.toEntity((LoanDto) dto);
        }
        throw new BusinessException("Unsupported DTO type", "INVALID_DTO_TYPE");
    }

    @Override
    public Object toDto(Position entity) {
        if (entity instanceof Facility) {
            return facilityService.toDto((Facility) entity);
        } else if (entity instanceof Loan) {
            return loanService.toDto((Loan) entity);
        }
        throw new BusinessException("Unsupported entity type", "INVALID_ENTITY_TYPE");
    }

    // 型を指定して取得するメソッド
    public Optional<FacilityDto> findFacilityById(Long id) {
        return repository.findById(id)
                .filter(position -> position instanceof Facility)
                .map(position -> facilityService.toDto((Facility) position));
    }

    public Optional<LoanDto> findLoanById(Long id) {
        return repository.findById(id)
                .filter(position -> position instanceof Loan)
                .map(position -> loanService.toDto((Loan) position));
    }
}

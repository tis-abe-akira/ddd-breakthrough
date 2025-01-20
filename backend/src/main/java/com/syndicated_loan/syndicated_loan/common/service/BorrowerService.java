package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.BorrowerDto;
import com.syndicated_loan.syndicated_loan.common.entity.Borrower;
import com.syndicated_loan.syndicated_loan.common.repository.BorrowerRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BorrowerService extends AbstractBaseService<Borrower, Long, BorrowerDto, BorrowerRepository> {

    public BorrowerService(BorrowerRepository repository) {
        super(repository);
    }

    @Override
    public Borrower toEntity(BorrowerDto dto) {
        Borrower entity = new Borrower();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setCreditRating(dto.getCreditRating());
        entity.setFinancialStatements(dto.getFinancialStatements());
        entity.setContactInformation(dto.getContactInformation());
        entity.setCompanyType(dto.getCompanyType());
        entity.setIndustry(dto.getIndustry());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    @Override
    public BorrowerDto toDto(Borrower entity) {
        return BorrowerDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .creditRating(entity.getCreditRating())
                .financialStatements(entity.getFinancialStatements())
                .contactInformation(entity.getContactInformation())
                .companyType(entity.getCompanyType())
                .industry(entity.getIndustry())
                .version(entity.getVersion())
                .build();
    }

    @Override
    protected void setEntityId(Borrower entity, Long id) {
        entity.setId(id);
    }

    public List<BorrowerDto> search(String name, String creditRating, String industry) {
        return repository.findAll().stream()
                .filter(borrower -> name == null || borrower.getName().contains(name))
                .filter(borrower -> creditRating == null || borrower.getCreditRating().equals(creditRating))
                .filter(borrower -> industry == null || borrower.getIndustry().equals(industry))
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

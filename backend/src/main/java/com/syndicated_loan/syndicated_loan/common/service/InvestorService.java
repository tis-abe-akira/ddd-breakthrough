package com.syndicated_loan.syndicated_loan.common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.entity.Investor;
import com.syndicated_loan.syndicated_loan.common.repository.InvestorRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InvestorService extends AbstractBaseService<Investor, Long, InvestorDto, InvestorRepository> {

    public InvestorService(InvestorRepository repository) {
        super(repository);
    }

    @Override
    public Investor toEntity(InvestorDto dto) {
        Investor entity = new Investor();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setInvestmentCapacity(dto.getInvestmentCapacity());
        entity.setVersion(dto.getVersion());
        return entity;
    }

    @Override
    public InvestorDto toDto(Investor entity) {
        return InvestorDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .investmentCapacity(entity.getInvestmentCapacity())
                .version(entity.getVersion())
                .build();
    }

    @Override
    protected void setEntityId(Investor entity, Long id) {
        entity.setId(id);
    }

    public List<InvestorDto> search(String name, String type, BigDecimal minCapacity) {
        return repository.findAll().stream()
                .filter(investor -> name == null || investor.getName().contains(name))
                .filter(investor -> type == null || investor.getType().equals(type))
                .filter(investor -> minCapacity == null || 
                        investor.getInvestmentCapacity().compareTo(minCapacity) >= 0)
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

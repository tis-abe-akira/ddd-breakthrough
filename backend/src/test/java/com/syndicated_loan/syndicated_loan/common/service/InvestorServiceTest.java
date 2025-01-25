package com.syndicated_loan.syndicated_loan.common.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.repository.InvestorRepository;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
public class InvestorServiceTest {

    @Autowired
    private InvestorService investorService;

    @Autowired
    private InvestorRepository investorRepository;

    private InvestorDto savedInvestor;

    @BeforeEach
    void setUp() {
        investorRepository.deleteAll();

        InvestorDto investor1 = InvestorDto.builder()
                .name("Investor1")
                .type("Type1")
                .investmentCapacity(BigDecimal.valueOf(1000))
                .currentInvestments(BigDecimal.valueOf(500))
                .version(1L)
                .build();
        savedInvestor = investorService.create(investor1);

        InvestorDto investor2 = InvestorDto.builder()
                .name("Investor2")
                .type("Type2")
                .investmentCapacity(BigDecimal.valueOf(2000))
                .currentInvestments(BigDecimal.valueOf(1000))
                .version(1L)
                .build();
        investorService.create(investor2);
    }

    @Test
    void testFindAll() {
        List<InvestorDto> investors = investorService.findAll();

        assertThat(investors).hasSize(2);

        InvestorDto investor1 = investors.get(0);
        assertThat(investor1.getName()).isEqualTo("Investor1");
        assertThat(investor1.getType()).isEqualTo("Type1");
        assertThat(investor1.getInvestmentCapacity()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(investor1.getCurrentInvestments()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(investor1.getVersion()).isEqualTo(1L);

        InvestorDto investor2 = investors.get(1);
        assertThat(investor2.getName()).isEqualTo("Investor2");
        assertThat(investor2.getType()).isEqualTo("Type2");
        assertThat(investor2.getInvestmentCapacity()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(investor2.getCurrentInvestments()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(investor2.getVersion()).isEqualTo(1L);
    }

    @Test
    void testFindById() {
        Optional<InvestorDto> investorOpt = investorService.findById(savedInvestor.getId());
        assertThat(investorOpt).isPresent();
        InvestorDto investor = investorOpt.get();

        assertThat(investor.getName()).isEqualTo("Investor1");
        assertThat(investor.getType()).isEqualTo("Type1");
        assertThat(investor.getInvestmentCapacity()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(investor.getCurrentInvestments()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(investor.getVersion()).isEqualTo(1L);
    }

    @Test
    void testCreate() {
        InvestorDto investor = InvestorDto.builder()
                .name("Investor3")
                .type("Type3")
                .investmentCapacity(BigDecimal.valueOf(3000))
                .currentInvestments(BigDecimal.valueOf(1500))
                .version(1L)
                .build();
        InvestorDto createdInvestor = investorService.create(investor);

        assertThat(createdInvestor.getId()).isNotNull();
        assertThat(createdInvestor.getName()).isEqualTo("Investor3");
        assertThat(createdInvestor.getType()).isEqualTo("Type3");
        assertThat(createdInvestor.getInvestmentCapacity()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(createdInvestor.getCurrentInvestments()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(createdInvestor.getVersion()).isEqualTo(1L);

        Optional<InvestorDto> savedInvestorOpt = investorService.findById(createdInvestor.getId());
        assertThat(savedInvestorOpt).isPresent();
        InvestorDto savedInvestor = savedInvestorOpt.get();
        assertThat(savedInvestor.getName()).isEqualTo("Investor3");
        assertThat(savedInvestor.getType()).isEqualTo("Type3");
        assertThat(savedInvestor.getInvestmentCapacity()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(savedInvestor.getCurrentInvestments()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(savedInvestor.getVersion()).isEqualTo(1L);
    }

    @Test
    void testUpdate() {
        savedInvestor.setName("UpdatedInvestor");
        savedInvestor.setType("UpdatedType");
        savedInvestor.setInvestmentCapacity(BigDecimal.valueOf(4000));
        savedInvestor.setCurrentInvestments(BigDecimal.valueOf(2000));

        InvestorDto updatedInvestor = investorService.update(savedInvestor.getId(), savedInvestor);

        assertThat(updatedInvestor.getId()).isNotNull();
        assertThat(updatedInvestor.getName()).isEqualTo("UpdatedInvestor");
        assertThat(updatedInvestor.getType()).isEqualTo("UpdatedType");
        assertThat(updatedInvestor.getInvestmentCapacity()).isEqualByComparingTo(BigDecimal.valueOf(4000));
        assertThat(updatedInvestor.getCurrentInvestments()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(updatedInvestor.getVersion()).isEqualTo(1L);

        Optional<InvestorDto> savedInvestorOpt = investorService.findById(updatedInvestor.getId());
        assertThat(savedInvestorOpt).isPresent();
        InvestorDto savedInvestor = savedInvestorOpt.get();
        assertThat(savedInvestor.getName()).isEqualTo("UpdatedInvestor");
        assertThat(savedInvestor.getType()).isEqualTo("UpdatedType");
        assertThat(savedInvestor.getInvestmentCapacity()).isEqualByComparingTo(BigDecimal.valueOf(4000));
        assertThat(savedInvestor.getCurrentInvestments()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(savedInvestor.getVersion()).isEqualTo(2L);
    }

    @Test
    void testDelete() {
        assertThatCode(() -> investorService.delete(savedInvestor.getId())).doesNotThrowAnyException();

        assertThat(investorService.findById(savedInvestor.getId())).isEmpty();
    }

    @Test
    void testDeleteNotFound() {
        assertThatThrownBy(() -> investorService.delete(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Entity not found with id: 999");
    }

    @Test
    void testSearch() {
        List<InvestorDto> investors = investorService.search("Investor1", "Type1", BigDecimal.valueOf(500));
        assertThat(investors).hasSize(1);

        InvestorDto investor = investors.get(0);
        assertThat(investor.getName()).isEqualTo("Investor1");
        assertThat(investor.getType()).isEqualTo("Type1");
        assertThat(investor.getInvestmentCapacity()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(investor.getCurrentInvestments()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(investor.getVersion()).isEqualTo(1L);
    }

    @Test
    void testFindByName() {
        Optional<InvestorDto> investor = investorService.findByName("Investor1");
        assertThat(investor).isPresent();
        assertThat(investor.get().getName()).isEqualTo("Investor1");
        assertThat(investor.get().getType()).isEqualTo("Type1");
        assertThat(investor.get().getInvestmentCapacity()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void testFindByNameContaining() {
        List<InvestorDto> investors = investorService.findByNameContaining("Investor");
        assertThat(investors).hasSize(2);
    }

    @Test
    void testFindByType() {
        List<InvestorDto> investors = investorService.findByType("Type1");
        assertThat(investors).hasSize(1);
        assertThat(investors.get(0).getName()).isEqualTo("Investor1");
    }

    @Test
    void testFindByInvestmentCapacityGreaterThan() {
        List<InvestorDto> investors = investorService.findByInvestmentCapacityGreaterThan(BigDecimal.valueOf(1500));
        assertThat(investors).hasSize(1);
        assertThat(investors.get(0).getName()).isEqualTo("Investor2");
    }

    @Test
    void testFindByCurrentInvestmentsLessThan() {
        List<InvestorDto> investors = investorService.findByCurrentInvestmentsLessThan(BigDecimal.valueOf(800));
        assertThat(investors).hasSize(1);
        assertThat(investors.get(0).getName()).isEqualTo("Investor1");
    }

    @Test
    void testFindByTypeAndInvestmentCapacityGreaterThan() {
        List<InvestorDto> investors = investorService.findByTypeAndInvestmentCapacityGreaterThan(
            "Type2", BigDecimal.valueOf(1500));
        assertThat(investors).hasSize(1);
        assertThat(investors.get(0).getName()).isEqualTo("Investor2");
    }
}

package com.syndicated_loan.syndicated_loan.common.service;

import com.syndicated_loan.syndicated_loan.common.dto.BorrowerDto;
import com.syndicated_loan.syndicated_loan.common.entity.Borrower;
import com.syndicated_loan.syndicated_loan.common.repository.BorrowerRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private BorrowerService borrowerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateBorrower() {
        BorrowerDto dto = new BorrowerDto();
        dto.setName("Test Borrower");
        dto.setCompanyType("LLC");
        dto.setIndustry("Finance");

        Borrower borrower = new Borrower();
        borrower.setId(1L);
        borrower.setName("Test Borrower");
        borrower.setCompanyType("LLC");
        borrower.setIndustry("Finance");

        when(borrowerRepository.save(any(Borrower.class))).thenReturn(borrower);

        BorrowerDto createdDto = borrowerService.create(dto);

        assertThat(createdDto).isNotNull();
        assertThat(createdDto.getName()).isEqualTo("Test Borrower");
        assertThat(createdDto.getCompanyType()).isEqualTo("LLC");
        assertThat(createdDto.getIndustry()).isEqualTo("Finance");
    }

    @Test
    void testFindById() {
        Borrower borrower = new Borrower();
        borrower.setId(1L);
        borrower.setName("Test Borrower");

        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));

        Optional<BorrowerDto> foundDto = borrowerService.findById(1L);

        assertThat(foundDto).isPresent();
        assertThat(foundDto.get().getName()).isEqualTo("Test Borrower");
    }

    @Test
    void testUpdateBorrower() {
        BorrowerDto dto = new BorrowerDto();
        dto.setId(1L);
        dto.setName("Updated Borrower");
        dto.setCompanyType("LLC");
        dto.setIndustry("Finance");

        Borrower borrower = new Borrower();
        borrower.setId(1L);
        borrower.setName("Test Borrower");
        borrower.setCompanyType("LLC");
        borrower.setIndustry("Finance");

        when(borrowerRepository.existsById(1L)).thenReturn(true);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(borrower);

        BorrowerDto updatedDto = borrowerService.update(1L, dto);

        assertThat(updatedDto).isNotNull();
        assertThat(updatedDto.getName()).isEqualTo("Updated Borrower");
    }

    @Test
    void testDeleteBorrower() {
        when(borrowerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(borrowerRepository).deleteById(1L);

        assertThatCode(() -> borrowerService.delete(1L)).doesNotThrowAnyException();
        verify(borrowerRepository, times(1)).deleteById(1L);
    }

    @Test
    void testSearchBorrowers() {
        Borrower borrower = new Borrower();
        borrower.setId(1L);
        borrower.setName("Test Borrower");
        borrower.setCompanyType("LLC");
        borrower.setIndustry("Finance");

        when(borrowerRepository.findAll()).thenReturn(List.of(borrower));

        List<BorrowerDto> foundDtos = borrowerService.search("Test", null, null);

        assertThat(foundDtos).isNotNull();
        assertThat(foundDtos).isNotEmpty();
        assertThat(foundDtos.get(0).getName()).isEqualTo("Test Borrower");
    }

    @Test
    void testCreateBorrowerValidation() {
        BorrowerDto dto = new BorrowerDto();
        dto.setName("Test Borrower");

        assertThatThrownBy(() -> borrowerService.create(dto))
                .isInstanceOf(BusinessException.class);
    }
}

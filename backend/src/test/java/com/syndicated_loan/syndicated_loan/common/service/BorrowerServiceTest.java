package com.syndicated_loan.syndicated_loan.common.service;

import com.syndicated_loan.syndicated_loan.common.dto.BorrowerDto;
import com.syndicated_loan.syndicated_loan.common.repository.BorrowerRepository;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class BorrowerServiceTest {

    @Autowired
    private BorrowerService borrowerService;

    @Autowired
    private BorrowerRepository borrowerRepository;

    private BorrowerDto savedBorrower;  // テストデータを保持する変数を追加

    @BeforeEach
    void setUp() {
        borrowerRepository.deleteAll();

        BorrowerDto borrowerDto = new BorrowerDto();
        borrowerDto.setCompanyType("Test Company Type");
        borrowerDto.setIndustry("Test Industry");
        borrowerDto.setName("Test Borrower");
        savedBorrower = borrowerService.create(borrowerDto);  // 作成したデータを保持
    }

    @Test
    void testFindAll() {
        List<BorrowerDto> borrowers = borrowerService.findAll();
        assertThat(borrowers).hasSize(1);
        BorrowerDto borrower = borrowers.get(0);
        assertThat(borrower.getCompanyType()).isEqualTo("Test Company Type");
        assertThat(borrower.getIndustry()).isEqualTo("Test Industry");
        assertThat(borrower.getName()).isEqualTo("Test Borrower");
    }

    @Test
    void testFindById() {
        Optional<BorrowerDto> borrowerOpt = borrowerService.findById(savedBorrower.getId());  // 保存したIDを使用
        assertThat(borrowerOpt).isPresent();
        BorrowerDto borrower = borrowerOpt.get();
        assertThat(borrower.getCompanyType()).isEqualTo("Test Company Type");
        assertThat(borrower.getIndustry()).isEqualTo("Test Industry");
        assertThat(borrower.getName()).isEqualTo("Test Borrower");
    }

    @Test
    void testCreate() {
        BorrowerDto borrowerDto = new BorrowerDto();
        borrowerDto.setCompanyType("New Company Type");
        borrowerDto.setIndustry("New Industry");
        borrowerDto.setName("New Borrower");

        BorrowerDto createdBorrower = borrowerService.create(borrowerDto);
        
        assertThat(createdBorrower).isNotNull();
        assertThat(createdBorrower.getCompanyType()).isEqualTo("New Company Type");
        assertThat(createdBorrower.getIndustry()).isEqualTo("New Industry");
        assertThat(createdBorrower.getName()).isEqualTo("New Borrower");

        // データが実際にDBに保存されているか確認
        Optional<BorrowerDto> savedBorrower = borrowerService.findById(createdBorrower.getId());
        assertThat(savedBorrower).isPresent();
        assertThat(savedBorrower.get().getCompanyType()).isEqualTo("New Company Type");
        assertThat(savedBorrower.get().getIndustry()).isEqualTo("New Industry");
        assertThat(savedBorrower.get().getName()).isEqualTo("New Borrower");
    }

    @Test
    void testDelete() {
        assertThatCode(() -> borrowerService.delete(savedBorrower.getId())).doesNotThrowAnyException();  // 保存したIDを使用
        
        // 削除後の確認を追加
        assertThat(borrowerService.findById(savedBorrower.getId())).isEmpty();
    }

    @Test
    void testDeleteNotFound() {
        assertThatThrownBy(() -> borrowerService.delete(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Entity not found with id: 999");
    }
}

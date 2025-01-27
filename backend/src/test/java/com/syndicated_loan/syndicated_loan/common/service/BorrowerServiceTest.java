package com.syndicated_loan.syndicated_loan.common.service;

import com.syndicated_loan.syndicated_loan.common.dto.BorrowerDto;
import com.syndicated_loan.syndicated_loan.common.repository.BorrowerRepository;
import com.syndicated_loan.syndicated_loan.common.testutil.TestDataBuilder;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class BorrowerServiceTest {

    @Autowired
    private TestDataBuilder testDataBuilder;
    
    @Autowired
    private BorrowerService borrowerService;

    // @Autowired
    // private BorrowerRepository borrowerRepository;

    private BorrowerDto borrower1;  // テストデータを保持する変数を追加
    private BorrowerDto borrower2;  // テストデータを保持する変数を追加

    @BeforeEach
    void setUp() {
        // borrowerRepository.deleteAll();

        // // 1件目のテストデータ
        // BorrowerDto borrower1 = new BorrowerDto();
        // borrower1.setCompanyType("株式会社");
        // borrower1.setIndustry("製造業");
        // borrower1.setName("テスト製造株式会社1");
        // borrower1.setCreditRating("A+");
        // borrower1.setFinancialStatements("直近3年間の財務諸表データ");
        // borrower1.setContactInformation("担当者: テスト1\nTEL: 03-0000-0001\nEmail: test1@example.com");
        // savedBorrower = borrowerService.create(borrower1);

        // // 2件目のテストデータ
        // BorrowerDto borrower2 = new BorrowerDto();
        // borrower2.setCompanyType("有限会社");
        // borrower2.setIndustry("小売業");
        // borrower2.setName("テスト商事2");
        // borrower2.setCreditRating("BB+");
        // borrower2.setFinancialStatements("2024年度第3四半期決算報告");
        // borrower2.setContactInformation("担当者: テスト2\nTEL: 03-0000-0002");
        // borrowerService.create(borrower2);

        Map<String, Object> testData = testDataBuilder.getTestDataForBorrower();
        borrower1 = (BorrowerDto) testData.get("borrower1");
        borrower2 = (BorrowerDto) testData.get("borrower2");
    }

    @Test
    void testFindAll() {
        List<BorrowerDto> borrowers = borrowerService.findAll();
        
        // 件数の検証
        assertThat(borrowers).hasSize(2);
        
        // 1件目のデータ検証
        BorrowerDto firstBorrower = borrowers.get(0);
        assertThat(firstBorrower.getCompanyType()).isEqualTo("CORPORATION");
        assertThat(firstBorrower.getIndustry()).isEqualTo("Technology");
        assertThat(firstBorrower.getName()).isEqualTo("Intel Corporation");
        assertThat(firstBorrower.getCreditRating()).isEqualTo("A+");
        assertThat(firstBorrower.getFinancialStatements()).isEqualTo("testFinancialStatements");
        assertThat(firstBorrower.getContactInformation()).contains("testContactInformation");

        // 2件目のデータ検証
        BorrowerDto secondBorrower = borrowers.get(1);
        assertThat(secondBorrower.getCompanyType()).isEqualTo("CORPORATION");
        assertThat(secondBorrower.getIndustry()).isEqualTo("Technology");
        assertThat(secondBorrower.getName()).isEqualTo("TestBorrower2");
        assertThat(secondBorrower.getCreditRating()).isEqualTo("B+");
        assertThat(secondBorrower.getFinancialStatements()).isEqualTo("testFinancialStatements2");
        assertThat(secondBorrower.getContactInformation()).contains("testContactInformation2");
    }

    @Test
    void testFindById() {
        Optional<BorrowerDto> borrowerOpt = borrowerService.findById(borrower1.getId());  // 保存したIDを使用
        assertThat(borrowerOpt).isPresent();
        BorrowerDto borrower = borrowerOpt.get();
        // 全項目の検証
        assertThat(borrower.getCompanyType()).isEqualTo("CORPORATION");
        assertThat(borrower.getIndustry()).isEqualTo("Technology");
        assertThat(borrower.getName()).isEqualTo("Intel Corporation");
        assertThat(borrower.getCreditRating()).isEqualTo("A+");
        assertThat(borrower.getFinancialStatements()).isEqualTo("testFinancialStatements");
        assertThat(borrower.getContactInformation()).contains("testContactInformation");
    }

    @Test
    void testCreate() {
        BorrowerDto borrowerDto = new BorrowerDto();
        borrowerDto.setCompanyType("有限会社");
        borrowerDto.setIndustry("小売業");
        borrowerDto.setName("テスト商事3");
        borrowerDto.setCreditRating("BB+");
        borrowerDto.setFinancialStatements("2024年度第3四半期決算報告");
        borrowerDto.setContactInformation("担当者: テスト3\nTEL: 03-0000-0003");

        BorrowerDto createdBorrower = borrowerService.create(borrowerDto);
        
        // 全項目の検証
        assertThat(createdBorrower).isNotNull();
        assertThat(createdBorrower.getCompanyType()).isEqualTo("有限会社");
        assertThat(createdBorrower.getIndustry()).isEqualTo("小売業");
        assertThat(createdBorrower.getName()).isEqualTo("テスト商事3");
        assertThat(createdBorrower.getCreditRating()).isEqualTo("BB+");
        assertThat(createdBorrower.getFinancialStatements()).isEqualTo("2024年度第3四半期決算報告");
        assertThat(createdBorrower.getContactInformation()).isEqualTo("担当者: テスト3\nTEL: 03-0000-0003");

        // DBに保存されたデータの検証
        Optional<BorrowerDto> savedBorrower = borrowerService.findById(createdBorrower.getId());
        assertThat(savedBorrower).isPresent();
        assertThat(savedBorrower.get().getCompanyType()).isEqualTo("有限会社");
        assertThat(savedBorrower.get().getIndustry()).isEqualTo("小売業");
        assertThat(savedBorrower.get().getName()).isEqualTo("テスト商事3");
        assertThat(savedBorrower.get().getCreditRating()).isEqualTo("BB+");
        assertThat(savedBorrower.get().getFinancialStatements()).isEqualTo("2024年度第3四半期決算報告");
        assertThat(savedBorrower.get().getContactInformation()).isEqualTo("担当者: テスト3\nTEL: 03-0000-0003");
    }

    @Test
    void testDelete() {
        assertThatCode(() -> borrowerService.delete(borrower1.getId())).doesNotThrowAnyException();  // 保存したIDを使用
        
        // 削除後の確認を追加
        assertThat(borrowerService.findById(borrower1.getId())).isEmpty();
    }

    @Test
    void testDeleteNotFound() {
        assertThatThrownBy(() -> borrowerService.delete(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Entity not found with id: 999");
    }
}

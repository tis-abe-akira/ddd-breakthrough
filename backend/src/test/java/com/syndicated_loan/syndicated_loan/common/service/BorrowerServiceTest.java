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
        borrowerDto.setCompanyType("株式会社");  // より実践的な値に変更
        borrowerDto.setIndustry("製造業");
        borrowerDto.setName("テスト製造株式会社");
        borrowerDto.setCreditRating("A+");  // 信用格付けを追加
        borrowerDto.setFinancialStatements("直近3年間の財務諸表データ");  // 財務情報を追加
        borrowerDto.setContactInformation("担当: 山田太郎\nTEL: 03-1234-5678\nEmail: yamada@test.com");  // 連絡先を追加
        savedBorrower = borrowerService.create(borrowerDto);
    }

    @Test
    void testFindAll() {
        List<BorrowerDto> borrowers = borrowerService.findAll();
        assertThat(borrowers).hasSize(1);
        BorrowerDto borrower = borrowers.get(0);
        // 全項目の検証
        assertThat(borrower.getCompanyType()).isEqualTo("株式会社");
        assertThat(borrower.getIndustry()).isEqualTo("製造業");
        assertThat(borrower.getName()).isEqualTo("テスト製造株式会社");
        assertThat(borrower.getCreditRating()).isEqualTo("A+");
        assertThat(borrower.getFinancialStatements()).isEqualTo("直近3年間の財務諸表データ");
        assertThat(borrower.getContactInformation()).contains("担当: 山田太郎");
    }

    @Test
    void testFindById() {
        Optional<BorrowerDto> borrowerOpt = borrowerService.findById(savedBorrower.getId());  // 保存したIDを使用
        assertThat(borrowerOpt).isPresent();
        BorrowerDto borrower = borrowerOpt.get();
        // 全項目の検証
        assertThat(borrower.getCompanyType()).isEqualTo("株式会社");
        assertThat(borrower.getIndustry()).isEqualTo("製造業");
        assertThat(borrower.getName()).isEqualTo("テスト製造株式会社");
        assertThat(borrower.getCreditRating()).isEqualTo("A+");
        assertThat(borrower.getFinancialStatements()).isEqualTo("直近3年間の財務諸表データ");
        assertThat(borrower.getContactInformation()).contains("担当: 山田太郎");
    }

    @Test
    void testCreate() {
        BorrowerDto borrowerDto = new BorrowerDto();
        borrowerDto.setCompanyType("有限会社");
        borrowerDto.setIndustry("小売業");
        borrowerDto.setName("テスト商事");
        borrowerDto.setCreditRating("BB+");
        borrowerDto.setFinancialStatements("2024年度第3四半期決算報告");
        borrowerDto.setContactInformation("担当: 鈴木花子\nTEL: 03-9876-5432");

        BorrowerDto createdBorrower = borrowerService.create(borrowerDto);
        
        // 全項目の検証
        assertThat(createdBorrower).isNotNull();
        assertThat(createdBorrower.getCompanyType()).isEqualTo("有限会社");
        assertThat(createdBorrower.getIndustry()).isEqualTo("小売業");
        assertThat(createdBorrower.getName()).isEqualTo("テスト商事");
        assertThat(createdBorrower.getCreditRating()).isEqualTo("BB+");
        assertThat(createdBorrower.getFinancialStatements()).isEqualTo("2024年度第3四半期決算報告");
        assertThat(createdBorrower.getContactInformation()).isEqualTo("担当: 鈴木花子\nTEL: 03-9876-5432");

        // DBに保存されたデータの検証
        Optional<BorrowerDto> savedBorrower = borrowerService.findById(createdBorrower.getId());
        assertThat(savedBorrower).isPresent();
        assertThat(savedBorrower.get().getCompanyType()).isEqualTo("有限会社");
        assertThat(savedBorrower.get().getIndustry()).isEqualTo("小売業");
        assertThat(savedBorrower.get().getName()).isEqualTo("テスト商事");
        assertThat(savedBorrower.get().getCreditRating()).isEqualTo("BB+");
        assertThat(savedBorrower.get().getFinancialStatements()).isEqualTo("2024年度第3四半期決算報告");
        assertThat(savedBorrower.get().getContactInformation()).isEqualTo("担当: 鈴木花子\nTEL: 03-9876-5432");
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

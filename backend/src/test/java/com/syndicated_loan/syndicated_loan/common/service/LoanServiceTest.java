package com.syndicated_loan.syndicated_loan.common.service;

import com.syndicated_loan.syndicated_loan.common.dto.AmountPieDto;
import com.syndicated_loan.syndicated_loan.common.dto.BorrowerDto;
import com.syndicated_loan.syndicated_loan.common.dto.DrawdownDto;
import com.syndicated_loan.syndicated_loan.common.dto.FacilityDto;
import com.syndicated_loan.syndicated_loan.common.dto.FacilityInvestmentDto;
import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.dto.LoanDto;
import com.syndicated_loan.syndicated_loan.common.dto.SharePieDto;
import com.syndicated_loan.syndicated_loan.common.testutil.TestDataBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
public class LoanServiceTest {

    @Autowired
    private LoanService loanService;

    @Autowired
    private TestDataBuilder testDataBuilder;

    // @Autowired
    // private InvestorService investorService;

    @Autowired
    private SharePieService sharePieService; // 追加

    private BorrowerDto borrower;
    private InvestorDto leadBank1;

    private InvestorDto member1;

    private FacilityDto savedFacility1;
    private FacilityDto savedFacility2;

    private FacilityInvestmentDto savedFacilityInvestment1;
    private FacilityInvestmentDto savedFacilityInvestment2;

    private SharePieDto savedSharePie;

    @BeforeEach
    void setUp() {

        Map<String, Object> testData = testDataBuilder.getTestDataForDrawdown();

        borrower = (BorrowerDto) testData.get("borrower");
        leadBank1 = (InvestorDto) testData.get("leadBank1");
        member1 = (InvestorDto) testData.get("member1");
        savedFacility1 = (FacilityDto) testData.get("facility1");
        savedFacility2 = (FacilityDto) testData.get("facility2");
        savedFacilityInvestment1 = (FacilityInvestmentDto) testData.get("facilityInvestment1");
        savedFacilityInvestment2 = (FacilityInvestmentDto) testData.get("facilityInvestment2");
        savedSharePie = (SharePieDto) testData.get("sharePie1");

        System.out.println("leadBank1: " + leadBank1);
        System.out.println("member1: " + member1);
        System.out.println("savedFacility1: " + savedFacility1);
        System.out.println("savedFacility2: " + savedFacility2);
        System.out.println("savedFacilityInvestment1: " + savedFacilityInvestment1);
        System.out.println("savedFacilityInvestment2: " + savedFacilityInvestment2);
        System.out.println("savedSharePie: " + savedSharePie);

    }

    @AfterEach
    void tearDown() {
        testDataBuilder.cleanupAll();
    }

    @Test
    void testCreate() {
        System.out.println("testCreate");
        
        // SharePieの状態を確認
        SharePieDto refreshedSharePie = sharePieService.findById(savedSharePie.getId())
            .orElseThrow(() -> new RuntimeException("SharePie not found"));
        System.out.println("refreshedSharePie: " + refreshedSharePie);
        
        LoanDto dto = new LoanDto();
        dto.setAmount(BigDecimal.valueOf(2000000));
        dto.setTotalAmount(BigDecimal.valueOf(2000000));
        dto.setStartDate(LocalDate.of(2025, 1, 31));
        dto.setEndDate(LocalDate.of(2030, 1, 31));
        dto.setInterestRate(BigDecimal.valueOf(2.5));
        dto.setBorrowerId(borrower.getId());
        dto.setFacilityId(savedFacility1.getId());
        dto.setSharePieId(refreshedSharePie.getId());

        System.out.println("dto: " + dto);
        
        LoanDto savedLoanDto = loanService.create(dto);
        
        // 検証を強化
        assertThat(savedLoanDto.getId()).isNotNull();
        assertThat(savedLoanDto.getTotalAmount()).isEqualByComparingTo("2000000");
        assertThat(savedLoanDto.getSharePieId()).isEqualTo(refreshedSharePie.getId());
    }
}

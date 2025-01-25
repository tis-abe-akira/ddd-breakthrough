package com.syndicated_loan.syndicated_loan.common.service;

import com.syndicated_loan.syndicated_loan.common.dto.AmountPieDto;
import com.syndicated_loan.syndicated_loan.common.dto.BorrowerDto;
import com.syndicated_loan.syndicated_loan.common.dto.DrawdownDto;
import com.syndicated_loan.syndicated_loan.common.dto.FacilityDto;
import com.syndicated_loan.syndicated_loan.common.dto.FacilityInvestmentDto;
import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.dto.SharePieDto;
import com.syndicated_loan.syndicated_loan.common.dto.SyndicateDto;
import com.syndicated_loan.syndicated_loan.common.repository.BorrowerRepository;
import com.syndicated_loan.syndicated_loan.common.repository.DrawdownRepository;
import com.syndicated_loan.syndicated_loan.common.repository.FacilityInvestmentRepository;
import com.syndicated_loan.syndicated_loan.common.repository.FacilityRepository;
import com.syndicated_loan.syndicated_loan.common.repository.InvestorRepository;
import com.syndicated_loan.syndicated_loan.common.repository.SharePieRepository;
import com.syndicated_loan.syndicated_loan.common.repository.SyndicateRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

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
public class DrawdownServiceTest {

    @Autowired
    private BorrowerService borrowerService;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private InvestorService investorService;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private SyndicateService syndicateService;

    @Autowired
    private SyndicateRepository syndicateRepository;

    @Autowired
    private SharePieService sharePieService;

    @Autowired
    private SharePieRepository sharePieRepository;

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private FacilityInvestmentService facilityInvestmentService;

    @Autowired
    private FacilityInvestmentRepository facilityInvestmentRepository;

    @Autowired
    private DrawdownService drawdownService;

    @Autowired
    private DrawdownRepository drawdownRepository;


    private BorrowerDto savedBorrower;

    private InvestorDto leadBank1;

    private InvestorDto leadBank2;

    private InvestorDto member1;

    private InvestorDto member2;

    private SyndicateDto savedSyndicate;

    private SharePieDto savedSharePie;

    private DrawdownDto savedDrawdown;

    @BeforeEach
    void setUp() {

        drawdownRepository.deleteAll();
        facilityInvestmentRepository.deleteAll();
        facilityRepository.deleteAll();
        sharePieRepository.deleteAll();
        syndicateRepository.deleteAll();
        investorRepository.deleteAll();
        borrowerRepository.deleteAll();

        // 1件目のBorrowerテストデータ
        BorrowerDto borrower1 = new BorrowerDto();
        borrower1.setCompanyType("CORPORATION");
        borrower1.setIndustry("Technology");
        borrower1.setName("Intel Corporatio");
        borrower1.setCreditRating("A+");
        borrower1.setFinancialStatements("testFinancialStatements");
        borrower1.setContactInformation("testContactInformation");
        savedBorrower = borrowerService.create(borrower1);

        // 2件目のBorrowerテストデータ
        BorrowerDto borrower2 = new BorrowerDto();
        borrower2.setCompanyType("有限会社");
        borrower2.setIndustry("小売業");
        borrower2.setName("テスト商事2");
        borrower2.setCreditRating("BB+");
        borrower2.setFinancialStatements("2024年度第3四半期決算報告");
        borrower2.setContactInformation("担当者: テスト2\nTEL: 03-0000-0002");
        borrowerService.create(borrower2);
        
        // Investorのテストデータを作成
        leadBank1 = investorService.create(InvestorDto.builder()
                .name("リード銀行1")
                .type("銀行")
                .investmentCapacity(BigDecimal.valueOf(10000000))
                .currentInvestments(BigDecimal.valueOf(0))
                .version(1L)
                .build());

        leadBank2 = investorService.create(InvestorDto.builder()
                .name("リード銀行2")
                .type("銀行")
                .investmentCapacity(BigDecimal.valueOf(20000000))
                .currentInvestments(BigDecimal.valueOf(8000000))
                .version(1L)
                .build());

        member1 = investorService.create(InvestorDto.builder()
                .name("メンバー銀行1")
                .type("銀行")
                .investmentCapacity(BigDecimal.valueOf(10000000))
                .currentInvestments(BigDecimal.valueOf(0))
                .version(1L)
                .build());

        member2 = investorService.create(InvestorDto.builder()
                .name("メンバー銀行2")
                .type("銀行")
                .investmentCapacity(BigDecimal.valueOf(20000000))
                .currentInvestments(BigDecimal.valueOf(0))
                .version(1L)
                .build());

        // シンジケートのテストデータを作成
        SyndicateDto syndicate1 = new SyndicateDto();
        syndicate1.setLeadBankId(leadBank1.getId()); // ちゃんとしたIDを設定
        syndicate1.setMemberIds(Set.of(member1.getId(), member2.getId()));
        syndicate1.setTotalCommitment(new BigDecimal("5000000"));
        syndicate1.setVersion(1L);
        savedSyndicate = syndicateService.create(syndicate1);

        SyndicateDto syndicate2 = new SyndicateDto();
        syndicate2.setLeadBankId(leadBank2.getId()); // ちゃんとしたIDを設定
        syndicate2.setMemberIds(Set.of(member1.getId(), leadBank1.getId()));
        syndicate2.setTotalCommitment(new BigDecimal("2000000"));
        syndicate2.setVersion(1L);
        syndicateService.create(syndicate2);

        // 1件目のSharePieテストデータ
        SharePieDto sharePie1 = new SharePieDto();
        sharePie1.setShares(new HashMap<>());
        sharePie1.getShares().put(1L, new BigDecimal("30.0000"));
        sharePie1.getShares().put(2L, new BigDecimal("70.0000"));
        savedSharePie = sharePieService.create(sharePie1);

        // 2件目のSharePieテストデータ
        SharePieDto sharePie2 = new SharePieDto();
        sharePie2.setShares(new HashMap<>());
        sharePie2.getShares().put(leadBank1.getId(), new BigDecimal("50.0000"));
        sharePie2.getShares().put(member1.getId(), new BigDecimal("50.0000"));
        SharePieDto savedSharePie2 = sharePieService.create(sharePie2);  // ← 保存して変数に入れる

        // FacilityのIDを保存して使うように修正
        FacilityDto facility1 = new FacilityDto();
        facility1.setTotalAmount(new BigDecimal("5000000"));
        facility1.setAvailableAmount(new BigDecimal("5000000"));
        facility1.setStartDate(LocalDate.of(2025, 1, 27));
        facility1.setTerm(60);
        facility1.setInterestRate(new BigDecimal("2.5"));
        facility1.setSyndicateId(savedSyndicate.getId());  // ← 保存したIDを使用
        facility1.setSharePieId(savedSharePie.getId());    // ← 保存したIDを使用
        facility1.setBorrowerId(savedBorrower.getId());    // ← 保存したIDを使用
        FacilityDto savedFacility1 = facilityService.create(facility1);

        // 1件目のFacilityInvestmentテストデータ
        FacilityInvestmentDto facilityInvestment1 = new FacilityInvestmentDto();
        facilityInvestment1.setInvestorId(leadBank1.getId());  // ← 保存したIDを使用
        facilityInvestment1.setDate(LocalDateTime.of(2025, 1, 28, 10, 0, 0));
        facilityInvestment1.setRelatedPositionId(1L);
        FacilityInvestmentDto savedFacilityInvestment1 = facilityInvestmentService.create(facilityInvestment1);

        // 1件目のDrawdownテストデータ
        DrawdownDto drawdown1 = new DrawdownDto();
        drawdown1.setRelatedFacilityId(savedFacility1.getId());  // ← 保存したIDを使用
        drawdown1.setDrawdownAmount(new BigDecimal("1000000"));
        drawdown1.setDate(LocalDateTime.of(2025, 1, 20, 0, 0, 0));
        drawdown1.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId());  // ← これを追加！
        savedDrawdown = drawdownService.create(drawdown1);

        // 2件目のFacility作成
        FacilityDto facility2 = new FacilityDto();
        // ... facility2の設定
        facility2.setTotalAmount(new BigDecimal("3000000"));
        facility2.setAvailableAmount(new BigDecimal("3000000"));
        facility2.setStartDate(LocalDate.of(2025, 2, 1));
        facility2.setTerm(48);
        facility2.setInterestRate(new BigDecimal("2.8"));
        facility2.setSyndicateId(savedSyndicate.getId());
        facility2.setSharePieId(savedSharePie2.getId());  // ← 2つ目のSharePieを使用
        facility2.setBorrowerId(savedBorrower.getId());
        FacilityDto savedFacility2 = facilityService.create(facility2);

        // 2件目のDrawdownテストデータ
        DrawdownDto drawdown2 = new DrawdownDto();
        drawdown2.setRelatedFacilityId(savedFacility2.getId());  // ← 保存したIDを使用
        drawdown2.setDrawdownAmount(new BigDecimal("2000000"));
        drawdown2.setDate(LocalDateTime.of(2025, 2, 20, 0, 0, 0));
        drawdown2.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId());  // ← これも追加！
        drawdownService.create(drawdown2);

    }

    @Test
    void testFindAll() {
        List<DrawdownDto> drawdowns = drawdownService.findAll();
        
        // 件数の検証
        assertThat(drawdowns).hasSize(2);
        
        // 1件目のデータ検証
        DrawdownDto firstDrawdown = drawdowns.get(0);
        assertThat(firstDrawdown.getAmount()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(firstDrawdown.getRelatedFacilityId()).isEqualTo(1L);
        assertThat(firstDrawdown.getDate()).isEqualTo(LocalDateTime.of(2025, 1, 20, 0, 0, 0));

        // 2件目のデータ検証
        DrawdownDto secondDrawdown = drawdowns.get(1);
        assertThat(secondDrawdown.getAmount()).isEqualByComparingTo(new BigDecimal("2000000"));
        assertThat(secondDrawdown.getRelatedFacilityId()).isEqualTo(2L);
        assertThat(secondDrawdown.getDate()).isEqualTo(LocalDateTime.of(2025, 2, 20, 0, 0, 0));
    }


}

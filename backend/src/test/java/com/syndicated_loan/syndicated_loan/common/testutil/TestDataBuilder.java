package com.syndicated_loan.syndicated_loan.common.testutil;

import com.syndicated_loan.syndicated_loan.common.dto.*;
import com.syndicated_loan.syndicated_loan.common.service.*;
import com.syndicated_loan.syndicated_loan.common.repository.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TestDataBuilder {
    private final BorrowerService borrowerService;
    private final InvestorService investorService;
    private final SyndicateService syndicateService;
    private final SharePieService sharePieService;
    private final FacilityService facilityService;
    private final FacilityInvestmentService facilityInvestmentService;
    private final DrawdownRepository drawdownRepository;
    private final FacilityInvestmentRepository facilityInvestmentRepository;
    private final FacilityRepository facilityRepository;
    private final PositionRepository positionRepository;
    private final SharePieRepository sharePieRepository;
    private final SyndicateRepository syndicateRepository;
    private final InvestorRepository investorRepository;
    private final BorrowerRepository borrowerRepository;

    public void cleanupAll() {
        // 外部キー制約を考慮した削除順序
        drawdownRepository.deleteAll();
        facilityInvestmentRepository.deleteAll();
        facilityRepository.deleteAll();
        positionRepository.deleteAll();
        sharePieRepository.deleteAll();
        syndicateRepository.deleteAll();
        investorRepository.deleteAll();
        borrowerRepository.deleteAll();
    }

    public TestData createBasicTestData() {
        cleanupAll();

        // Borrowerの作成
        BorrowerDto borrower = createBorrower();

        // Investorの作成
        InvestorDto leadBank1 = createLeadBank1();
        InvestorDto leadBank2 = createLeadBank2();
        InvestorDto member1 = createMember1();
        InvestorDto member2 = createMember2();

        // Syndicateの作成
        SyndicateDto syndicate1 = createSyndicate1(leadBank1, member1, member2);
        SyndicateDto syndicate2 = createSyndicate2(leadBank2, member1, leadBank1);

        // SharePieの作成
        SharePieDto sharePie1 = createSharePie1(leadBank1, member1);
        SharePieDto sharePie2 = createSharePie2(leadBank2, member2);

        // Facilityの作成
        FacilityDto facility1 = createFacility1(syndicate1, sharePie1, borrower);
        FacilityDto facility2 = createFacility2(syndicate2, sharePie2, borrower);

        // FacilityInvestmentの作成
        FacilityInvestmentDto facilityInvestment1 = createFacilityInvestment1(leadBank1, facility1);
        FacilityInvestmentDto facilityInvestment2 = createFacilityInvestment2(leadBank2, facility2);

        return new TestData(borrower, leadBank1, leadBank2, member1, member2,
                syndicate1, syndicate2, sharePie1, sharePie2,
                facility1, facility2, facilityInvestment1, facilityInvestment2);
    }

    // 以下、各エンティティの作成メソッド...
    private BorrowerDto createBorrower() {
        return borrowerService.create(BorrowerDto.builder()
                .companyType("CORPORATION")
                .industry("Technology")
                .name("Intel Corporation")
                .creditRating("A+")
                .financialStatements("testFinancialStatements")
                .contactInformation("testContactInformation")
                .build());
    }

    private InvestorDto createLeadBank1() {
        return investorService.create(InvestorDto.builder()
                .name("リード銀行1")
                .type("銀行")
                .investmentCapacity(BigDecimal.valueOf(10000000))
                .currentInvestments(BigDecimal.valueOf(0))
                .version(1L)
                .build());
    }

    private InvestorDto createLeadBank2() {
        return investorService.create(InvestorDto.builder()
                .name("リード銀行2")
                .type("銀行")
                .investmentCapacity(BigDecimal.valueOf(20000000))
                .currentInvestments(BigDecimal.valueOf(8000000))
                .version(1L)
                .build());
    }

    private InvestorDto createMember1() {
        return investorService.create(InvestorDto.builder()
                .name("メンバー銀行1")
                .type("銀行")
                .investmentCapacity(BigDecimal.valueOf(10000000))
                .currentInvestments(BigDecimal.valueOf(0))
                .version(1L)
                .build());
    }

    private InvestorDto createMember2() {
        return investorService.create(InvestorDto.builder()
                .name("メンバー銀行2")
                .type("銀行")
                .investmentCapacity(BigDecimal.valueOf(20000000))
                .currentInvestments(BigDecimal.valueOf(0))
                .version(1L)
                .build());
    }

    private SyndicateDto createSyndicate1(InvestorDto leadBank, InvestorDto member1, InvestorDto member2) {
        return syndicateService.create(SyndicateDto.builder()
                .leadBankId(leadBank.getId())
                .memberIds(Set.of(member1.getId(), member2.getId()))
                .totalCommitment(BigDecimal.valueOf(5000000))
                .version(1L)
                .build());
    }

    private SyndicateDto createSyndicate2(InvestorDto leadBank, InvestorDto member1, InvestorDto leadBank1) {
        return syndicateService.create(SyndicateDto.builder()
                .leadBankId(leadBank.getId())
                .memberIds(Set.of(member1.getId(), leadBank1.getId()))
                .totalCommitment(BigDecimal.valueOf(2000000))
                .version(1L)
                .build());
    }

    private SharePieDto createSharePie1(InvestorDto leadBank, InvestorDto member) {
        return sharePieService.create(SharePieDto.builder()
                .shares(Map.of(leadBank.getId(), BigDecimal.valueOf(30), member.getId(), BigDecimal.valueOf(70)))
                .version(1L)
                .build());
    }

    private SharePieDto createSharePie2(InvestorDto leadBank, InvestorDto member) {
        return sharePieService.create(SharePieDto.builder()
                .shares(Map.of(leadBank.getId(), BigDecimal.valueOf(50), member.getId(), BigDecimal.valueOf(50)))
                .version(1L)
                .build());

    }

    private FacilityDto createFacility1(SyndicateDto syndicate, SharePieDto sharePie, BorrowerDto borrower) {
        return facilityService.create(FacilityDto.builder()
                .totalAmount(BigDecimal.valueOf(5000000))
                .availableAmount(BigDecimal.valueOf(5000000))
                .startDate(LocalDate.of(2025, 1, 27))
                .term(60)
                .interestRate(BigDecimal.valueOf(2.5))
                .syndicateId(syndicate.getId())
                .sharePieId(sharePie.getId())
                .borrowerId(borrower.getId())
                .build());
    }

    private FacilityDto createFacility2(SyndicateDto syndicate, SharePieDto sharePie, BorrowerDto borrower) {
        return facilityService.create(FacilityDto.builder()
                .totalAmount(BigDecimal.valueOf(3000000))
                .availableAmount(BigDecimal.valueOf(3000000))
                .startDate(LocalDate.of(2025, 2, 1))
                .term(48)
                .interestRate(BigDecimal.valueOf(2.8))
                .syndicateId(syndicate.getId())
                .sharePieId(sharePie.getId())
                .borrowerId(borrower.getId())
                .build());
    }

    private FacilityInvestmentDto createFacilityInvestment1(InvestorDto investor, FacilityDto facility) {
        return facilityInvestmentService.create(FacilityInvestmentDto.builder()
                .investorId(investor.getId())
                .date(LocalDateTime.of(2025, 1, 28, 10, 0, 0))
                .relatedPositionId(facility.getId())
                .amount(new BigDecimal("600000"))
                .build());
    }

    private FacilityInvestmentDto createFacilityInvestment2(InvestorDto investor, FacilityDto facility) {
        return facilityInvestmentService.create(FacilityInvestmentDto.builder()
                .investorId(investor.getId())
                .date(LocalDateTime.of(2025, 1, 28, 10, 0, 0))
                .relatedPositionId(facility.getId())
                .amount(BigDecimal.valueOf(1400000))
                .build());
    }
}

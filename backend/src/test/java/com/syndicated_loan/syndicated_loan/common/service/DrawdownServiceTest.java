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

    // @Autowired
    // private pointRepository pointRepository;

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

    private SyndicateDto savedSyndicate1;
    private SyndicateDto savedSyndicate2;

    private SharePieDto savedSharePie1;
    private SharePieDto savedSharePie2;

    private FacilityDto savedFacility1;
    private FacilityDto savedFacility2;

    private FacilityInvestmentDto savedFacilityInvestment1;
    private FacilityInvestmentDto savedFacilityInvestment2;

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
        savedSyndicate1 = syndicateService.create(syndicate1);

        SyndicateDto syndicate2 = new SyndicateDto();
        syndicate2.setLeadBankId(leadBank2.getId()); // ちゃんとしたIDを設定
        syndicate2.setMemberIds(Set.of(member1.getId(), leadBank1.getId()));
        syndicate2.setTotalCommitment(new BigDecimal("2000000"));
        syndicate2.setVersion(1L);
        savedSyndicate2 = syndicateService.create(syndicate2);

        // 1件目のSharePieテストデータ
        SharePieDto sharePie1 = new SharePieDto();
        sharePie1.setShares(new HashMap<>());
        sharePie1.getShares().put(leadBank1.getId(), new BigDecimal("30.0000")); // ← IDを実際の投資家IDに変更
        sharePie1.getShares().put(member1.getId(), new BigDecimal("70.0000")); // ← IDを実際の投資家IDに変更
        savedSharePie1 = sharePieService.create(sharePie1);

        // 2件目のSharePieテストデータ
        SharePieDto sharePie2 = new SharePieDto();
        sharePie2.setShares(new HashMap<>());
        sharePie2.getShares().put(leadBank2.getId(), new BigDecimal("50.0000"));
        sharePie2.getShares().put(member2.getId(), new BigDecimal("50.0000"));
        savedSharePie2 = sharePieService.create(sharePie2);

        // 1件目のFacility作成
        FacilityDto facility1 = new FacilityDto();
        facility1.setTotalAmount(new BigDecimal("5000000"));
        facility1.setAvailableAmount(new BigDecimal("5000000"));
        facility1.setStartDate(LocalDate.of(2025, 1, 27));
        facility1.setTerm(60);
        facility1.setInterestRate(new BigDecimal("2.5"));
        facility1.setSyndicateId(savedSyndicate1.getId()); // ← 保存したIDを使用
        facility1.setSharePieId(savedSharePie1.getId()); // ← 保存したIDを使用
        facility1.setBorrowerId(savedBorrower.getId()); // ← 保存したIDを使用
        savedFacility1 = facilityService.create(facility1);

        // FacilityInvestmentに、作成されたFacilityのPosition IDを設定
        FacilityInvestmentDto facilityInvestment1 = new FacilityInvestmentDto();
        facilityInvestment1.setInvestorId(leadBank1.getId());
        facilityInvestment1.setDate(LocalDateTime.of(2025, 1, 28, 10, 0, 0));
        facilityInvestment1.setRelatedPositionId(savedFacility1.getId()); // ← Facilityのidを使用
        savedFacilityInvestment1 = facilityInvestmentService.create(facilityInvestment1);

        // 2件目のFacility作成
        FacilityDto facility2 = new FacilityDto();
        facility2.setTotalAmount(new BigDecimal("3000000"));
        facility2.setAvailableAmount(new BigDecimal("3000000"));
        facility2.setStartDate(LocalDate.of(2025, 2, 1));
        facility2.setTerm(48);
        facility2.setInterestRate(new BigDecimal("2.8"));
        facility2.setSyndicateId(savedSyndicate2.getId());
        facility2.setSharePieId(savedSharePie2.getId()); // ← 2つ目のSharePieを使用
        facility2.setBorrowerId(savedBorrower.getId());
        savedFacility2 = facilityService.create(facility2);

        // 2件目のFacilityInvestmentテストデータ
        FacilityInvestmentDto facilityInvestment2 = new FacilityInvestmentDto();
        facilityInvestment2.setInvestorId(leadBank2.getId());
        facilityInvestment2.setDate(LocalDateTime.of(2025, 1, 28, 10, 0, 0));
        facilityInvestment2.setRelatedPositionId(savedFacility2.getId()); // ← これを追加！
        savedFacilityInvestment2 = facilityInvestmentService.create(facilityInvestment2);

    }

    @Test
    void testFindAll() {

        // 1件目のDrawdownテストデータ
        DrawdownDto drawdown1 = new DrawdownDto();
        drawdown1.setRelatedFacilityId(this.savedFacility1.getId()); // ← 保存したIDを使用
        drawdown1.setDrawdownAmount(new BigDecimal("1000000"));
        drawdown1.setDate(LocalDateTime.of(2025, 1, 20, 0, 0, 0));
        drawdown1.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId()); // ← これを追加！
        DrawdownDto savedDrawdown1 = drawdownService.create(drawdown1);

        // 2件目のDrawdownテストデータ
        DrawdownDto drawdown2 = new DrawdownDto();
        drawdown2.setRelatedFacilityId(savedFacility2.getId()); // ← 保存したIDを使用
        drawdown2.setDrawdownAmount(new BigDecimal("2000000"));
        drawdown2.setDate(LocalDateTime.of(2025, 2, 20, 0, 0, 0));
        drawdown2.setRelatedPositionId(savedFacilityInvestment2.getRelatedPositionId()); // ← これも追加！
        DrawdownDto savedDrawdown2 = drawdownService.create(drawdown2);

        List<DrawdownDto> drawdowns = drawdownService.findAll();

        // 件数の検証
        assertThat(drawdowns).hasSize(2);

        // 1件目のデータ検証
        DrawdownDto firstDrawdown = drawdowns.get(0);
        assertThat(firstDrawdown.getAmount()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(firstDrawdown.getRelatedFacilityId()).isEqualTo(savedFacility1.getId()); // ← 実際のIDを使用
        assertThat(firstDrawdown.getDate()).isEqualTo(LocalDateTime.of(2025, 1, 20, 0, 0, 0));

        // 2件目のデータ検証
        DrawdownDto secondDrawdown = drawdowns.get(1);
        assertThat(secondDrawdown.getAmount()).isEqualByComparingTo(new BigDecimal("2000000"));
        assertThat(secondDrawdown.getRelatedFacilityId()).isEqualTo(savedFacility2.getId()); // ← 実際のIDを使用
        assertThat(secondDrawdown.getDate()).isEqualTo(LocalDateTime.of(2025, 2, 20, 0, 0, 0));
    }

    @Test
    void testFindById() {

        // 1件目のDrawdownテストデータ
        DrawdownDto drawdown1 = new DrawdownDto();
        drawdown1.setRelatedFacilityId(this.savedFacility1.getId()); // ← 保存したIDを使用
        drawdown1.setDrawdownAmount(new BigDecimal("1000000"));
        drawdown1.setDate(LocalDateTime.of(2025, 1, 20, 0, 0, 0));
        drawdown1.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId()); // ← これを追加！
        DrawdownDto savedDrawdown1 = drawdownService.create(drawdown1);

        Optional<DrawdownDto> drawdownOpt = drawdownService.findById(savedDrawdown1.getId()); // 保存したIDを使用
        assertThat(drawdownOpt).isPresent();
        DrawdownDto drawdown = drawdownOpt.get();
        // 全項目の検証
        assertThat(drawdown.getAmount()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(drawdown.getRelatedFacilityId()).isEqualTo(savedFacility1.getId());
        assertThat(drawdown.getDate()).isEqualTo(LocalDateTime.of(2025, 1, 20, 0, 0, 0));
    }

    @Test
    void testCreate() {
        DrawdownDto drawdown = new DrawdownDto();
        drawdown.setRelatedFacilityId(savedFacility1.getId());
        drawdown.setDrawdownAmount(new BigDecimal("2000000"));
        drawdown.setDate(LocalDateTime.of(2025, 1, 31, 14, 0, 0));
        drawdown.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId());

        // AmountPieの設定を修正
        AmountPieDto amountPie = new AmountPieDto();
        Map<Long, BigDecimal> amounts = new HashMap<>();
        amounts.put(leadBank1.getId(), BigDecimal.valueOf(600000)); // ← 実際の投資家IDを使用
        amounts.put(member1.getId(), BigDecimal.valueOf(1400000)); // ← 実際の投資家IDを使用
        amountPie.setAmounts(amounts);
        drawdown.setAmountPie(amountPie);

        // ここで検証に使うIDを出力してデバッグ
        System.out.println("Debug IDs:");
        System.out.println("LeadBank1 ID: " + leadBank1.getId());
        System.out.println("Member1 ID: " + member1.getId());

        DrawdownDto savedDrawdown = drawdownService.create(drawdown);

        // DBの状態を検証
        Optional<DrawdownDto> drawdownOpt = drawdownService.findById(savedDrawdown.getId());
        assertThat(drawdownOpt).isPresent();
        DrawdownDto drawdownFromDb = drawdownOpt.get();

        var amountPieFromDb = drawdownFromDb.getAmountPie();
        assertThat(amountPieFromDb).isNotNull(); // ここでnullになってる
        assertThat(amountPieFromDb.getAmounts()).hasSize(2);
        assertThat(amountPieFromDb.getAmounts().get(leadBank1.getId())) // ← 固定値じゃなくて実際のIDを使用
                .isEqualByComparingTo(new BigDecimal("600000"));
        assertThat(amountPieFromDb.getAmounts().get(member1.getId())) // ← 固定値じゃなくて実際のIDを使用
                .isEqualByComparingTo(new BigDecimal("1400000"));
    }

    @Test
    void testUpdate() {
        // 最初のDrawdownを作成
        DrawdownDto drawdown = new DrawdownDto();
        drawdown.setRelatedFacilityId(savedFacility1.getId());
        drawdown.setDrawdownAmount(new BigDecimal("2000000"));
        drawdown.setDate(LocalDateTime.of(2025, 1, 31, 14, 0, 0));
        drawdown.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId());

        // AmountPieの作成を修正
        AmountPieDto amountPie = new AmountPieDto();
        Map<Long, BigDecimal> amounts = new HashMap<>();
        amounts.put(leadBank1.getId(), BigDecimal.valueOf(600000));
        amounts.put(member1.getId(), BigDecimal.valueOf(1400000));
        amountPie.setAmounts(amounts);
        amountPie.setVersion(1L);
        drawdown.setAmountPie(amountPie);

        DrawdownDto savedDrawdown = drawdownService.create(drawdown);

        // AmountPieの情報を取得
        Optional<DrawdownDto> savedDrawdownOpt = drawdownService.findById(savedDrawdown.getId());
        assertThat(savedDrawdownOpt).isPresent();
        DrawdownDto retrievedDrawdown = savedDrawdownOpt.get();
        assertThat(retrievedDrawdown.getAmountPie()).isNotNull();

        // 更新用のデータを作成
        DrawdownDto updateDto = new DrawdownDto();
        updateDto.setId(savedDrawdown.getId());
        updateDto.setRelatedFacilityId(savedFacility1.getId());
        updateDto.setDrawdownAmount(new BigDecimal("3000000"));
        updateDto.setDate(LocalDateTime.of(2025, 2, 1, 10, 0, 0));
        updateDto.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId());
        updateDto.setVersion(retrievedDrawdown.getVersion());

        // 更新用のAmountPieも修正
        AmountPieDto newAmountPie = new AmountPieDto();
        Map<Long, BigDecimal> newAmounts = new HashMap<>();
        newAmounts.put(leadBank1.getId(), BigDecimal.valueOf(900000));
        newAmounts.put(member1.getId(), BigDecimal.valueOf(2100000));
        newAmountPie.setAmounts(newAmounts);
        newAmountPie.setVersion(retrievedDrawdown.getAmountPie().getVersion());
        updateDto.setAmountPie(newAmountPie);

        // 更新を実行
        DrawdownDto updatedDrawdown = drawdownService.update(savedDrawdown.getId(), updateDto);

        // 更新結果を検証
        assertThat(updatedDrawdown.getId()).isEqualTo(savedDrawdown.getId());
        assertThat(updatedDrawdown.getAmount()).isEqualByComparingTo(new BigDecimal("3000000"));
        assertThat(updatedDrawdown.getDate()).isEqualTo(LocalDateTime.of(2025, 2, 1, 10, 0, 0));

        // AmountPieがnullじゃないことを先に確認
        assertThat(updatedDrawdown.getAmountPie()).isNotNull();

        // それからAmountPieの中身を検証
        var actualAmounts = updatedDrawdown.getAmountPie().getAmounts();
        assertThat(actualAmounts).isNotNull();
        assertThat(actualAmounts.get(leadBank1.getId()))
                .isEqualByComparingTo(new BigDecimal("900000"));
        assertThat(actualAmounts.get(member1.getId()))
                .isEqualByComparingTo(new BigDecimal("2100000"));
    }

    @Test
    void testDelete() {
        // Drawdownを作成
        DrawdownDto drawdown = new DrawdownDto();
        drawdown.setRelatedFacilityId(savedFacility1.getId());
        drawdown.setDrawdownAmount(new BigDecimal("2000000"));
        drawdown.setDate(LocalDateTime.of(2025, 1, 31, 14, 0, 0));
        drawdown.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId());
        AmountPieDto amountPie = new AmountPieDto();
        Map<Long, BigDecimal> amounts = new HashMap<>();
        amounts.put(1L, BigDecimal.valueOf(400000));
        amounts.put(2L, BigDecimal.valueOf(1600000));
        amountPie.setAmounts(amounts);
        drawdown.setAmountPie(amountPie);
        DrawdownDto savedDrawdown = drawdownService.create(drawdown);

        // 削除を実行
        assertThatCode(() -> drawdownService.delete(savedDrawdown.getId()))
                .doesNotThrowAnyException();

        // 削除されたことを確認
        assertThat(drawdownService.findById(savedDrawdown.getId())).isEmpty();
    }

    @Test
    void testDeleteNotFound() {
        assertThatThrownBy(() -> drawdownService.delete(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Entity not found with id: 999");
    }

    @Test
    void testFindByFacility() {
        // 1件目のDrawdown作成
        DrawdownDto drawdown1 = new DrawdownDto();
        drawdown1.setRelatedFacilityId(savedFacility1.getId());
        drawdown1.setDrawdownAmount(new BigDecimal("1000000"));
        drawdown1.setDate(LocalDateTime.of(2025, 1, 20, 0, 0, 0));
        drawdown1.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId());

        // AmountPieを修正
        AmountPieDto amountPie1 = new AmountPieDto();
        Map<Long, BigDecimal> amounts1 = new HashMap<>();
        amounts1.put(leadBank1.getId(), BigDecimal.valueOf(300000)); // ← 実際の投資家ID
        amounts1.put(member1.getId(), BigDecimal.valueOf(700000)); // ← 実際の投資家ID
        amountPie1.setAmounts(amounts1);
        drawdown1.setAmountPie(amountPie1);

        drawdownService.create(drawdown1);

        // 2件目のDrawdown作成
        DrawdownDto drawdown2 = new DrawdownDto();
        drawdown2.setRelatedFacilityId(savedFacility1.getId());
        drawdown2.setDrawdownAmount(new BigDecimal("2000000"));
        drawdown2.setDate(LocalDateTime.of(2025, 2, 20, 0, 0, 0));
        drawdown2.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId());

        // AmountPieを修正
        AmountPieDto amountPie2 = new AmountPieDto();
        Map<Long, BigDecimal> amounts2 = new HashMap<>();
        amounts2.put(leadBank1.getId(), BigDecimal.valueOf(600000)); // ← 実際の投資家ID
        amounts2.put(member1.getId(), BigDecimal.valueOf(1400000)); // ← 実際の投資家ID
        amountPie2.setAmounts(amounts2);
        drawdown2.setAmountPie(amountPie2);

        DrawdownDto saved2 = drawdownService.create(drawdown2);

        // デバッグ情報を出力
        System.out.println("Debug - Amount distribution:");
        System.out.println("LeadBank1 (ID: " + leadBank1.getId() + "): 30%");
        System.out.println("Member1 (ID: " + member1.getId() + "): 70%");

        // 検索を実行
        List<DrawdownDto> drawdowns = drawdownService.findByRelatedFacility(savedFacility1.getId());

        // 結果を検証
        assertThat(drawdowns).hasSize(2);
        assertThat(drawdowns).allMatch(d -> d.getRelatedFacilityId().equals(savedFacility1.getId()));
    }

    private AmountPieDto createAmountPie(Map<Long, BigDecimal> amounts) {
        AmountPieDto amountPie = new AmountPieDto();
        amountPie.setAmounts(amounts);
        return amountPie;
    }
}

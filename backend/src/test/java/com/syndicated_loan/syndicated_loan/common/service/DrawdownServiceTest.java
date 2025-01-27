package com.syndicated_loan.syndicated_loan.common.service;

import com.syndicated_loan.syndicated_loan.common.dto.AmountPieDto;
import com.syndicated_loan.syndicated_loan.common.dto.DrawdownDto;
import com.syndicated_loan.syndicated_loan.common.dto.FacilityDto;
import com.syndicated_loan.syndicated_loan.common.dto.FacilityInvestmentDto;
import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.testutil.TestDataBuilder;
import java.math.BigDecimal;
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
public class DrawdownServiceTest {

    @Autowired
    private DrawdownService drawdownService;

    @Autowired
    private TestDataBuilder testDataBuilder;

    @Autowired
    private InvestorService investorService;

    private InvestorDto leadBank1;

    private InvestorDto member1;

    private FacilityDto savedFacility1;
    private FacilityDto savedFacility2;

    private FacilityInvestmentDto savedFacilityInvestment1;
    private FacilityInvestmentDto savedFacilityInvestment2;

    @BeforeEach
    void setUp() {

        Map<String, Object> testData = testDataBuilder.getTestDataForDrawdown();

        leadBank1 = (InvestorDto) testData.get("leadBank1");
        member1 = (InvestorDto) testData.get("member1");
        savedFacility1 = (FacilityDto) testData.get("facility1");
        savedFacility2 = (FacilityDto) testData.get("facility2");
        savedFacilityInvestment1 = (FacilityInvestmentDto) testData.get("facilityInvestment1");
        savedFacilityInvestment2 = (FacilityInvestmentDto) testData.get("facilityInvestment2");

    }

    @AfterEach
    void tearDown() {
        testDataBuilder.cleanupAll();
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

        // AmountPieの作成
        AmountPieDto amountPie = new AmountPieDto();
        Map<Long, BigDecimal> amounts = new HashMap<>();
        amounts.put(leadBank1.getId(), BigDecimal.valueOf(600000));
        amounts.put(member1.getId(), BigDecimal.valueOf(1400000));
        amountPie.setAmounts(amounts);
        amountPie.setVersion(1L);
        drawdown.setAmountPie(amountPie);

        DrawdownDto savedDrawdown = drawdownService.create(drawdown);

        // 検証
        assertThat(savedDrawdown.getId()).isNotNull();
        assertThat(savedDrawdown.getDrawdownAmount()).isEqualByComparingTo("2000000");
    }

    @Test
    void testUpdate() {
        // 最初のDrawdownを作成
        DrawdownDto drawdown = new DrawdownDto();
        drawdown.setRelatedFacilityId(savedFacility1.getId());
        drawdown.setDrawdownAmount(new BigDecimal("2000000"));
        drawdown.setDate(LocalDateTime.of(2025, 1, 31, 14, 0, 0));
        drawdown.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId());

        // AmountPieの作成
        AmountPieDto amountPie = new AmountPieDto();
        Map<Long, BigDecimal> amounts = new HashMap<>();
        amounts.put(leadBank1.getId(), BigDecimal.valueOf(600000));
        amounts.put(member1.getId(), BigDecimal.valueOf(1400000));
        amountPie.setAmounts(amounts);
        amountPie.setVersion(1L);
        drawdown.setAmountPie(amountPie);

        DrawdownDto savedDrawdown = drawdownService.create(drawdown);

        // 最新のバージョンを取得
        Optional<DrawdownDto> latestDrawdown = drawdownService.findById(savedDrawdown.getId());
        assertThat(latestDrawdown).isPresent();
        DrawdownDto currentDrawdown = latestDrawdown.get();

        // 更新用のデータを作成
        DrawdownDto updateDto = new DrawdownDto();
        updateDto.setId(currentDrawdown.getId());
        updateDto.setRelatedFacilityId(savedFacility1.getId());
        updateDto.setDrawdownAmount(new BigDecimal("3000000"));
        updateDto.setDate(LocalDateTime.of(2025, 2, 1, 10, 0, 0));
        updateDto.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId());
        updateDto.setVersion(currentDrawdown.getVersion()); // 最新のバージョンを使用

        // 更新用のAmountPie
        AmountPieDto newAmountPie = new AmountPieDto();
        Map<Long, BigDecimal> newAmounts = new HashMap<>();
        newAmounts.put(leadBank1.getId(), BigDecimal.valueOf(900000));
        newAmounts.put(member1.getId(), BigDecimal.valueOf(2100000));
        newAmountPie.setAmounts(newAmounts);
        newAmountPie.setVersion(currentDrawdown.getAmountPie().getVersion()); // 最新のバージョンを使用
        updateDto.setAmountPie(newAmountPie);

        DrawdownDto updatedDrawdown = drawdownService.update(currentDrawdown.getId(), updateDto);

        // 検証
        assertThat(updatedDrawdown.getDrawdownAmount()).isEqualByComparingTo("3000000");
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

    @Test
    void testExecuteDrawdown_UpdatesInvestorCurrentInvestments() {
        // Drawdownを作成
        DrawdownDto drawdown = new DrawdownDto();
        drawdown.setRelatedFacilityId(savedFacility1.getId());
        drawdown.setDrawdownAmount(new BigDecimal("2000000"));
        drawdown.setDate(LocalDateTime.of(2025, 1, 31, 14, 0, 0));
        drawdown.setRelatedPositionId(savedFacilityInvestment1.getRelatedPositionId());

        // AmountPieの作成
        AmountPieDto amountPie = new AmountPieDto();
        Map<Long, BigDecimal> amounts = new HashMap<>();
        amounts.put(leadBank1.getId(), BigDecimal.valueOf(600000));
        amounts.put(member1.getId(), BigDecimal.valueOf(1400000));
        amountPie.setAmounts(amounts);
        amountPie.setVersion(1L);
        drawdown.setAmountPie(amountPie);

        DrawdownDto savedDrawdown = drawdownService.create(drawdown);

        // 実行前の投資額を記録
        BigDecimal leadBank1InitialInvestment = leadBank1.getCurrentInvestments();
        BigDecimal member1InitialInvestment = member1.getCurrentInvestments();

        // ドローダウンを実行
        drawdownService.executeDrawdown(savedDrawdown.getId());

        // 投資家の現在の投資額が正しく更新されていることを確認
        InvestorDto updatedLeadBank1 = investorService.findById(leadBank1.getId()).get();
        InvestorDto updatedMember1 = investorService.findById(member1.getId()).get();

        assertThat(updatedLeadBank1.getCurrentInvestments())
                .isEqualByComparingTo(leadBank1InitialInvestment.add(BigDecimal.valueOf(600000)));
        assertThat(updatedMember1.getCurrentInvestments())
                .isEqualByComparingTo(member1InitialInvestment.add(BigDecimal.valueOf(1400000)));
    }

}

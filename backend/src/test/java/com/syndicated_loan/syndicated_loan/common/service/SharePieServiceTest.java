package com.syndicated_loan.syndicated_loan.common.service;

import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.dto.SharePieDto;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;
import com.syndicated_loan.syndicated_loan.common.testutil.TestDataBuilder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
public class SharePieServiceTest {

    @Autowired
    private TestDataBuilder testDataBuilder;
    
    @Autowired
    private SharePieService sharePieService;

    private InvestorDto leadBank1;
    private InvestorDto member1;
    private InvestorDto leadBank2;
    private InvestorDto member2;
    private SharePieDto savedSharePie;

    @BeforeEach
    void setUp() {
        Map<String, Object> testData = testDataBuilder.getTestDataForSharePie();

        leadBank1 = (InvestorDto) testData.get("leadBank1");
        member1 = (InvestorDto) testData.get("member1");
        leadBank2 = (InvestorDto) testData.get("leadBank2");
        member2 = (InvestorDto) testData.get("member2");
        savedSharePie = (SharePieDto) testData.get("sharePie1");
    }

    @AfterEach
    void tearDown() {
        testDataBuilder.cleanupAll();
    }

    @Test
    void testFindAll() {
        List<SharePieDto> sharePies = sharePieService.findAll();

        // 件数の検証
        assertThat(sharePies).hasSize(2);

        // 1件目のデータの検証
        SharePieDto sharePie1 = sharePies.get(0);
        assertThat(sharePie1.getId()).isEqualTo(savedSharePie.getId());
        assertThat(sharePie1.getShares()).hasSize(2);
        assertThat(sharePie1.getShares().get(leadBank1.getId())).isEqualByComparingTo(new BigDecimal("30.0000"));
        assertThat(sharePie1.getShares().get(member1.getId())).isEqualByComparingTo(new BigDecimal("70.0000"));

        // 2件目のデータの検証
        SharePieDto sharePie2 = sharePies.get(1);
        assertThat(sharePie2.getShares()).hasSize(2);
        assertThat(sharePie2.getShares().get(leadBank2.getId())).isEqualByComparingTo(new BigDecimal("50.0000"));
        assertThat(sharePie2.getShares().get(member2.getId())).isEqualByComparingTo(new BigDecimal("50.0000"));
    }

    @Test
    void testFindById() {
        SharePieDto sharePie = sharePieService.findById(savedSharePie.getId()).orElseThrow();

        // データの検証
        assertThat(sharePie.getId()).isEqualTo(savedSharePie.getId());
        assertThat(sharePie.getShares()).hasSize(2);
        assertThat(sharePie.getShares().get(leadBank1.getId())).isEqualByComparingTo(new BigDecimal("30.0000"));
        assertThat(sharePie.getShares().get(member1.getId())).isEqualByComparingTo(new BigDecimal("70.0000"));
    }

    @Test
    void testCreate() {
        SharePieDto sharePie = new SharePieDto();
        sharePie.setShares(new HashMap<>());
        sharePie.getShares().put(1L, new BigDecimal("40.0000"));
        sharePie.getShares().put(2L, new BigDecimal("60.0000"));
        SharePieDto createdSharePie = sharePieService.create(sharePie);

        // 作成したデータの検証
        assertThat(createdSharePie.getId()).isNotNull();
        assertThat(createdSharePie.getShares()).hasSize(2);
        assertThat(createdSharePie.getShares().get(1L)).isEqualByComparingTo(new BigDecimal("40.0000"));
        assertThat(createdSharePie.getShares().get(2L)).isEqualByComparingTo(new BigDecimal("60.0000"));

        // データがDBに保存されているか検証
        SharePieDto savedSharePie = sharePieService.findById(createdSharePie.getId()).orElseThrow();
        assertThat(savedSharePie.getShares()).hasSize(2);
        assertThat(savedSharePie.getShares().get(1L)).isEqualByComparingTo(new BigDecimal("40.0000"));
        assertThat(savedSharePie.getShares().get(2L)).isEqualByComparingTo(new BigDecimal("60.0000"));
    }

    @Test
    void testCreateWithEmptyShares() {
        SharePieDto sharePie = new SharePieDto();
        sharePie.setShares(new HashMap<>());
        assertThatThrownBy(() -> sharePieService.create(sharePie))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Shares cannot be empty");
    }

    @Test
    void testUpdate() {
        SharePieDto sharePie = sharePieService.findById(savedSharePie.getId()).orElseThrow();
        sharePie.getShares().put(leadBank1.getId(), new BigDecimal("40.0000"));
        sharePie.getShares().put(member1.getId(), new BigDecimal("60.0000"));
        sharePieService.update(savedSharePie.getId(), sharePie);

        // 更新後のデータの検証
        SharePieDto updatedSharePie = sharePieService.findById(savedSharePie.getId()).orElseThrow();
        assertThat(updatedSharePie.getShares()).hasSize(2);
        assertThat(updatedSharePie.getShares().get(leadBank1.getId())).isEqualByComparingTo(new BigDecimal("40.0000"));
        assertThat(updatedSharePie.getShares().get(member1.getId())).isEqualByComparingTo(new BigDecimal("60.0000"));
        assertThat(updatedSharePie.getVersion()).isEqualTo(3L);

        // DBに保存されたデータの検証
        SharePieDto savedSharePie = sharePieService.findById(updatedSharePie.getId()).orElseThrow();
        assertThat(savedSharePie.getShares()).hasSize(2);
        assertThat(savedSharePie.getShares().get(leadBank1.getId())).isEqualByComparingTo(new BigDecimal("40.0000"));
        assertThat(savedSharePie.getShares().get(member1.getId())).isEqualByComparingTo(new BigDecimal("60.0000"));
        assertThat(savedSharePie.getVersion()).isEqualTo(3L);
    }

    @Test
    void testDelete() {
        assertThatCode(() -> sharePieService.delete(savedSharePie.getId())).doesNotThrowAnyException();
        assertThat(sharePieService.findById(savedSharePie.getId())).isEmpty();
    }

    @Test
    void testDeleteNotFound() {
        assertThatThrownBy(() -> sharePieService.delete(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Entity not found with id: 999");
    }

    //TODO: 他のテストケースを追加する
    // sharePieService.updateShares() のテストケースを追加する
    // getInvestorShare
    // findByMinimumShare
    // findByInvestorId

}

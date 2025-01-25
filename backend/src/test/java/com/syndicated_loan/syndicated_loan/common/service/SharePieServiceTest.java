package com.syndicated_loan.syndicated_loan.common.service;

import com.syndicated_loan.syndicated_loan.common.dto.SharePieDto;
import com.syndicated_loan.syndicated_loan.common.exception.BusinessException;
import com.syndicated_loan.syndicated_loan.common.repository.SharePieRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest; // 追加！

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest // これを追加！
public class SharePieServiceTest {

    @Autowired
    private SharePieService sharePieService;

    @Autowired
    private SharePieRepository sharePieRepository;

    private SharePieDto savedSharePie; // テストデータを保持する変数を追加

    @BeforeEach
    void setUp() {
        sharePieRepository.deleteAll();

        // 1件目のテストデータ
        SharePieDto sharePie1 = new SharePieDto();
        sharePie1.setShares(new HashMap<>());
        sharePie1.getShares().put(1L, new BigDecimal("30.0000"));
        sharePie1.getShares().put(2L, new BigDecimal("70.0000"));
        savedSharePie = sharePieService.create(sharePie1);

        // 2件目のテストデータ
        SharePieDto sharePie2 = new SharePieDto();
        sharePie2.setShares(new HashMap<>());
        sharePie2.getShares().put(1L, new BigDecimal("50.0000"));
        sharePie2.getShares().put(2L, new BigDecimal("50.0000"));
        sharePieService.create(sharePie2);
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
        assertThat(sharePie1.getShares().get(1L)).isEqualByComparingTo(new BigDecimal("30.0000"));
        assertThat(sharePie1.getShares().get(2L)).isEqualByComparingTo(new BigDecimal("70.0000"));

        // 2件目のデータの検証
        SharePieDto sharePie2 = sharePies.get(1);
        assertThat(sharePie2.getShares()).hasSize(2);
        assertThat(sharePie2.getShares().get(1L)).isEqualByComparingTo(new BigDecimal("50.0000"));
        assertThat(sharePie2.getShares().get(2L)).isEqualByComparingTo(new BigDecimal("50.0000"));

    }

    @Test
    void testFindById() {
        SharePieDto sharePie = sharePieService.findById(savedSharePie.getId()).orElseThrow();

        // データの検証
        assertThat(sharePie.getId()).isEqualTo(savedSharePie.getId());
        assertThat(sharePie.getShares()).hasSize(2);
        assertThat(sharePie.getShares().get(1L)).isEqualByComparingTo(new BigDecimal("30.0000"));
        assertThat(sharePie.getShares().get(2L)).isEqualByComparingTo(new BigDecimal("70.0000"));
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
        sharePie.getShares().put(1L, new BigDecimal("40.0000"));
        sharePie.getShares().put(2L, new BigDecimal("60.0000"));
        sharePieService.update(savedSharePie.getId(), sharePie);

        // 更新後のデータの検証
        SharePieDto updatedSharePie = sharePieService.findById(savedSharePie.getId()).orElseThrow();
        assertThat(updatedSharePie.getShares()).hasSize(2);
        assertThat(updatedSharePie.getShares().get(1L)).isEqualByComparingTo(new BigDecimal("40.0000"));
        assertThat(updatedSharePie.getShares().get(2L)).isEqualByComparingTo(new BigDecimal("60.0000"));
        assertThat(updatedSharePie.getVersion()).isEqualTo(1L);

        // DBに保存されたデータの検証
        SharePieDto savedSharePie = sharePieService.findById(updatedSharePie.getId()).orElseThrow();
        assertThat(savedSharePie.getShares()).hasSize(2);
        assertThat(savedSharePie.getShares().get(1L)).isEqualByComparingTo(new BigDecimal("40.0000"));
        assertThat(savedSharePie.getShares().get(2L)).isEqualByComparingTo(new BigDecimal("60.0000"));
        assertThat(savedSharePie.getVersion()).isEqualTo(1L);
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
}

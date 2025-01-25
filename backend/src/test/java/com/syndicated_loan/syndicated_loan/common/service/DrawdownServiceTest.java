package com.syndicated_loan.syndicated_loan.common.service;

import com.syndicated_loan.syndicated_loan.common.dto.AmountPieDto;
import com.syndicated_loan.syndicated_loan.common.dto.DrawdownDto;
import com.syndicated_loan.syndicated_loan.common.repository.DrawdownRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

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
public class DrawdownServiceTest {

    @Autowired
    private DrawdownService drawdownService;

    @Autowired
    private DrawdownRepository drawdownRepository;

    private DrawdownDto savedDrawdown; // テストデータを保持する変数を追加

    @BeforeEach
    void setUp() {
        drawdownRepository.deleteAll();

        // 1件目のテストデータ
        DrawdownDto drawdown1 = new DrawdownDto();
        drawdown1.setRelatedFacilityId(1L);
        drawdown1.setDrawdownAmount(new BigDecimal("1000000"));
        drawdown1.setDate(LocalDateTime.of(2025, 1, 20, 0, 0, 0));
        AmountPieDto amountPieDto = new AmountPieDto();
        amountPieDto.setAmounts(Map.of(
                1L, new BigDecimal("4000000"),
                2L, new BigDecimal("6000000")));
        savedDrawdown = drawdownService.create(drawdown1);

        // 2件目のテストデータ
        DrawdownDto drawdown2 = new DrawdownDto();
        drawdown2.setRelatedFacilityId(2L);
        drawdown2.setDrawdownAmount(new BigDecimal("2000000"));
        drawdown2.setDate(LocalDateTime.of(2025, 2, 20, 0, 0, 0));
        AmountPieDto amountPieDto2 = new AmountPieDto();
        amountPieDto2.setAmounts(Map.of(
                1L, new BigDecimal("400000"),
                2L, new BigDecimal("1600000")));

    }

}

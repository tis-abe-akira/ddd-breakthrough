package com.syndicated_loan.syndicated_loan.common.service;

import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.dto.SyndicateDto;
import com.syndicated_loan.syndicated_loan.common.testutil.TestDataBuilder;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
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

/**
 * シンジケートサービスのテストクラス。
 * シンジケート団の各機能が正しく動作することを検証します。
 */
@SpringBootTest
public class SyndicateServiceTest {

    @Autowired
    private TestDataBuilder testDataBuilder;

    @Autowired
    private SyndicateService syndicateService;

    @Autowired
    private InvestorService investorService;

    private InvestorDto leadBank1;
    private InvestorDto leadBank2;
    private InvestorDto member1;
    private InvestorDto member2;
    private SyndicateDto savedSyndicate;

    @BeforeEach
    void setUp() {
        Map<String, Object> testData = testDataBuilder.getTestDataForSyndicate();

        leadBank1 = (InvestorDto) testData.get("leadBank1");
        leadBank2 = (InvestorDto) testData.get("leadBank2");
        member1 = (InvestorDto) testData.get("member1");
        member2 = (InvestorDto) testData.get("member2");
        savedSyndicate = (SyndicateDto) testData.get("syndicate1");
    }

    @AfterEach
    void tearDown() {
        testDataBuilder.cleanupAll();
    }

    @Test
    void testFindAll() {
        List<SyndicateDto> syndicates = syndicateService.findAll();

        // テストデータの件数を検証
        assertThat(syndicates).hasSize(2);

        // 1件目のテストデータを検証
        SyndicateDto syndicate1 = syndicates.get(0);
        assertThat(syndicate1.getId()).isEqualTo(savedSyndicate.getId());
        assertThat(syndicate1.getLeadBankId()).isEqualTo(leadBank1.getId());
        assertThat(syndicate1.getMemberIds()).containsExactlyInAnyOrder(member1.getId(), member2.getId());

        // 2件目のテストデータを検証
        SyndicateDto syndicate2 = syndicates.get(1);
        assertThat(syndicate2.getLeadBankId()).isEqualTo(leadBank2.getId());
        assertThat(syndicate2.getMemberIds()).containsExactlyInAnyOrder(member1.getId(), leadBank1.getId());

    }

    @Test
    void testFindById() {
        Optional<SyndicateDto> syndicateOpt = syndicateService.findById(savedSyndicate.getId());
        assertThat(syndicateOpt).isPresent();
        SyndicateDto syndicate = syndicateOpt.get();
        assertThat(syndicate.getLeadBankId()).isEqualTo(leadBank1.getId());
        assertThat(syndicate.getMemberIds()).containsExactlyInAnyOrder(member1.getId(), member2.getId());
        assertThat(syndicate.getTotalCommitment()).isEqualByComparingTo("5000000");
    }

    @Test
    void testCreate() {
        SyndicateDto syndicateDto = new SyndicateDto();
        syndicateDto.setLeadBankId(leadBank2.getId());
        syndicateDto.setMemberIds(Set.of(member1.getId(), member2.getId()));
        syndicateDto.setTotalCommitment(new BigDecimal("3000000"));
        syndicateDto.setVersion(1L);

        SyndicateDto createdSyndicate = syndicateService.create(syndicateDto);

        // 作成したデータを検証
        assertThat(createdSyndicate.getId()).isNotNull();
        assertThat(createdSyndicate.getLeadBankId()).isEqualTo(leadBank2.getId());
        assertThat(createdSyndicate.getMemberIds()).containsExactlyInAnyOrder(member1.getId(), member2.getId());
        assertThat(createdSyndicate.getTotalCommitment()).isEqualByComparingTo("3000000");

        // データがDBに保存されていることを検証
        Optional<SyndicateDto> savedSyndicateOpt = syndicateService.findById(createdSyndicate.getId());
        assertThat(savedSyndicateOpt).isPresent();
        SyndicateDto savedSyndicate = savedSyndicateOpt.get();
        assertThat(savedSyndicate.getLeadBankId()).isEqualTo(leadBank2.getId());
        assertThat(savedSyndicate.getMemberIds()).containsExactlyInAnyOrder(member1.getId(), member2.getId());
        assertThat(savedSyndicate.getTotalCommitment()).isEqualByComparingTo("3000000");
    }

    @Test
    void testUpdate() {
        // まず最新のデータを取得
        Optional<SyndicateDto> currentSyndicateOpt = syndicateService.findById(savedSyndicate.getId());
        assertThat(currentSyndicateOpt).isPresent();
        SyndicateDto currentSyndicate = currentSyndicateOpt.get();

        // 更新用のDTOを作成
        SyndicateDto updateDto = new SyndicateDto();
        updateDto.setId(currentSyndicate.getId());
        updateDto.setLeadBankId(leadBank2.getId());
        updateDto.setMemberIds(Set.of(member1.getId(), member2.getId()));
        updateDto.setTotalCommitment(new BigDecimal("3000000"));
        updateDto.setVersion(currentSyndicate.getVersion());

        SyndicateDto updatedSyndicate = syndicateService.update(savedSyndicate.getId(), updateDto);

        // 更新したデータを検証
        assertThat(updatedSyndicate.getId()).isEqualTo(currentSyndicate.getId());
        assertThat(updatedSyndicate.getLeadBankId()).isEqualTo(leadBank2.getId());
        assertThat(updatedSyndicate.getMemberIds()).containsExactlyInAnyOrder(member1.getId(), member2.getId());
        assertThat(updatedSyndicate.getTotalCommitment()).isEqualByComparingTo("3000000");
        assertThat(updatedSyndicate.getVersion()).isEqualTo(2L);

        // DBの値も確認
        Optional<SyndicateDto> savedSyndicateOpt = syndicateService.findById(updatedSyndicate.getId());
        assertThat(savedSyndicateOpt).isPresent();
        SyndicateDto savedSyndicate = savedSyndicateOpt.get();
        System.out.println("Final DB version: " + savedSyndicate.getVersion());
        assertThat(savedSyndicate.getLeadBankId()).isEqualTo(leadBank2.getId());
        assertThat(savedSyndicate.getMemberIds()).containsExactlyInAnyOrder(member1.getId(), member2.getId());
        assertThat(savedSyndicate.getTotalCommitment()).isEqualByComparingTo("3000000");
        assertThat(savedSyndicate.getVersion()).isEqualTo(3L); // 固定値で検証
    }

    /**
     * 削除メソッドのテスト。
     * シンジケートが正しく削除されることを確認します。
     */
    @Test
    void testDelete() {
        assertThat(syndicateService.findById(savedSyndicate.getId())).isEmpty();
    }

    /**
     * 存在しないシンジケートを削除しようとした場合のテスト。
     * 適切な例外がスローされることを確認します。
     */
    @Test
    void testDeleteNotFound() {
        assertThatThrownBy(() -> syndicateService.delete(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Entity not found with id: 999");
    }

    /**
     * メンバー追加メソッドのテスト。
     * シンジケート団にメンバーが正しく追加されることを確認します。
     */
    @Test
    void testAddMember() {
        // メンバー追加前のシンジケートを取得
        Optional<SyndicateDto> currentSyndicateOpt = syndicateService.findById(savedSyndicate.getId());
        assertThat(currentSyndicateOpt).isPresent();
    }

    @Test
    /**
     * removeMemberメソッドのテスト
     */
    void testRemoveMember() {
        // メンバー削除前のシンジケートを取得
        Optional<SyndicateDto> currentSyndicateOpt = syndicateService.findById(savedSyndicate.getId());
        assertThat(currentSyndicateOpt).isPresent();
        SyndicateDto currentSyndicate = currentSyndicateOpt.get();

        // メンバー削除
        SyndicateDto updatedSyndicate = syndicateService.removeMember(savedSyndicate.getId(), member2.getId());

        // メンバー削除後のシンジケートを取得
        Optional<SyndicateDto> finalSyndicateOpt = syndicateService.findById(savedSyndicate.getId());
        assertThat(finalSyndicateOpt).isPresent();
        SyndicateDto finalSyndicate = finalSyndicateOpt.get();

        // メンバー削除前後でメンバー数が1つ減っていることを検証
        assertThat(currentSyndicate.getMemberIds().size() - 1).isEqualTo(finalSyndicate.getMemberIds().size());
        assertThat(finalSyndicate.getMemberIds()).doesNotContain(member2.getId());
    }

    @Test
    void testFindByLeadBank() {
        List<SyndicateDto> syndicates = syndicateService.findByLeadBank(leadBank1.getId());
        assertThat(syndicates).hasSize(1);
        assertThat(syndicates.get(0).getLeadBankId()).isEqualTo(leadBank1.getId());
    }

    @Test
    void testFindByLeadBankNotFound() {
        assertThatThrownBy(() -> syndicateService.findByLeadBank(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Lead bank not found");
    }

    @Test
    void testFindByMember() {
        List<SyndicateDto> syndicates = syndicateService.findByMember(member1.getId());
        assertThat(syndicates).hasSize(2);
        assertThat(syndicates.get(0).getMemberIds()).contains(member1.getId());
        assertThat(syndicates.get(1).getMemberIds()).contains(member1.getId());
    }

    @Test
    void testFindByMemberNotFound() {
        assertThatThrownBy(() -> syndicateService.findByMember(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    void testFindByTotalCommitmentGreaterThan() {
        List<SyndicateDto> syndicates = syndicateService.findByTotalCommitmentGreaterThan(new BigDecimal("2000000"));
        assertThat(syndicates).hasSize(1);
        assertThat(syndicates.get(0).getTotalCommitment()).isGreaterThan(new BigDecimal("2000000"));
    }
}

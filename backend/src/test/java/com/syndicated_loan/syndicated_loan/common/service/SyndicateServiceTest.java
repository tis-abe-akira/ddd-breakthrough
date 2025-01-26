package com.syndicated_loan.syndicated_loan.common.service;

import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.dto.SyndicateDto;
import com.syndicated_loan.syndicated_loan.common.repository.SyndicateRepository;
import java.math.BigDecimal;
import java.util.Set;

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
public class SyndicateServiceTest {

    @Autowired
    private SyndicateService syndicateService;

    @Autowired
    private SyndicateRepository syndicateRepository;

    @Autowired
    private InvestorService investorService; // InvestorServiceを追加

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private DrawdownRepository drawdownRepository;

    @Autowired
    private FacilityInvestmentRepository facilityInvestmentRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private SharePieRepository sharePieRepository;

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    private InvestorDto leadBank1;
    private InvestorDto leadBank2;
    private InvestorDto member1;
    private InvestorDto member2;

    private SyndicateDto savedSyndicate; // テストデータを保持する変数を追加

    @BeforeEach
    void setUp() {
        // 外部キー制約を考慮した削除順序
        positionRepository.deleteAll(); // まずPositionを削除
        drawdownRepository.deleteAll();
        facilityInvestmentRepository.deleteAll();
        facilityRepository.deleteAll();
        sharePieRepository.deleteAll();
        syndicateRepository.deleteAll(); // 最後にSyndicateを削除
        investorRepository.deleteAll();
        borrowerRepository.deleteAll();

        // 次にInvestorもクリア
        if (investorService instanceof AbstractBaseService) {
            ((AbstractBaseService<?, ?, ?, ?>) investorService).getRepository().deleteAll();
        }

        // 以降は同じ
        // まず銀行データを準備
        leadBank1 = investorService.create(InvestorDto.builder()
                .name("リード銀行1")
                .type("銀行")
                .investmentCapacity(BigDecimal.valueOf(10000000))
                .currentInvestments(BigDecimal.valueOf(5000000))
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
                .investmentCapacity(BigDecimal.valueOf(5000000))
                .currentInvestments(BigDecimal.valueOf(2000000))
                .version(1L)
                .build());

        member2 = investorService.create(InvestorDto.builder()
                .name("メンバー銀行2")
                .type("銀行")
                .investmentCapacity(BigDecimal.valueOf(8000000))
                .currentInvestments(BigDecimal.valueOf(3000000))
                .version(1L)
                .build());

        // シンジケートのテストデータを作成
        SyndicateDto syndicate1 = new SyndicateDto();
        syndicate1.setLeadBankId(leadBank1.getId()); // ちゃんとしたIDを設定
        syndicate1.setMemberIds(Set.of(member1.getId(), member2.getId()));
        syndicate1.setTotalCommitment(new BigDecimal("1000000"));
        syndicate1.setVersion(1L);
        savedSyndicate = syndicateService.create(syndicate1);

        SyndicateDto syndicate2 = new SyndicateDto();
        syndicate2.setLeadBankId(leadBank2.getId()); // ちゃんとしたIDを設定
        syndicate2.setMemberIds(Set.of(member1.getId(), leadBank1.getId()));
        syndicate2.setTotalCommitment(new BigDecimal("2000000"));
        syndicate2.setVersion(1L);
        syndicateService.create(syndicate2);
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
        assertThat(syndicate.getTotalCommitment()).isEqualByComparingTo("1000000");
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

        // バージョンをデバッグ出力
        System.out.println("======= Version Debug Info =======");
        System.out.println("1. Initial save version (in setUp): " + savedSyndicate.getVersion());
        System.out.println("2. Current version from DB: " + currentSyndicate.getVersion());

        // 更新用のDTOを作成
        SyndicateDto updateDto = new SyndicateDto();
        updateDto.setId(currentSyndicate.getId());
        updateDto.setLeadBankId(leadBank2.getId());
        updateDto.setMemberIds(Set.of(member1.getId(), member2.getId()));
        updateDto.setTotalCommitment(new BigDecimal("3000000"));
        updateDto.setVersion(currentSyndicate.getVersion());

        System.out.println("3. Version being used for update: " + updateDto.getVersion());

        SyndicateDto updatedSyndicate = syndicateService.update(savedSyndicate.getId(), updateDto);
        System.out.println("4. Version after update: " + updatedSyndicate.getVersion());

        // DBから再取得して最終確認
        Optional<SyndicateDto> finalCheck = syndicateService.findById(updatedSyndicate.getId());
        System.out.println("5. Final version in DB: " + finalCheck.get().getVersion());
        System.out.println("================================");

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

    @Test
    void testDelete() {
        assertThatCode(() -> syndicateService.delete(savedSyndicate.getId())).doesNotThrowAnyException();

        // 削除後の確認を追加
        assertThat(syndicateService.findById(savedSyndicate.getId())).isEmpty();
    }

    @Test
    void testDeleteNotFound() {
        assertThatThrownBy(() -> syndicateService.delete(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Entity not found with id: 999");
    }

    @Test
    /**
     * addMemberメソッドのテスト
     */
    void testAddMember() {
        // メンバー追加前のシンジケートを取得
        Optional<SyndicateDto> currentSyndicateOpt = syndicateService.findById(savedSyndicate.getId());
        assertThat(currentSyndicateOpt).isPresent();
        SyndicateDto currentSyndicate = currentSyndicateOpt.get();

        InvestorDto member3 = investorService.create(InvestorDto.builder()
                .name("メンバー銀行3")
                .type("銀行")
                .investmentCapacity(BigDecimal.valueOf(3000000))
                .currentInvestments(BigDecimal.valueOf(1000000))
                .version(1L)
                .build());

        // メンバー追加
        SyndicateDto updatedSyndicate = syndicateService.addMember(savedSyndicate.getId(), member3.getId());

        // メンバー追加後のシンジケートを取得
        Optional<SyndicateDto> finalSyndicateOpt = syndicateService.findById(savedSyndicate.getId());
        assertThat(finalSyndicateOpt).isPresent();
        SyndicateDto finalSyndicate = finalSyndicateOpt.get();

        System.out.println("======= Member Debug Info =======");
        currentSyndicate.getMemberIds().forEach(System.out::println);
        System.out.println("================================");
        finalSyndicate.getMemberIds().forEach(System.out::println);

        // メンバー追加前後でメンバー数が1つ増えていることを検証
        assertThat(currentSyndicate.getMemberIds().size() + 1).isEqualTo(finalSyndicate.getMemberIds().size());
        assertThat(finalSyndicate.getMemberIds()).contains(member2.getId());
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
        List<SyndicateDto> syndicates = syndicateService.findByTotalCommitmentGreaterThan(new BigDecimal("1500000"));
        assertThat(syndicates).hasSize(1);
        assertThat(syndicates.get(0).getTotalCommitment()).isGreaterThan(new BigDecimal("1500000"));
    }
}

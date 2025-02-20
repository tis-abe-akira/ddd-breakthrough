package com.syndicated_loan.syndicated_loan.feature.master;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.SyndicateDto;
import com.syndicated_loan.syndicated_loan.common.service.SyndicateService;

import java.math.BigDecimal;
import java.util.List;

/**
 * シンジケート団（Syndicate）に関するREST APIのエンドポイントを提供するコントローラークラスです。
 * シンジケート団の検索、登録、更新、削除、メンバー管理などの操作を扱います。
 * シンジケート団は幹事銀行と参加金融機関で構成され、協調融資を実行する組織です。
 */
@RestController
@RequestMapping("/api/syndicates")
public class SyndicateController {

    private final SyndicateService syndicateService;

    public SyndicateController(SyndicateService syndicateService) {
        this.syndicateService = syndicateService;
    }

    /**
     * 全てのシンジケート団情報を取得します。
     *
     * @return 全シンジケート団のリストを含むレスポンス
     */
    @GetMapping
    public ResponseEntity<List<SyndicateDto>> findAll() {
        return ResponseEntity.ok(syndicateService.findAll());
    }

    /**
     * 指定されたIDのシンジケート団情報を取得します。
     *
     * @param id シンジケート団ID
     * @return シンジケート団情報を含むレスポンス。該当するシンジケート団が存在しない場合は404を返します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<SyndicateDto> findById(@PathVariable Long id) {
        return syndicateService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新規のシンジケート団を登録します。
     *
     * @param dto 登録するシンジケート団情報
     * @return 登録されたシンジケート団情報を含むレスポンス
     */
    @PostMapping
    public ResponseEntity<SyndicateDto> create(@RequestBody SyndicateDto dto) {
        return ResponseEntity.ok(syndicateService.create(dto));
    }

    /**
     * 指定されたIDのシンジケート団情報を更新します。
     *
     * @param id 更新対象のシンジケート団ID
     * @param dto 更新するシンジケート団情報
     * @return 更新されたシンジケート団情報を含むレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<SyndicateDto> update(
            @PathVariable Long id,
            @RequestBody SyndicateDto dto) {
        return ResponseEntity.ok(syndicateService.update(id, dto));
    }

    /**
     * 指定されたIDのシンジケート団を削除します。
     *
     * @param id 削除対象のシンジケート団ID
     * @return 削除成功を示すレスポンス（204 No Content）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        syndicateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 指定された幹事銀行が関与するシンジケート団を検索します。
     *
     * @param leadBankId 幹事銀行ID
     * @return 指定された幹事銀行が関与するシンジケート団のリストを含むレスポンス
     */
    @GetMapping("/search/lead-bank/{leadBankId}")
    public ResponseEntity<List<SyndicateDto>> findByLeadBank(@PathVariable Long leadBankId) {
        return ResponseEntity.ok(syndicateService.findByLeadBank(leadBankId));
    }

    /**
     * 指定された参加金融機関が所属するシンジケート団を検索します。
     *
     * @param memberId 参加金融機関ID
     * @return 指定された参加金融機関が所属するシンジケート団のリストを含むレスポンス
     */
    @GetMapping("/search/member/{memberId}")
    public ResponseEntity<List<SyndicateDto>> findByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(syndicateService.findByMember(memberId));
    }

    /**
     * 総コミットメント額が指定された金額以上のシンジケート団を検索します。
     *
     * @param minAmount 最小総コミットメント額
     * @return 条件に合致するシンジケート団のリストを含むレスポンス
     */
    @GetMapping("/search/commitment")
    public ResponseEntity<List<SyndicateDto>> findByTotalCommitmentGreaterThan(
            @RequestParam BigDecimal minAmount) {
        return ResponseEntity.ok(syndicateService.findByTotalCommitmentGreaterThan(minAmount));
    }

    /**
     * シンジケート団に新しい参加金融機関を追加します。
     *
     * @param id シンジケート団ID
     * @param investorId 追加する参加金融機関のID
     * @return 更新後のシンジケート団情報を含むレスポンス
     */
    @PostMapping("/{id}/members/{investorId}")
    public ResponseEntity<SyndicateDto> addMember(
            @PathVariable Long id,
            @PathVariable Long investorId) {
        return ResponseEntity.ok(syndicateService.addMember(id, investorId));
    }

    /**
     * シンジケート団から参加金融機関を削除します。
     *
     * @param id シンジケート団ID
     * @param investorId 削除する参加金融機関のID
     * @return 更新後のシンジケート団情報を含むレスポンス
     */
    @DeleteMapping("/{id}/members/{investorId}")
    public ResponseEntity<SyndicateDto> removeMember(
            @PathVariable Long id,
            @PathVariable Long investorId) {
        return ResponseEntity.ok(syndicateService.removeMember(id, investorId));
    }
}

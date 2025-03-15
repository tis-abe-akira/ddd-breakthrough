package com.syndicated_loan.syndicated_loan.feature.position;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.SharePieDto;
import com.syndicated_loan.syndicated_loan.common.service.SharePieService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * シェア配分（SharePie）に関するREST APIのエンドポイントを提供するコントローラークラスです。
 * シェア配分の検索、登録、更新、削除、個別シェアの参照や更新などの操作を扱います。
 * シェア配分は、シンジケートローンにおける各参加金融機関の出資比率を管理します。
 */
@RestController
@RequestMapping("/api/share-pies")
public class SharePieController {

    private final SharePieService sharePieService;

    public SharePieController(SharePieService sharePieService) {
        this.sharePieService = sharePieService;
    }

    /**
     * 全てのシェア配分情報を取得します。
     *
     * @return 全シェア配分のリストを含むレスポンス
     */
    @GetMapping
    public ResponseEntity<List<SharePieDto>> findAll() {
        return ResponseEntity.ok(sharePieService.findAll());
    }

    /**
     * 指定されたIDのシェア配分情報を取得します。
     *
     * @param id シェア配分ID
     * @return シェア配分情報を含むレスポンス。該当するシェア配分が存在しない場合は404を返します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<SharePieDto> findById(@PathVariable Long id) {
        return sharePieService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新規のシェア配分を登録します。
     *
     * @param dto 登録するシェア配分情報
     * @return 登録されたシェア配分情報を含むレスポンス
     */
    @PostMapping
    public ResponseEntity<SharePieDto> create(@RequestBody SharePieDto dto) {
        return ResponseEntity.ok(sharePieService.create(dto));
    }

    /**
     * 指定されたIDのシェア配分情報を更新します。
     *
     * @param id 更新対象のシェア配分ID
     * @param dto 更新するシェア配分情報
     * @return 更新されたシェア配分情報を含むレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<SharePieDto> update(
            @PathVariable Long id,
            @RequestBody SharePieDto dto) {
        return ResponseEntity.ok(sharePieService.update(id, dto));
    }

    /**
     * 指定されたIDのシェア配分を削除します。
     *
     * @param id 削除対象のシェア配分ID
     * @return 削除成功を示すレスポンス（204 No Content）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sharePieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 指定された投資家の出資比率を取得します。
     *
     * @param id シェア配分ID
     * @param investorId 投資家ID
     * @return 指定された投資家の出資比率を含むレスポンス
     */
    @GetMapping("/{id}/investors/{investorId}/share")
    public ResponseEntity<BigDecimal> getInvestorShare(
            @PathVariable Long id,
            @PathVariable Long investorId) {
        return ResponseEntity.ok(sharePieService.getInvestorShare(id, investorId));
    }

    /**
     * シェア配分の出資比率を一括更新します。
     * シェアの売買や再配分が発生した際に使用します。
     *
     * @param id シェア配分ID
     * @param shares 更新する出資比率のマップ（キー：投資家ID、値：出資比率）
     * @return 更新後のシェア配分情報を含むレスポンス
     */
    @PutMapping("/{id}/shares")
    public ResponseEntity<SharePieDto> updateShares(
            @PathVariable Long id,
            @RequestBody Map<Long, BigDecimal> shares) {
        return ResponseEntity.ok(sharePieService.updateShares(id, shares));
    }

    /**
     * 最小出資比率以上のシェアを含むシェア配分を検索します。
     *
     * @param minShare 最小出資比率
     * @return 条件に合致するシェア配分のリストを含むレスポンス
     */
    @GetMapping("/search/minimum-share")
    public ResponseEntity<List<SharePieDto>> findByMinimumShare(
            @RequestParam BigDecimal minShare) {
        return ResponseEntity.ok(sharePieService.findByMinimumShare(minShare));
    }

    /**
     * 指定された投資家が参加しているシェア配分を検索します。
     *
     * @param investorId 投資家ID
     * @return 指定された投資家が参加しているシェア配分のリストを含むレスポンス
     */
    @GetMapping("/search/investor/{investorId}")
    public ResponseEntity<List<SharePieDto>> findByInvestorId(
            @PathVariable Long investorId) {
        return ResponseEntity.ok(sharePieService.findByInvestorId(investorId));
    }
}

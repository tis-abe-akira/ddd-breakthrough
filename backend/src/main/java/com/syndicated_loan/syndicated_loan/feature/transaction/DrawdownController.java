package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.DrawdownDto;
import com.syndicated_loan.syndicated_loan.common.service.DrawdownService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ドローダウン（Drawdown）に関するREST APIのエンドポイントを提供するコントローラークラスです。
 * ドローダウンの検索、登録、更新、削除、実行、金額・配分の管理などの操作を扱います。
 * ドローダウンは、シンジケートローンにおいて借入人がファシリティから資金を引き出す取引を表します。
 */
@RestController
@RequestMapping("/api/drawdowns")
public class DrawdownController {

    private final DrawdownService drawdownService;

    public DrawdownController(DrawdownService drawdownService) {
        this.drawdownService = drawdownService;
    }

    /**
     * 全てのドローダウン情報を取得します。
     *
     * @return 全ドローダウンのリストを含むレスポンス
     */
    @GetMapping
    public ResponseEntity<List<DrawdownDto>> findAll() {
        return ResponseEntity.ok(drawdownService.findAll());
    }

    /**
     * 指定されたIDのドローダウン情報を取得します。
     *
     * @param id ドローダウンID
     * @return ドローダウン情報を含むレスポンス。該当するドローダウンが存在しない場合は404を返します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<DrawdownDto> findById(@PathVariable Long id) {
        return drawdownService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 指定されたファシリティに紐づくドローダウンを検索します。
     *
     * @param facilityId ファシリティID
     * @return 指定されたファシリティに紐づくドローダウンのリストを含むレスポンス
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<DrawdownDto>> findByRelatedFacility(@PathVariable Long facilityId) {
        return ResponseEntity.ok(drawdownService.findByRelatedFacility(facilityId));
    }

    /**
     * ドローダウン金額が指定された金額以上の取引を検索します。
     *
     * @param amount 最小ドローダウン金額
     * @return 条件に合致するドローダウンのリストを含むレスポンス
     */
    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<DrawdownDto>> findByDrawdownAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(drawdownService.findByDrawdownAmountGreaterThan(amount));
    }

    /**
     * 指定されたファシリティに紐づき、かつドローダウン金額が指定された金額以上の取引を検索します。
     *
     * @param facilityId ファシリティID
     * @param amount 最小ドローダウン金額
     * @return 条件に合致するドローダウンのリストを含むレスポンス
     */
    @GetMapping("/facility/{facilityId}/amount-greater-than/{amount}")
    public ResponseEntity<List<DrawdownDto>> findByRelatedFacilityAndDrawdownAmountGreaterThan(
            @PathVariable Long facilityId,
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(
                drawdownService.findByRelatedFacilityAndDrawdownAmountGreaterThan(facilityId, amount));
    }

    /**
     * 新規のドローダウンを登録します。
     *
     * @param dto 登録するドローダウン情報
     * @return 登録されたドローダウン情報を含むレスポンス
     */
    @PostMapping
    public ResponseEntity<DrawdownDto> create(@RequestBody DrawdownDto dto) {
        return ResponseEntity.ok(drawdownService.create(dto));
    }

    /**
     * 指定されたIDのドローダウン情報を更新します。
     *
     * @param id 更新対象のドローダウンID
     * @param dto 更新するドローダウン情報
     * @return 更新されたドローダウン情報を含むレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<DrawdownDto> update(
            @PathVariable Long id,
            @RequestBody DrawdownDto dto) {
        return ResponseEntity.ok(drawdownService.update(id, dto));
    }

    /**
     * ドローダウンを実行します。
     * ドローダウンの実行により、ファシリティの利用可能額が減少し、関連する会計処理が行われます。
     *
     * @param id 実行対象のドローダウンID
     * @return 実行後のドローダウン情報を含むレスポンス
     */
    @PutMapping("/{id}/execute")
    public ResponseEntity<DrawdownDto> executeDrawdown(@PathVariable Long id) {
        return ResponseEntity.ok(drawdownService.executeDrawdown(id));
    }

    /**
     * ドローダウンの引出金額を更新します。
     * 未実行のドローダウンに対してのみ実行可能です。
     *
     * @param id ドローダウンID
     * @param newAmount 新しいドローダウン金額
     * @return 更新後のドローダウン情報を含むレスポンス
     */
    @PutMapping("/{id}/drawdown-amount")
    public ResponseEntity<DrawdownDto> updateDrawdownAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal newAmount) {
        return ResponseEntity.ok(drawdownService.updateDrawdownAmount(id, newAmount));
    }

    /**
     * ドローダウンの金額配分を更新します。
     * 各参加金融機関への配分額を変更する際に使用します。
     *
     * @param id ドローダウンID
     * @param dto 更新する金額配分を含むドローダウン情報
     * @return 更新後のドローダウン情報を含むレスポンス
     */
    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<DrawdownDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody DrawdownDto dto) {
        return ResponseEntity.ok(drawdownService.updateAmountPie(id, dto.getAmountPie()));
    }

    /**
     * 指定されたIDのドローダウンを削除します。
     * 未実行のドローダウンに対してのみ実行可能です。
     *
     * @param id 削除対象のドローダウンID
     * @return 削除成功を示すレスポンス（204 No Content）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        drawdownService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.FacilityTradeDto;
import com.syndicated_loan.syndicated_loan.common.service.FacilityTradeService;

import java.math.BigDecimal;
import java.util.List;

/**
 * ファシリティ売買取引（Facility Trade）に関するREST APIのエンドポイントを提供するコントローラークラスです。
 * ファシリティ売買取引の検索、登録、更新、削除、取引金額の管理などの操作を扱います。
 * ファシリティ売買取引は、シンジケートローンの参加金融機関間でファシリティの持分を売買する取引を表します。
 */
@RestController
@RequestMapping("/api/facility-trades")
public class FacilityTradeController {

    private final FacilityTradeService facilityTradeService;

    public FacilityTradeController(FacilityTradeService facilityTradeService) {
        this.facilityTradeService = facilityTradeService;
    }

    /**
     * 全てのファシリティ売買取引情報を取得します。
     *
     * @return 全ファシリティ売買取引のリストを含むレスポンス
     */
    @GetMapping
    public ResponseEntity<List<FacilityTradeDto>> findAll() {
        return ResponseEntity.ok(facilityTradeService.findAll());
    }

    /**
     * 指定されたIDのファシリティ売買取引情報を取得します。
     *
     * @param id ファシリティ売買取引ID
     * @return ファシリティ売買取引情報を含むレスポンス。該当する取引が存在しない場合は404を返します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacilityTradeDto> findById(@PathVariable Long id) {
        return facilityTradeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 指定された売り手のファシリティ売買取引を検索します。
     *
     * @param sellerId 売り手ID（投資家ID）
     * @return 指定された売り手の売買取引のリストを含むレスポンス
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<FacilityTradeDto>> findBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(facilityTradeService.findBySeller(sellerId));
    }

    /**
     * 指定された買い手のファシリティ売買取引を検索します。
     *
     * @param buyerId 買い手ID（投資家ID）
     * @return 指定された買い手の売買取引のリストを含むレスポンス
     */
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<FacilityTradeDto>> findByBuyer(@PathVariable Long buyerId) {
        return ResponseEntity.ok(facilityTradeService.findByBuyer(buyerId));
    }

    /**
     * 取引金額が指定された金額以上のファシリティ売買取引を検索します。
     *
     * @param amount 最小取引金額
     * @return 条件に合致する売買取引のリストを含むレスポンス
     */
    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<FacilityTradeDto>> findByTradeAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(facilityTradeService.findByTradeAmountGreaterThan(amount));
    }

    /**
     * 指定された投資家が売り手または買い手として関与するファシリティ売買取引を検索します。
     *
     * @param investorId 投資家ID
     * @return 指定された投資家が関与する売買取引のリストを含むレスポンス
     */
    @GetMapping("/investor/{investorId}")
    public ResponseEntity<List<FacilityTradeDto>> findBySellerOrBuyer(@PathVariable Long investorId) {
        return ResponseEntity.ok(facilityTradeService.findBySellerOrBuyer(investorId));
    }

    /**
     * 新規のファシリティ売買取引を登録します。
     *
     * @param dto 登録する売買取引情報
     * @return 登録された売買取引情報を含むレスポンス
     */
    @PostMapping
    public ResponseEntity<FacilityTradeDto> create(@RequestBody FacilityTradeDto dto) {
        return ResponseEntity.ok(facilityTradeService.create(dto));
    }

    /**
     * 指定されたIDのファシリティ売買取引情報を更新します。
     *
     * @param id 更新対象の売買取引ID
     * @param dto 更新する売買取引情報
     * @return 更新された売買取引情報を含むレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<FacilityTradeDto> update(
            @PathVariable Long id,
            @RequestBody FacilityTradeDto dto) {
        return ResponseEntity.ok(facilityTradeService.update(id, dto));
    }

    /**
     * ファシリティ売買取引の取引金額を更新します。
     * 未実行の取引に対してのみ実行可能です。
     *
     * @param id 売買取引ID
     * @param newAmount 新しい取引金額
     * @return 更新後の売買取引情報を含むレスポンス
     */
    @PutMapping("/{id}/trade-amount")
    public ResponseEntity<FacilityTradeDto> updateTradeAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal newAmount) {
        return ResponseEntity.ok(facilityTradeService.updateTradeAmount(id, newAmount));
    }

    /**
     * ファシリティ売買取引の金額配分を更新します。
     * 取引に関連する参加金融機関間での配分変更が発生した際に使用します。
     *
     * @param id 売買取引ID
     * @param dto 更新する金額配分を含む売買取引情報
     * @return 更新後の売買取引情報を含むレスポンス
     */
    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<FacilityTradeDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody FacilityTradeDto dto) {
        return ResponseEntity.ok(facilityTradeService.updateAmountPie(id, dto.getAmountPie()));
    }

    /**
     * 指定されたIDのファシリティ売買取引を削除します。
     * 未実行の取引に対してのみ実行可能です。
     *
     * @param id 削除対象の売買取引ID
     * @return 削除成功を示すレスポンス（204 No Content）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facilityTradeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

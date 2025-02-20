package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.FeePaymentDto;
import com.syndicated_loan.syndicated_loan.common.service.FeePaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 手数料支払い（Fee Payment）に関するREST APIのエンドポイントを提供するコントローラークラスです。
 * 手数料支払いの検索、登録、更新、削除、実行、金額・配分の管理などの操作を扱います。
 * 手数料支払いは、シンジケートローンにおいてアレンジメントフィーやエージェントフィーなどの各種手数料の支払いを表します。
 */
@RestController
@RequestMapping("/api/fee-payments")
public class FeePaymentController {

    private final FeePaymentService feePaymentService;

    public FeePaymentController(FeePaymentService feePaymentService) {
        this.feePaymentService = feePaymentService;
    }

    /**
     * 全ての手数料支払い情報を取得します。
     *
     * @return 全手数料支払いのリストを含むレスポンス
     */
    @GetMapping
    public ResponseEntity<List<FeePaymentDto>> findAll() {
        return ResponseEntity.ok(feePaymentService.findAll());
    }

    /**
     * 指定されたIDの手数料支払い情報を取得します。
     *
     * @param id 手数料支払いID
     * @return 手数料支払い情報を含むレスポンス。該当する支払いが存在しない場合は404を返します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeePaymentDto> findById(@PathVariable Long id) {
        return feePaymentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 指定されたファシリティに紐づく手数料支払いを検索します。
     *
     * @param facilityId ファシリティID
     * @return 指定されたファシリティに紐づく手数料支払いのリストを含むレスポンス
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<FeePaymentDto>> findByFacility(@PathVariable Long facilityId) {
        return ResponseEntity.ok(feePaymentService.findByFacility(facilityId));
    }

    /**
     * 指定された手数料タイプの支払いを検索します。
     * 
     * @param feeType 手数料タイプ（例：アレンジメントフィー、エージェントフィー）
     * @return 指定された手数料タイプの支払いのリストを含むレスポンス
     */
    @GetMapping("/type/{feeType}")
    public ResponseEntity<List<FeePaymentDto>> findByFeeType(@PathVariable String feeType) {
        return ResponseEntity.ok(feePaymentService.findByFeeType(feeType));
    }

    /**
     * 支払い金額が指定された金額以上の手数料支払いを検索します。
     *
     * @param amount 最小支払い金額
     * @return 条件に合致する手数料支払いのリストを含むレスポンス
     */
    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<FeePaymentDto>> findByPaymentAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(feePaymentService.findByPaymentAmountGreaterThan(amount));
    }

    /**
     * 指定されたファシリティと手数料タイプに合致する手数料支払いを検索します。
     *
     * @param facilityId ファシリティID
     * @param feeType 手数料タイプ
     * @return 条件に合致する手数料支払いのリストを含むレスポンス
     */
    @GetMapping("/facility/{facilityId}/type/{feeType}")
    public ResponseEntity<List<FeePaymentDto>> findByFacilityAndFeeType(
            @PathVariable Long facilityId,
            @PathVariable String feeType) {
        return ResponseEntity.ok(feePaymentService.findByFacilityAndFeeType(facilityId, feeType));
    }

    /**
     * 新規の手数料支払いを登録します。
     *
     * @param dto 登録する手数料支払い情報
     * @return 登録された手数料支払い情報を含むレスポンス
     */
    @PostMapping
    public ResponseEntity<FeePaymentDto> create(@RequestBody FeePaymentDto dto) {
        return ResponseEntity.ok(feePaymentService.create(dto));
    }

    /**
     * 指定されたIDの手数料支払い情報を更新します。
     *
     * @param id 更新対象の手数料支払いID
     * @param dto 更新する手数料支払い情報
     * @return 更新された手数料支払い情報を含むレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<FeePaymentDto> update(
            @PathVariable Long id,
            @RequestBody FeePaymentDto dto) {
        return ResponseEntity.ok(feePaymentService.update(id, dto));
    }

    /**
     * 手数料支払いを実行します。
     * 支払いの実行により、関連する会計処理が行われます。
     *
     * @param id 実行対象の手数料支払いID
     * @return 実行後の手数料支払い情報を含むレスポンス
     */
    @PutMapping("/{id}/execute")
    public ResponseEntity<FeePaymentDto> executeFeePayment(@PathVariable Long id) {
        return ResponseEntity.ok(feePaymentService.executeFeePayment(id));
    }

    /**
     * 手数料支払いの支払い金額を更新します。
     * 未実行の支払いに対してのみ実行可能です。
     *
     * @param id 手数料支払いID
     * @param newAmount 新しい支払い金額
     * @return 更新後の手数料支払い情報を含むレスポンス
     */
    @PutMapping("/{id}/payment-amount")
    public ResponseEntity<FeePaymentDto> updatePaymentAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal newAmount) {
        return ResponseEntity.ok(feePaymentService.updatePaymentAmount(id, newAmount));
    }

    /**
     * 手数料支払いの金額配分を更新します。
     * 手数料を受け取る参加金融機関間での配分変更が発生した際に使用します。
     *
     * @param id 手数料支払いID
     * @param dto 更新する金額配分を含む手数料支払い情報
     * @return 更新後の手数料支払い情報を含むレスポンス
     */
    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<FeePaymentDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody FeePaymentDto dto) {
        return ResponseEntity.ok(feePaymentService.updateAmountPie(id, dto.getAmountPie()));
    }

    /**
     * 指定されたIDの手数料支払いを削除します。
     * 未実行の支払いに対してのみ実行可能です。
     *
     * @param id 削除対象の手数料支払いID
     * @return 削除成功を示すレスポンス（204 No Content）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feePaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.FeePaymentDto;
import com.syndicated_loan.syndicated_loan.common.service.FeePaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 手数料支払いに関するREST APIを提供するコントローラクラス。
 * 手数料支払いの検索、作成、更新、実行などの機能を公開します。
 */
@RestController
@RequestMapping("/api/fee-payments")
public class FeePaymentController {

    /**
     * 手数料支払いサービス
     */
    private final FeePaymentService feePaymentService;

    /**
     * コンストラクタ
     *
     * @param feePaymentService 手数料支払いサービス
     */
    public FeePaymentController(FeePaymentService feePaymentService) {
        this.feePaymentService = feePaymentService;
    }

    /**
     * すべての手数料支払いを取得します
     *
     * @return 手数料支払いDTOのリスト
     */
    @GetMapping
    public ResponseEntity<List<FeePaymentDto>> findAll() {
        return ResponseEntity.ok(feePaymentService.findAll());
    }

    /**
     * IDを指定して手数料支払いを取得します
     *
     * @param id 手数料支払いID
     * @return 手数料支払いDTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeePaymentDto> findById(@PathVariable Long id) {
        return feePaymentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ファシリティに関連する手数料支払いを取得します
     *
     * @param facilityId ファシリティID
     * @return 手数料支払いDTOのリスト
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<FeePaymentDto>> findByFacility(@PathVariable Long facilityId) {
        return ResponseEntity.ok(feePaymentService.findByFacility(facilityId));
    }

    /**
     * 指定した手数料タイプの支払いを取得します
     *
     * @param feeType 手数料タイプ
     * @return 手数料支払いDTOのリスト
     */
    @GetMapping("/type/{feeType}")
    public ResponseEntity<List<FeePaymentDto>> findByFeeType(@PathVariable String feeType) {
        return ResponseEntity.ok(feePaymentService.findByFeeType(feeType));
    }

    /**
     * 指定した金額より大きい支払額の手数料支払いを取得します
     *
     * @param amount 基準金額
     * @return 手数料支払いDTOのリスト
     */
    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<FeePaymentDto>> findByPaymentAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(feePaymentService.findByPaymentAmountGreaterThan(amount));
    }

    /**
     * 指定したファシリティと手数料タイプに関連する支払いを取得します
     *
     * @param facilityId ファシリティID
     * @param feeType    手数料タイプ
     * @return 手数料支払いDTOのリスト
     */
    @GetMapping("/facility/{facilityId}/type/{feeType}")
    public ResponseEntity<List<FeePaymentDto>> findByFacilityAndFeeType(
            @PathVariable Long facilityId,
            @PathVariable String feeType) {
        return ResponseEntity.ok(feePaymentService.findByFacilityAndFeeType(facilityId, feeType));
    }

    /**
     * 新しい手数料支払いを作成します
     *
     * @param dto 作成する手数料支払い情報
     * @return 作成された手数料支払いDTO
     */
    @PostMapping
    public ResponseEntity<FeePaymentDto> create(@RequestBody FeePaymentDto dto) {
        return ResponseEntity.ok(feePaymentService.create(dto));
    }

    /**
     * 手数料支払いを更新します
     *
     * @param id  更新する手数料支払いID
     * @param dto 更新情報
     * @return 更新された手数料支払いDTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<FeePaymentDto> update(
            @PathVariable Long id,
            @RequestBody FeePaymentDto dto) {
        return ResponseEntity.ok(feePaymentService.update(id, dto));
    }

    /**
     * 手数料支払いを実行します
     *
     * @param id 実行する手数料支払いID
     * @return 実行後の手数料支払いDTO
     */
    @PutMapping("/{id}/execute")
    public ResponseEntity<FeePaymentDto> executeFeePayment(@PathVariable Long id) {
        return ResponseEntity.ok(feePaymentService.executeFeePayment(id));
    }

    /**
     * 手数料支払い金額を更新します
     *
     * @param id        対象の手数料支払いID
     * @param newAmount 新しい支払い金額
     * @return 更新後の手数料支払いDTO
     */
    @PutMapping("/{id}/payment-amount")
    public ResponseEntity<FeePaymentDto> updatePaymentAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal newAmount) {
        return ResponseEntity.ok(feePaymentService.updatePaymentAmount(id, newAmount));
    }

    /**
     * 手数料支払いの金額配分を更新します
     *
     * @param id  対象の手数料支払いID
     * @param dto 金額配分情報を含むDTO
     * @return 更新後の手数料支払いDTO
     */
    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<FeePaymentDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody FeePaymentDto dto) {
        return ResponseEntity.ok(feePaymentService.updateAmountPie(id, dto.getAmountPie()));
    }

    /**
     * 手数料支払いを削除します
     *
     * @param id 削除する手数料支払いID
     * @return レスポンス
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feePaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

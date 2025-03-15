package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.PrincipalPaymentDto;
import com.syndicated_loan.syndicated_loan.common.service.PrincipalPaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 元本返済（Principal Payment）に関するREST APIのエンドポイントを提供するコントローラークラスです。
 * 元本返済の検索、登録、更新、削除、実行、金額・配分の管理などの操作を扱います。
 * 元本返済は、シンジケートローンにおいて借入人が参加金融機関に返済する元本の取引を表します。
 */
@RestController
@RequestMapping("/api/principal-payments")
public class PrincipalPaymentController {

    private final PrincipalPaymentService principalPaymentService;

    public PrincipalPaymentController(PrincipalPaymentService principalPaymentService) {
        this.principalPaymentService = principalPaymentService;
    }

    /**
     * 全ての元本返済情報を取得します。
     *
     * @return 全元本返済のリストを含むレスポンス
     */
    @GetMapping
    public ResponseEntity<List<PrincipalPaymentDto>> findAll() {
        return ResponseEntity.ok(principalPaymentService.findAll());
    }

    /**
     * 指定されたIDの元本返済情報を取得します。
     *
     * @param id 元本返済ID
     * @return 元本返済情報を含むレスポンス。該当する返済が存在しない場合は404を返します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<PrincipalPaymentDto> findById(@PathVariable Long id) {
        return principalPaymentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 指定されたローンに紐づく元本返済を検索します。
     *
     * @param loanId ローンID
     * @return 指定されたローンに紐づく元本返済のリストを含むレスポンス
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<PrincipalPaymentDto>> findByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(principalPaymentService.findByLoan(loanId));
    }

    /**
     * 返済金額が指定された金額以上の元本返済を検索します。
     *
     * @param amount 最小返済金額
     * @return 条件に合致する元本返済のリストを含むレスポンス
     */
    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<PrincipalPaymentDto>> findByPaymentAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(principalPaymentService.findByPaymentAmountGreaterThan(amount));
    }

    /**
     * 指定された期間内に予定される元本返済を検索します。
     *
     * @param startDate 期間開始日時
     * @param endDate 期間終了日時
     * @return 条件に合致する元本返済のリストを含むレスポンス
     */
    @GetMapping("/period")
    public ResponseEntity<List<PrincipalPaymentDto>> findByDateBetween(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(principalPaymentService.findByDateBetween(startDate, endDate));
    }

    /**
     * 指定されたローンの、指定された期間内に予定される元本返済を検索します。
     *
     * @param loanId ローンID
     * @param startDate 期間開始日時
     * @param endDate 期間終了日時
     * @return 条件に合致する元本返済のリストを含むレスポンス
     */
    @GetMapping("/loan/{loanId}/period")
    public ResponseEntity<List<PrincipalPaymentDto>> findByLoanAndDateBetween(
            @PathVariable Long loanId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(
                principalPaymentService.findByLoanAndDateBetween(loanId, startDate, endDate));
    }

    /**
     * 新規の元本返済を登録します。
     *
     * @param dto 登録する元本返済情報
     * @return 登録された元本返済情報を含むレスポンス
     */
    @PostMapping
    public ResponseEntity<PrincipalPaymentDto> create(@RequestBody PrincipalPaymentDto dto) {
        return ResponseEntity.ok(principalPaymentService.create(dto));
    }

    /**
     * 指定されたIDの元本返済情報を更新します。
     *
     * @param id 更新対象の元本返済ID
     * @param dto 更新する元本返済情報
     * @return 更新された元本返済情報を含むレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<PrincipalPaymentDto> update(
            @PathVariable Long id,
            @RequestBody PrincipalPaymentDto dto) {
        return ResponseEntity.ok(principalPaymentService.update(id, dto));
    }

    /**
     * 元本返済を実行します。
     * 返済の実行により、ローンの元本残高が減少し、関連する会計処理が行われます。
     *
     * @param id 実行対象の元本返済ID
     * @return 実行後の元本返済情報を含むレスポンス
     */
    @PutMapping("/{id}/execute")
    public ResponseEntity<PrincipalPaymentDto> executePrincipalPayment(@PathVariable Long id) {
        return ResponseEntity.ok(principalPaymentService.executePrincipalPayment(id));
    }

    /**
     * 元本返済の返済金額を更新します。
     * 未実行の返済に対してのみ実行可能です。
     *
     * @param id 元本返済ID
     * @param newAmount 新しい返済金額
     * @return 更新後の元本返済情報を含むレスポンス
     */
    @PutMapping("/{id}/payment-amount")
    public ResponseEntity<PrincipalPaymentDto> updatePaymentAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal newAmount) {
        return ResponseEntity.ok(principalPaymentService.updatePaymentAmount(id, newAmount));
    }

    /**
     * 元本返済の金額配分を更新します。
     * 返済を受け取る参加金融機関間での配分変更が発生した際に使用します。
     *
     * @param id 元本返済ID
     * @param dto 更新する金額配分を含む元本返済情報
     * @return 更新後の元本返済情報を含むレスポンス
     */
    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<PrincipalPaymentDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody PrincipalPaymentDto dto) {
        return ResponseEntity.ok(principalPaymentService.updateAmountPie(id, dto.getAmountPie()));
    }

    /**
     * 指定されたIDの元本返済を削除します。
     * 未実行の返済に対してのみ実行可能です。
     *
     * @param id 削除対象の元本返済ID
     * @return 削除成功を示すレスポンス（204 No Content）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        principalPaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

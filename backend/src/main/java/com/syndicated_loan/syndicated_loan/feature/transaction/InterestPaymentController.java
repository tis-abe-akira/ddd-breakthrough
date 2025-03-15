package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.InterestPaymentDto;
import com.syndicated_loan.syndicated_loan.common.service.InterestPaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 利息支払い（Interest Payment）に関するREST APIのエンドポイントを提供するコントローラークラスです。
 * 利息支払いの検索、登録、更新、削除、実行、金額計算、配分の管理などの操作を扱います。
 * 利息支払いは、シンジケートローンにおいて借入人が参加金融機関に支払う利息の取引を表します。
 */
@RestController
@RequestMapping("/api/interest-payments")
public class InterestPaymentController {

    private final InterestPaymentService interestPaymentService;

    public InterestPaymentController(InterestPaymentService interestPaymentService) {
        this.interestPaymentService = interestPaymentService;
    }

    /**
     * 全ての利息支払い情報を取得します。
     *
     * @return 全利息支払いのリストを含むレスポンス
     */
    @GetMapping
    public ResponseEntity<List<InterestPaymentDto>> findAll() {
        return ResponseEntity.ok(interestPaymentService.findAll());
    }

    /**
     * 指定されたIDの利息支払い情報を取得します。
     *
     * @param id 利息支払いID
     * @return 利息支払い情報を含むレスポンス。該当する支払いが存在しない場合は404を返します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<InterestPaymentDto> findById(@PathVariable Long id) {
        return interestPaymentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 指定されたローンに紐づく利息支払いを検索します。
     *
     * @param loanId ローンID
     * @return 指定されたローンに紐づく利息支払いのリストを含むレスポンス
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<InterestPaymentDto>> findByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(interestPaymentService.findByLoan(loanId));
    }

    /**
     * 金利が指定された率以上の利息支払いを検索します。
     *
     * @param rate 最小金利（小数表示、例：0.05 = 5%）
     * @return 条件に合致する利息支払いのリストを含むレスポンス
     */
    @GetMapping("/rate-greater-than/{rate}")
    public ResponseEntity<List<InterestPaymentDto>> findByInterestRateGreaterThan(
            @PathVariable BigDecimal rate) {
        return ResponseEntity.ok(interestPaymentService.findByInterestRateGreaterThan(rate));
    }

    /**
     * 支払い金額が指定された金額以上の利息支払いを検索します。
     *
     * @param amount 最小支払い金額
     * @return 条件に合致する利息支払いのリストを含むレスポンス
     */
    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<InterestPaymentDto>> findByPaymentAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(interestPaymentService.findByPaymentAmountGreaterThan(amount));
    }

    /**
     * 指定された期間内に利息期間が開始する支払いを検索します。
     *
     * @param startDate 期間開始日
     * @param endDate 期間終了日
     * @return 条件に合致する利息支払いのリストを含むレスポンス
     */
    @GetMapping("/period")
    public ResponseEntity<List<InterestPaymentDto>> findByInterestStartDateBetween(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(interestPaymentService.findByInterestStartDateBetween(startDate, endDate));
    }

    /**
     * 指定されたローンの、指定された期間内に利息期間が開始する支払いを検索します。
     *
     * @param loanId ローンID
     * @param startDate 期間開始日
     * @param endDate 期間終了日
     * @return 条件に合致する利息支払いのリストを含むレスポンス
     */
    @GetMapping("/loan/{loanId}/period")
    public ResponseEntity<List<InterestPaymentDto>> findByLoanAndInterestStartDateBetween(
            @PathVariable Long loanId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(
                interestPaymentService.findByLoanAndInterestStartDateBetween(loanId, startDate, endDate));
    }

    /**
     * 新規の利息支払いを登録します。
     *
     * @param dto 登録する利息支払い情報
     * @return 登録された利息支払い情報を含むレスポンス
     */
    @PostMapping
    public ResponseEntity<InterestPaymentDto> create(@RequestBody InterestPaymentDto dto) {
        return ResponseEntity.ok(interestPaymentService.create(dto));
    }

    /**
     * 指定されたIDの利息支払い情報を更新します。
     *
     * @param id 更新対象の利息支払いID
     * @param dto 更新する利息支払い情報
     * @return 更新された利息支払い情報を含むレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<InterestPaymentDto> update(
            @PathVariable Long id,
            @RequestBody InterestPaymentDto dto) {
        return ResponseEntity.ok(interestPaymentService.update(id, dto));
    }

    /**
     * 利息支払いを実行します。
     * 支払いの実行により、関連する会計処理が行われます。
     *
     * @param id 実行対象の利息支払いID
     * @return 実行後の利息支払い情報を含むレスポンス
     */
    @PutMapping("/{id}/execute")
    public ResponseEntity<InterestPaymentDto> executeInterestPayment(@PathVariable Long id) {
        return ResponseEntity.ok(interestPaymentService.executeInterestPayment(id));
    }

    /**
     * 利息支払いの支払い金額を計算して更新します。
     * 金利、期間、元本残高などから利息額を算出します。
     *
     * @param id 利息支払いID
     * @return 支払い金額が更新された利息支払い情報を含むレスポンス
     */
    @PutMapping("/{id}/payment-amount")
    public ResponseEntity<InterestPaymentDto> calculateAndUpdatePaymentAmount(@PathVariable Long id) {
        return ResponseEntity.ok(interestPaymentService.calculateAndUpdatePaymentAmount(id));
    }

    /**
     * 利息支払いの金額配分を更新します。
     * 利息を受け取る参加金融機関間での配分変更が発生した際に使用します。
     *
     * @param id 利息支払いID
     * @param dto 更新する金額配分を含む利息支払い情報
     * @return 更新後の利息支払い情報を含むレスポンス
     */
    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<InterestPaymentDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody InterestPaymentDto dto) {
        return ResponseEntity.ok(interestPaymentService.updateAmountPie(id, dto.getAmountPie()));
    }

    /**
     * 指定されたIDの利息支払いを削除します。
     * 未実行の支払いに対してのみ実行可能です。
     *
     * @param id 削除対象の利息支払いID
     * @return 削除成功を示すレスポンス（204 No Content）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interestPaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.InterestPaymentDto;
import com.syndicated_loan.syndicated_loan.common.service.InterestPaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 利息支払いに関するREST APIを提供するコントローラクラス。
 * 利息支払いの検索、作成、更新、実行などの機能を公開します。
 */
@RestController
@RequestMapping("/api/interest-payments")
public class InterestPaymentController {

    /**
     * 利息支払いサービス
     */
    private final InterestPaymentService interestPaymentService;

    /**
     * コンストラクタ
     *
     * @param interestPaymentService 利息支払いサービス
     */
    public InterestPaymentController(InterestPaymentService interestPaymentService) {
        this.interestPaymentService = interestPaymentService;
    }

    /**
     * すべての利息支払いを取得します
     *
     * @return 利息支払いDTOのリスト
     */
    @GetMapping
    public ResponseEntity<List<InterestPaymentDto>> findAll() {
        return ResponseEntity.ok(interestPaymentService.findAll());
    }

    /**
     * IDを指定して利息支払いを取得します
     *
     * @param id 利息支払いID
     * @return 利息支払いDTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<InterestPaymentDto> findById(@PathVariable Long id) {
        return interestPaymentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ローンに関連する利息支払いを取得します
     *
     * @param loanId ローンID
     * @return 利息支払いDTOのリスト
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<InterestPaymentDto>> findByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(interestPaymentService.findByLoan(loanId));
    }

    /**
     * 指定した金利より高い利息支払いを取得します
     *
     * @param rate 基準金利
     * @return 利息支払いDTOのリスト
     */
    @GetMapping("/rate-greater-than/{rate}")
    public ResponseEntity<List<InterestPaymentDto>> findByInterestRateGreaterThan(
            @PathVariable BigDecimal rate) {
        return ResponseEntity.ok(interestPaymentService.findByInterestRateGreaterThan(rate));
    }

    /**
     * 指定した金額より大きい支払額の利息支払いを取得します
     *
     * @param amount 基準金額
     * @return 利息支払いDTOのリスト
     */
    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<InterestPaymentDto>> findByPaymentAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(interestPaymentService.findByPaymentAmountGreaterThan(amount));
    }

    /**
     * 指定した期間内の利息支払いを取得します
     *
     * @param startDate 開始日
     * @param endDate   終了日
     * @return 利息支払いDTOのリスト
     */
    @GetMapping("/period")
    public ResponseEntity<List<InterestPaymentDto>> findByInterestStartDateBetween(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(interestPaymentService.findByInterestStartDateBetween(startDate, endDate));
    }

    /**
     * 指定したローンと期間に関連する利息支払いを取得します
     *
     * @param loanId    ローンID
     * @param startDate 開始日
     * @param endDate   終了日
     * @return 利息支払いDTOのリスト
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
     * 新しい利息支払いを作成します
     *
     * @param dto 作成する利息支払い情報
     * @return 作成された利息支払いDTO
     */
    @PostMapping
    public ResponseEntity<InterestPaymentDto> create(@RequestBody InterestPaymentDto dto) {
        return ResponseEntity.ok(interestPaymentService.create(dto));
    }

    /**
     * 利息支払いを更新します
     *
     * @param id  更新する利息支払いID
     * @param dto 更新情報
     * @return 更新された利息支払いDTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<InterestPaymentDto> update(
            @PathVariable Long id,
            @RequestBody InterestPaymentDto dto) {
        return ResponseEntity.ok(interestPaymentService.update(id, dto));
    }

    /**
     * 利息支払いを実行します
     *
     * @param id 実行する利息支払いID
     * @return 実行後の利息支払いDTO
     */
    @PutMapping("/{id}/execute")
    public ResponseEntity<InterestPaymentDto> executeInterestPayment(@PathVariable Long id) {
        return ResponseEntity.ok(interestPaymentService.executeInterestPayment(id));
    }

    /**
     * 利息支払い金額を計算・更新します
     *
     * @param id 対象の利息支払いID
     * @return 更新後の利息支払いDTO
     */
    @PutMapping("/{id}/payment-amount")
    public ResponseEntity<InterestPaymentDto> calculateAndUpdatePaymentAmount(@PathVariable Long id) {
        return ResponseEntity.ok(interestPaymentService.calculateAndUpdatePaymentAmount(id));
    }

    /**
     * 利息支払いの金額配分を更新します
     *
     * @param id  対象の利息支払いID
     * @param dto 金額配分情報を含むDTO
     * @return 更新後の利息支払いDTO
     */
    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<InterestPaymentDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody InterestPaymentDto dto) {
        return ResponseEntity.ok(interestPaymentService.updateAmountPie(id, dto.getAmountPie()));
    }

    /**
     * 利息支払いを削除します
     *
     * @param id 削除する利息支払いID
     * @return レスポンス
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interestPaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 利息支払いの配分結果を取得します
     *
     * @param id 対象の利息支払いID
     * @return 配分結果情報
     */
    @GetMapping("/{id}/distribution")
    public ResponseEntity<Map<String, Object>> getDistribution(@PathVariable Long id) {
        return ResponseEntity.ok(interestPaymentService.getDistributionResult(id));
    }
}

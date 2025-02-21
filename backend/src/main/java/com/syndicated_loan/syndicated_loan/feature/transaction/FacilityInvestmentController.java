package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.FacilityInvestmentDto;
import com.syndicated_loan.syndicated_loan.common.service.FacilityInvestmentService;

import java.math.BigDecimal;
import java.util.List;

/**
 * ファシリティ投資（Facility Investment）に関するREST APIのエンドポイントを提供するコントローラークラスです。
 * ファシリティ投資の検索、登録、更新、削除、投資額の管理、金額配分の更新などの操作を扱います。
 * ファシリティ投資は、シンジケートローンにおいて参加金融機関がファシリティに出資する取引を表します。
 */
@RestController
@RequestMapping("/api/facility-investments")
public class FacilityInvestmentController {

    private final FacilityInvestmentService facilityInvestmentService;

    public FacilityInvestmentController(FacilityInvestmentService facilityInvestmentService) {
        this.facilityInvestmentService = facilityInvestmentService;
    }

    /**
     * 全てのファシリティ投資情報を取得します。
     *
     * @return 全ファシリティ投資のリストを含むレスポンス
     */
    @GetMapping
    public ResponseEntity<List<FacilityInvestmentDto>> findAll() {
        return ResponseEntity.ok(facilityInvestmentService.findAll());
    }

    /**
     * 指定されたIDのファシリティ投資情報を取得します。
     *
     * @param id ファシリティ投資ID
     * @return ファシリティ投資情報を含むレスポンス。該当する投資が存在しない場合は404を返します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacilityInvestmentDto> findById(@PathVariable Long id) {
        return facilityInvestmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 指定された投資家のファシリティ投資を検索します。
     *
     * @param investorId 投資家ID
     * @return 指定された投資家のファシリティ投資のリストを含むレスポンス
     */
    @GetMapping("/investor/{investorId}")
    public ResponseEntity<List<FacilityInvestmentDto>> findByInvestor(@PathVariable Long investorId) {
        return ResponseEntity.ok(facilityInvestmentService.findByInvestor(investorId));
    }

    /**
     * 投資金額が指定された金額以上の取引を検索します。
     *
     * @param amount 最小投資金額
     * @return 条件に合致するファシリティ投資のリストを含むレスポンス
     */
    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<FacilityInvestmentDto>> findByInvestmentAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(facilityInvestmentService.findByInvestmentAmountGreaterThan(amount));
    }

    /**
     * 新規のファシリティ投資を登録します。
     *
     * @param dto 登録するファシリティ投資情報
     * @return 登録されたファシリティ投資情報を含むレスポンス
     */
    @PostMapping
    public ResponseEntity<FacilityInvestmentDto> create(@RequestBody FacilityInvestmentDto dto) {
        return ResponseEntity.ok(facilityInvestmentService.create(dto));
    }

    /**
     * 指定されたIDのファシリティ投資情報を更新します。
     *
     * @param id 更新対象のファシリティ投資ID
     * @param dto 更新するファシリティ投資情報
     * @return 更新されたファシリティ投資情報を含むレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<FacilityInvestmentDto> update(
            @PathVariable Long id,
            @RequestBody FacilityInvestmentDto dto) {
        return ResponseEntity.ok(facilityInvestmentService.update(id, dto));
    }

    /**
     * ファシリティ投資の投資金額を更新します。
     * 未実行の投資に対してのみ実行可能です。
     *
     * @param id ファシリティ投資ID
     * @param newAmount 新しい投資金額
     * @return 更新後のファシリティ投資情報を含むレスポンス
     */
    @PutMapping("/{id}/investment-amount")
    public ResponseEntity<FacilityInvestmentDto> updateInvestmentAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal newAmount) {
        return ResponseEntity.ok(facilityInvestmentService.updateInvestmentAmount(id, newAmount));
    }

    /**
     * ファシリティ投資の金額配分を更新します。
     * シンジケートローンの参加金融機関間での配分変更が発生した際に使用します。
     *
     * @param id ファシリティ投資ID
     * @param dto 更新する金額配分を含むファシリティ投資情報
     * @return 更新後のファシリティ投資情報を含むレスポンス
     */
    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<FacilityInvestmentDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody FacilityInvestmentDto dto) {
        return ResponseEntity.ok(facilityInvestmentService.updateAmountPie(id, dto.getAmountPie()));
    }

    /**
     * 指定されたIDのファシリティ投資を削除します。
     * 未実行の投資に対してのみ実行可能です。
     *
     * @param id 削除対象のファシリティ投資ID
     * @return 削除成功を示すレスポンス（204 No Content）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facilityInvestmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

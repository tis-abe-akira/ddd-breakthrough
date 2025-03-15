package com.syndicated_loan.syndicated_loan.feature.position;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.FacilityDto;
import com.syndicated_loan.syndicated_loan.common.dto.SharePieDto;
import com.syndicated_loan.syndicated_loan.common.service.FacilityService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ファシリティ（Facility）に関するREST APIのエンドポイントを提供するコントローラークラスです。
 * ファシリティの検索、登録、更新、削除、利用可能額の管理、シェア配分の更新などの操作を扱います。
 * ファシリティはシンジケートローンにおける個別の融資枠を表し、総額、期間、利用可能額などの情報を管理します。
 */
@RestController
@RequestMapping("/api/facilities")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    /**
     * 全てのファシリティ情報を取得します。
     *
     * @return 全ファシリティのリストを含むレスポンス
     */
    @GetMapping
    public ResponseEntity<List<FacilityDto>> findAll() {
        return ResponseEntity.ok(facilityService.findAll());
    }

    /**
     * 指定されたIDのファシリティ情報を取得します。
     *
     * @param id ファシリティID
     * @return ファシリティ情報を含むレスポンス。該当するファシリティが存在しない場合は404を返します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacilityDto> findById(@PathVariable Long id) {
        return facilityService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新規のファシリティを登録します。
     *
     * @param dto 登録するファシリティ情報
     * @return 登録されたファシリティ情報を含むレスポンス
     */
    @PostMapping
    public ResponseEntity<FacilityDto> create(@RequestBody FacilityDto dto) {
        return ResponseEntity.ok(facilityService.create(dto));
    }

    /**
     * 指定されたIDのファシリティ情報を更新します。
     *
     * @param id 更新対象のファシリティID
     * @param dto 更新するファシリティ情報
     * @return 更新されたファシリティ情報を含むレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<FacilityDto> update(
            @PathVariable Long id,
            @RequestBody FacilityDto dto) {
        return ResponseEntity.ok(facilityService.update(id, dto));
    }

    /**
     * 指定されたIDのファシリティを削除します。
     *
     * @param id 削除対象のファシリティID
     * @return 削除成功を示すレスポンス（204 No Content）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facilityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 指定されたシンジケート団に紐づくファシリティを検索します。
     *
     * @param syndicateId シンジケート団ID
     * @return 指定されたシンジケート団に紐づくファシリティのリストを含むレスポンス
     */
    @GetMapping("/search/syndicate/{syndicateId}")
    public ResponseEntity<List<FacilityDto>> findBySyndicate(
            @PathVariable Long syndicateId) {
        return ResponseEntity.ok(facilityService.findBySyndicate(syndicateId));
    }

    /**
     * 総額が指定された金額以上のファシリティを検索します。
     *
     * @param minAmount 最小総額
     * @return 条件に合致するファシリティのリストを含むレスポンス
     */
    @GetMapping("/search/total-amount")
    public ResponseEntity<List<FacilityDto>> findByTotalAmountGreaterThan(
            @RequestParam BigDecimal minAmount) {
        return ResponseEntity.ok(facilityService.findByTotalAmountGreaterThan(minAmount));
    }

    /**
     * 終了日が指定された日付より後のファシリティを検索します。
     *
     * @param date 検索基準日
     * @return 条件に合致するファシリティのリストを含むレスポンス
     */
    @GetMapping("/search/end-date")
    public ResponseEntity<List<FacilityDto>> findByEndDateAfter(
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(facilityService.findByEndDateAfter(date));
    }

    /**
     * 利用可能額が指定された金額以上のファシリティを検索します。
     *
     * @param minAmount 最小利用可能額
     * @return 条件に合致するファシリティのリストを含むレスポンス
     */
    @GetMapping("/search/available-amount")
    public ResponseEntity<List<FacilityDto>> findByAvailableAmountGreaterThan(
            @RequestParam BigDecimal minAmount) {
        return ResponseEntity.ok(facilityService.findByAvailableAmountGreaterThan(minAmount));
    }

    /**
     * ファシリティの利用可能額を更新します。
     * ドローダウンや返済などの取引が発生した際に利用可能額が変動します。
     *
     * @param id ファシリティID
     * @param newAvailableAmount 新しい利用可能額
     * @return 更新後のファシリティ情報を含むレスポンス
     */
    @PutMapping("/{id}/available-amount")
    public ResponseEntity<FacilityDto> updateAvailableAmount(
            @PathVariable Long id,
            @RequestBody BigDecimal newAvailableAmount) {
        return ResponseEntity.ok(facilityService.updateAvailableAmount(id, newAvailableAmount));
    }

    /**
     * ファシリティのシェア配分を更新します。
     * シンジケートローンの参加金融機関間でのシェアの変更や、シェアの売買が発生した際に更新されます。
     *
     * @param id ファシリティID
     * @param sharePieDto 新しいシェア配分情報
     * @return 更新後のファシリティ情報を含むレスポンス
     */
    @PutMapping("/{id}/share-pie")
    public ResponseEntity<FacilityDto> updateSharePie(
            @PathVariable Long id,
            @RequestBody SharePieDto sharePieDto) {
        return ResponseEntity.ok(facilityService.updateSharePie(id, sharePieDto));
    }
}

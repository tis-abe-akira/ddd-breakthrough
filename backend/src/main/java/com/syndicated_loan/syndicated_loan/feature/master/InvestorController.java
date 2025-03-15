package com.syndicated_loan.syndicated_loan.feature.master;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.service.InvestorService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 投資家（Investor）に関するREST APIのエンドポイントを提供するコントローラークラスです。
 * 投資家の検索、登録、更新、削除などの操作を扱います。
 */
@RestController
@RequestMapping("/api/investors")
public class InvestorController {

    private final InvestorService investorService;

    public InvestorController(InvestorService investorService) {
        this.investorService = investorService;
    }

    /**
     * 全ての投資家情報を取得します。
     *
     * @return 全投資家のリストを含むレスポンス
     */
    @GetMapping
    public ResponseEntity<List<InvestorDto>> findAll() {
        return ResponseEntity.ok(investorService.findAll());
    }

    /**
     * 指定されたIDの投資家情報を取得します。
     *
     * @param id 投資家ID
     * @return 投資家情報を含むレスポンス。該当する投資家が存在しない場合は404を返します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvestorDto> findById(@PathVariable Long id) {
        return investorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新規の投資家を登録します。
     *
     * @param dto 登録する投資家情報
     * @return 登録された投資家情報を含むレスポンス
     */
    @PostMapping
    public ResponseEntity<InvestorDto> create(@RequestBody InvestorDto dto) {
        return ResponseEntity.ok(investorService.create(dto));
    }

    /**
     * 指定されたIDの投資家情報を更新します。
     *
     * @param id 更新対象の投資家ID
     * @param dto 更新する投資家情報
     * @return 更新された投資家情報を含むレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<InvestorDto> update(
            @PathVariable Long id,
            @RequestBody InvestorDto dto) {
        return ResponseEntity.ok(investorService.update(id, dto));
    }

    /**
     * 指定されたIDの投資家を削除します。
     *
     * @param id 削除対象の投資家ID
     * @return 削除成功を示すレスポンス（204 No Content）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        investorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 指定された条件に基づいて投資家を検索します。
     *
     * @param name 投資家名（部分一致）
     * @param type 投資家タイプ
     * @param minCapacity 最小投資可能額
     * @return 検索条件に合致する投資家のリストを含むレスポンス
     */
    @GetMapping("/search")
    public ResponseEntity<List<InvestorDto>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minCapacity) {
        return ResponseEntity.ok(investorService.search(name, type, minCapacity));
    }
}

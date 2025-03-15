package com.syndicated_loan.syndicated_loan.feature.master;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.BorrowerDto;
import com.syndicated_loan.syndicated_loan.common.service.BorrowerService;

import java.util.List;

/**
 * 借入人（Borrower）に関するREST APIのエンドポイントを提供するコントローラークラスです。
 * 借入人の検索、登録、更新、削除などの操作を扱います。
 */
@RestController
@RequestMapping("/api/borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;

    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    /**
     * 全ての借入人情報を取得します。
     *
     * @return 全借入人のリストを含むレスポンス
     */
    @GetMapping
    public ResponseEntity<List<BorrowerDto>> findAll() {
        return ResponseEntity.ok(borrowerService.findAll());
    }

    /**
     * 指定されたIDの借入人情報を取得します。
     *
     * @param id 借入人ID
     * @return 借入人情報を含むレスポンス。該当する借入人が存在しない場合は404を返します。
     */
    @GetMapping("/{id}")
    public ResponseEntity<BorrowerDto> findById(@PathVariable Long id) {
        return borrowerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新規の借入人を登録します。
     *
     * @param dto 登録する借入人情報
     * @return 登録された借入人情報を含むレスポンス
     */
    @PostMapping
    public ResponseEntity<BorrowerDto> create(@Valid @RequestBody BorrowerDto dto) {
        return ResponseEntity.ok(borrowerService.create(dto));
    }

    /**
     * 指定されたIDの借入人情報を更新します。
     *
     * @param id 更新対象の借入人ID
     * @param dto 更新する借入人情報
     * @return 更新された借入人情報を含むレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<BorrowerDto> update(
            @PathVariable Long id,
            @Valid @RequestBody BorrowerDto dto) {
        return ResponseEntity.ok(borrowerService.update(id, dto));
    }

    /**
     * 指定されたIDの借入人を削除します。
     *
     * @param id 削除対象の借入人ID
     * @return 削除成功を示すレスポンス（204 No Content）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        borrowerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 指定された条件に基づいて借入人を検索します。
     *
     * @param name 借入人名（部分一致）
     * @param creditRating 信用格付
     * @param industry 業種
     * @return 検索条件に合致する借入人のリストを含むレスポンス
     */
    @GetMapping("/search")
    public ResponseEntity<List<BorrowerDto>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String creditRating,
            @RequestParam(required = false) String industry) {
        return ResponseEntity.ok(borrowerService.search(name, creditRating, industry));
    }
}

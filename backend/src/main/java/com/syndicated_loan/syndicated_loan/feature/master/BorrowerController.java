package com.syndicated_loan.syndicated_loan.feature.master;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.BorrowerDto;
import com.syndicated_loan.syndicated_loan.common.service.BorrowerService;

import java.util.List;

@RestController
@RequestMapping("/api/borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;

    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    @GetMapping
    public ResponseEntity<List<BorrowerDto>> findAll() {
        return ResponseEntity.ok(borrowerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowerDto> findById(@PathVariable Long id) {
        return borrowerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BorrowerDto> create(@Valid @RequestBody BorrowerDto dto) {
        return ResponseEntity.ok(borrowerService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BorrowerDto> update(
            @PathVariable Long id,
            @Valid @RequestBody BorrowerDto dto) {
        // 既存のエンティティのバージョン情報を取得するために、まず既存のエンティティを取得
        BorrowerDto existingDto = borrowerService.findById(id)
                .orElseThrow(() -> new RuntimeException("借入人が見つかりません: ID=" + id));

        // DTOにIDとバージョンを設定（クライアントから送られてこない場合に備えて）
        dto.setId(id);
        dto.setVersion(existingDto.getVersion());

        return ResponseEntity.ok(borrowerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        borrowerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<BorrowerDto>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String creditRating,
            @RequestParam(required = false) String industry) {
        return ResponseEntity.ok(borrowerService.search(name, creditRating, industry));
    }
}

package com.syndicated_loan.syndicated_loan.feature.position;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.SharePieDto;
import com.syndicated_loan.syndicated_loan.common.service.SharePieService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/share-pies")
public class SharePieController {

    private final SharePieService sharePieService;

    public SharePieController(SharePieService sharePieService) {
        this.sharePieService = sharePieService;
    }

    @GetMapping
    public ResponseEntity<List<SharePieDto>> findAll() {
        return ResponseEntity.ok(sharePieService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SharePieDto> findById(@PathVariable Long id) {
        return sharePieService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SharePieDto> create(@RequestBody SharePieDto dto) {
        return ResponseEntity.ok(sharePieService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SharePieDto> update(
            @PathVariable Long id,
            @RequestBody SharePieDto dto) {
        return ResponseEntity.ok(sharePieService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sharePieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/investors/{investorId}/share")
    public ResponseEntity<BigDecimal> getInvestorShare(
            @PathVariable Long id,
            @PathVariable Long investorId) {
        return ResponseEntity.ok(sharePieService.getInvestorShare(id, investorId));
    }

    @PutMapping("/{id}/shares")
    public ResponseEntity<SharePieDto> updateShares(
            @PathVariable Long id,
            @RequestBody Map<Long, BigDecimal> shares) {
        return ResponseEntity.ok(sharePieService.updateShares(id, shares));
    }

    @GetMapping("/search/minimum-share")
    public ResponseEntity<List<SharePieDto>> findByMinimumShare(
            @RequestParam BigDecimal minShare) {
        return ResponseEntity.ok(sharePieService.findByMinimumShare(minShare));
    }

    @GetMapping("/search/investor/{investorId}")
    public ResponseEntity<List<SharePieDto>> findByInvestorId(
            @PathVariable Long investorId) {
        return ResponseEntity.ok(sharePieService.findByInvestorId(investorId));
    }
}

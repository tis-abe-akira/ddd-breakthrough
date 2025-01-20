package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.DrawdownDto;
import com.syndicated_loan.syndicated_loan.common.service.DrawdownService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/drawdowns")
public class DrawdownController {

    private final DrawdownService drawdownService;

    public DrawdownController(DrawdownService drawdownService) {
        this.drawdownService = drawdownService;
    }

    @GetMapping
    public ResponseEntity<List<DrawdownDto>> findAll() {
        return ResponseEntity.ok(drawdownService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DrawdownDto> findById(@PathVariable Long id) {
        return drawdownService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<DrawdownDto>> findByRelatedFacility(@PathVariable Long facilityId) {
        return ResponseEntity.ok(drawdownService.findByRelatedFacility(facilityId));
    }

    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<DrawdownDto>> findByDrawdownAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(drawdownService.findByDrawdownAmountGreaterThan(amount));
    }

    @GetMapping("/facility/{facilityId}/amount-greater-than/{amount}")
    public ResponseEntity<List<DrawdownDto>> findByRelatedFacilityAndDrawdownAmountGreaterThan(
            @PathVariable Long facilityId,
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(
                drawdownService.findByRelatedFacilityAndDrawdownAmountGreaterThan(facilityId, amount));
    }

    @PostMapping
    public ResponseEntity<DrawdownDto> create(@RequestBody DrawdownDto dto) {
        return ResponseEntity.ok(drawdownService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DrawdownDto> update(
            @PathVariable Long id,
            @RequestBody DrawdownDto dto) {
        return ResponseEntity.ok(drawdownService.update(id, dto));
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<DrawdownDto> executeDrawdown(@PathVariable Long id) {
        return ResponseEntity.ok(drawdownService.executeDrawdown(id));
    }

    @PutMapping("/{id}/drawdown-amount")
    public ResponseEntity<DrawdownDto> updateDrawdownAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal newAmount) {
        return ResponseEntity.ok(drawdownService.updateDrawdownAmount(id, newAmount));
    }

    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<DrawdownDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody DrawdownDto dto) {
        return ResponseEntity.ok(drawdownService.updateAmountPie(id, dto.getAmountPie()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        drawdownService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.FacilityInvestmentDto;
import com.syndicated_loan.syndicated_loan.common.service.FacilityInvestmentService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/facility-investments")
public class FacilityInvestmentController {

    private final FacilityInvestmentService facilityInvestmentService;

    public FacilityInvestmentController(FacilityInvestmentService facilityInvestmentService) {
        this.facilityInvestmentService = facilityInvestmentService;
    }

    @GetMapping
    public ResponseEntity<List<FacilityInvestmentDto>> findAll() {
        return ResponseEntity.ok(facilityInvestmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacilityInvestmentDto> findById(@PathVariable Long id) {
        return facilityInvestmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/investor/{investorId}")
    public ResponseEntity<List<FacilityInvestmentDto>> findByInvestor(@PathVariable Long investorId) {
        return ResponseEntity.ok(facilityInvestmentService.findByInvestor(investorId));
    }

    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<FacilityInvestmentDto>> findByInvestmentAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(facilityInvestmentService.findByInvestmentAmountGreaterThan(amount));
    }

    @PostMapping
    public ResponseEntity<FacilityInvestmentDto> create(@RequestBody FacilityInvestmentDto dto) {
        return ResponseEntity.ok(facilityInvestmentService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacilityInvestmentDto> update(
            @PathVariable Long id,
            @RequestBody FacilityInvestmentDto dto) {
        return ResponseEntity.ok(facilityInvestmentService.update(id, dto));
    }

    @PutMapping("/{id}/investment-amount")
    public ResponseEntity<FacilityInvestmentDto> updateInvestmentAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal newAmount) {
        return ResponseEntity.ok(facilityInvestmentService.updateInvestmentAmount(id, newAmount));
    }

    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<FacilityInvestmentDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody FacilityInvestmentDto dto) {
        return ResponseEntity.ok(facilityInvestmentService.updateAmountPie(id, dto.getAmountPie()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facilityInvestmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

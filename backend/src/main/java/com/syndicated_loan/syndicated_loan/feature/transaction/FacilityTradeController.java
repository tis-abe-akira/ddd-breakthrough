package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.FacilityTradeDto;
import com.syndicated_loan.syndicated_loan.common.service.FacilityTradeService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/facility-trades")
public class FacilityTradeController {

    private final FacilityTradeService facilityTradeService;

    public FacilityTradeController(FacilityTradeService facilityTradeService) {
        this.facilityTradeService = facilityTradeService;
    }

    @GetMapping
    public ResponseEntity<List<FacilityTradeDto>> findAll() {
        return ResponseEntity.ok(facilityTradeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacilityTradeDto> findById(@PathVariable Long id) {
        return facilityTradeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<FacilityTradeDto>> findBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(facilityTradeService.findBySeller(sellerId));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<FacilityTradeDto>> findByBuyer(@PathVariable Long buyerId) {
        return ResponseEntity.ok(facilityTradeService.findByBuyer(buyerId));
    }

    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<FacilityTradeDto>> findByTradeAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(facilityTradeService.findByTradeAmountGreaterThan(amount));
    }

    @GetMapping("/investor/{investorId}")
    public ResponseEntity<List<FacilityTradeDto>> findBySellerOrBuyer(@PathVariable Long investorId) {
        return ResponseEntity.ok(facilityTradeService.findBySellerOrBuyer(investorId));
    }

    @PostMapping
    public ResponseEntity<FacilityTradeDto> create(@RequestBody FacilityTradeDto dto) {
        return ResponseEntity.ok(facilityTradeService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacilityTradeDto> update(
            @PathVariable Long id,
            @RequestBody FacilityTradeDto dto) {
        return ResponseEntity.ok(facilityTradeService.update(id, dto));
    }

    @PutMapping("/{id}/trade-amount")
    public ResponseEntity<FacilityTradeDto> updateTradeAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal newAmount) {
        return ResponseEntity.ok(facilityTradeService.updateTradeAmount(id, newAmount));
    }

    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<FacilityTradeDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody FacilityTradeDto dto) {
        return ResponseEntity.ok(facilityTradeService.updateAmountPie(id, dto.getAmountPie()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facilityTradeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

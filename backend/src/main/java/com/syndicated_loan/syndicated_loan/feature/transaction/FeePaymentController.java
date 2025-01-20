package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.FeePaymentDto;
import com.syndicated_loan.syndicated_loan.common.service.FeePaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fee-payments")
public class FeePaymentController {

    private final FeePaymentService feePaymentService;

    public FeePaymentController(FeePaymentService feePaymentService) {
        this.feePaymentService = feePaymentService;
    }

    @GetMapping
    public ResponseEntity<List<FeePaymentDto>> findAll() {
        return ResponseEntity.ok(feePaymentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeePaymentDto> findById(@PathVariable Long id) {
        return feePaymentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<FeePaymentDto>> findByFacility(@PathVariable Long facilityId) {
        return ResponseEntity.ok(feePaymentService.findByFacility(facilityId));
    }

    @GetMapping("/type/{feeType}")
    public ResponseEntity<List<FeePaymentDto>> findByFeeType(@PathVariable String feeType) {
        return ResponseEntity.ok(feePaymentService.findByFeeType(feeType));
    }

    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<FeePaymentDto>> findByPaymentAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(feePaymentService.findByPaymentAmountGreaterThan(amount));
    }

    @GetMapping("/facility/{facilityId}/type/{feeType}")
    public ResponseEntity<List<FeePaymentDto>> findByFacilityAndFeeType(
            @PathVariable Long facilityId,
            @PathVariable String feeType) {
        return ResponseEntity.ok(feePaymentService.findByFacilityAndFeeType(facilityId, feeType));
    }

    @PostMapping
    public ResponseEntity<FeePaymentDto> create(@RequestBody FeePaymentDto dto) {
        return ResponseEntity.ok(feePaymentService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeePaymentDto> update(
            @PathVariable Long id,
            @RequestBody FeePaymentDto dto) {
        return ResponseEntity.ok(feePaymentService.update(id, dto));
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<FeePaymentDto> executeFeePayment(@PathVariable Long id) {
        return ResponseEntity.ok(feePaymentService.executeFeePayment(id));
    }

    @PutMapping("/{id}/payment-amount")
    public ResponseEntity<FeePaymentDto> updatePaymentAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal newAmount) {
        return ResponseEntity.ok(feePaymentService.updatePaymentAmount(id, newAmount));
    }

    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<FeePaymentDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody FeePaymentDto dto) {
        return ResponseEntity.ok(feePaymentService.updateAmountPie(id, dto.getAmountPie()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feePaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

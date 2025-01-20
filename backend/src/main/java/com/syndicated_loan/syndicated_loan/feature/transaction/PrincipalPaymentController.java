package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.PrincipalPaymentDto;
import com.syndicated_loan.syndicated_loan.common.service.PrincipalPaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/principal-payments")
public class PrincipalPaymentController {

    private final PrincipalPaymentService principalPaymentService;

    public PrincipalPaymentController(PrincipalPaymentService principalPaymentService) {
        this.principalPaymentService = principalPaymentService;
    }

    @GetMapping
    public ResponseEntity<List<PrincipalPaymentDto>> findAll() {
        return ResponseEntity.ok(principalPaymentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrincipalPaymentDto> findById(@PathVariable Long id) {
        return principalPaymentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<PrincipalPaymentDto>> findByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(principalPaymentService.findByLoan(loanId));
    }

    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<PrincipalPaymentDto>> findByPaymentAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(principalPaymentService.findByPaymentAmountGreaterThan(amount));
    }

    @GetMapping("/period")
    public ResponseEntity<List<PrincipalPaymentDto>> findByDateBetween(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(principalPaymentService.findByDateBetween(startDate, endDate));
    }

    @GetMapping("/loan/{loanId}/period")
    public ResponseEntity<List<PrincipalPaymentDto>> findByLoanAndDateBetween(
            @PathVariable Long loanId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(
                principalPaymentService.findByLoanAndDateBetween(loanId, startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<PrincipalPaymentDto> create(@RequestBody PrincipalPaymentDto dto) {
        return ResponseEntity.ok(principalPaymentService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrincipalPaymentDto> update(
            @PathVariable Long id,
            @RequestBody PrincipalPaymentDto dto) {
        return ResponseEntity.ok(principalPaymentService.update(id, dto));
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<PrincipalPaymentDto> executePrincipalPayment(@PathVariable Long id) {
        return ResponseEntity.ok(principalPaymentService.executePrincipalPayment(id));
    }

    @PutMapping("/{id}/payment-amount")
    public ResponseEntity<PrincipalPaymentDto> updatePaymentAmount(
            @PathVariable Long id,
            @RequestParam BigDecimal newAmount) {
        return ResponseEntity.ok(principalPaymentService.updatePaymentAmount(id, newAmount));
    }

    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<PrincipalPaymentDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody PrincipalPaymentDto dto) {
        return ResponseEntity.ok(principalPaymentService.updateAmountPie(id, dto.getAmountPie()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        principalPaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

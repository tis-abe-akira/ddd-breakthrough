package com.syndicated_loan.syndicated_loan.feature.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.InterestPaymentDto;
import com.syndicated_loan.syndicated_loan.common.service.InterestPaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/interest-payments")
public class InterestPaymentController {

    private final InterestPaymentService interestPaymentService;

    public InterestPaymentController(InterestPaymentService interestPaymentService) {
        this.interestPaymentService = interestPaymentService;
    }

    @GetMapping
    public ResponseEntity<List<InterestPaymentDto>> findAll() {
        return ResponseEntity.ok(interestPaymentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterestPaymentDto> findById(@PathVariable Long id) {
        return interestPaymentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<InterestPaymentDto>> findByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(interestPaymentService.findByLoan(loanId));
    }

    @GetMapping("/rate-greater-than/{rate}")
    public ResponseEntity<List<InterestPaymentDto>> findByInterestRateGreaterThan(
            @PathVariable BigDecimal rate) {
        return ResponseEntity.ok(interestPaymentService.findByInterestRateGreaterThan(rate));
    }

    @GetMapping("/amount-greater-than/{amount}")
    public ResponseEntity<List<InterestPaymentDto>> findByPaymentAmountGreaterThan(
            @PathVariable BigDecimal amount) {
        return ResponseEntity.ok(interestPaymentService.findByPaymentAmountGreaterThan(amount));
    }

    @GetMapping("/period")
    public ResponseEntity<List<InterestPaymentDto>> findByInterestStartDateBetween(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(interestPaymentService.findByInterestStartDateBetween(startDate, endDate));
    }

    @GetMapping("/loan/{loanId}/period")
    public ResponseEntity<List<InterestPaymentDto>> findByLoanAndInterestStartDateBetween(
            @PathVariable Long loanId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(
                interestPaymentService.findByLoanAndInterestStartDateBetween(loanId, startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<InterestPaymentDto> create(@RequestBody InterestPaymentDto dto) {
        return ResponseEntity.ok(interestPaymentService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InterestPaymentDto> update(
            @PathVariable Long id,
            @RequestBody InterestPaymentDto dto) {
        return ResponseEntity.ok(interestPaymentService.update(id, dto));
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<InterestPaymentDto> executeInterestPayment(@PathVariable Long id) {
        return ResponseEntity.ok(interestPaymentService.executeInterestPayment(id));
    }

    @PutMapping("/{id}/payment-amount")
    public ResponseEntity<InterestPaymentDto> calculateAndUpdatePaymentAmount(@PathVariable Long id) {
        return ResponseEntity.ok(interestPaymentService.calculateAndUpdatePaymentAmount(id));
    }

    @PutMapping("/{id}/amount-pie")
    public ResponseEntity<InterestPaymentDto> updateAmountPie(
            @PathVariable Long id,
            @RequestBody InterestPaymentDto dto) {
        return ResponseEntity.ok(interestPaymentService.updateAmountPie(id, dto.getAmountPie()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interestPaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.syndicated_loan.syndicated_loan.feature.master;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.SyndicateDto;
import com.syndicated_loan.syndicated_loan.common.service.SyndicateService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/syndicates")
public class SyndicateController {

    private final SyndicateService syndicateService;

    public SyndicateController(SyndicateService syndicateService) {
        this.syndicateService = syndicateService;
    }

    @GetMapping
    public ResponseEntity<List<SyndicateDto>> findAll() {
        return ResponseEntity.ok(syndicateService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SyndicateDto> findById(@PathVariable Long id) {
        return syndicateService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SyndicateDto> create(@RequestBody SyndicateDto dto) {
        return ResponseEntity.ok(syndicateService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SyndicateDto> update(
            @PathVariable Long id,
            @RequestBody SyndicateDto dto) {
        return ResponseEntity.ok(syndicateService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        syndicateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/lead-bank/{leadBankId}")
    public ResponseEntity<List<SyndicateDto>> findByLeadBank(@PathVariable Long leadBankId) {
        return ResponseEntity.ok(syndicateService.findByLeadBank(leadBankId));
    }

    @GetMapping("/search/member/{memberId}")
    public ResponseEntity<List<SyndicateDto>> findByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(syndicateService.findByMember(memberId));
    }

    @GetMapping("/search/commitment")
    public ResponseEntity<List<SyndicateDto>> findByTotalCommitmentGreaterThan(
            @RequestParam BigDecimal minAmount) {
        return ResponseEntity.ok(syndicateService.findByTotalCommitmentGreaterThan(minAmount));
    }

    @PostMapping("/{id}/members/{investorId}")
    public ResponseEntity<SyndicateDto> addMember(
            @PathVariable Long id,
            @PathVariable Long investorId) {
        return ResponseEntity.ok(syndicateService.addMember(id, investorId));
    }

    @DeleteMapping("/{id}/members/{investorId}")
    public ResponseEntity<SyndicateDto> removeMember(
            @PathVariable Long id,
            @PathVariable Long investorId) {
        return ResponseEntity.ok(syndicateService.removeMember(id, investorId));
    }
}

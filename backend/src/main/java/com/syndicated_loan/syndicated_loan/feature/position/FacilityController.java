package com.syndicated_loan.syndicated_loan.feature.position;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.FacilityDto;
import com.syndicated_loan.syndicated_loan.common.dto.SharePieDto;
import com.syndicated_loan.syndicated_loan.common.service.FacilityService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/facilities")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    public ResponseEntity<List<FacilityDto>> findAll() {
        return ResponseEntity.ok(facilityService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacilityDto> findById(@PathVariable Long id) {
        return facilityService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FacilityDto> create(@RequestBody FacilityDto dto) {
        return ResponseEntity.ok(facilityService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacilityDto> update(
            @PathVariable Long id,
            @RequestBody FacilityDto dto) {
        return ResponseEntity.ok(facilityService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        facilityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/syndicate/{syndicateId}")
    public ResponseEntity<List<FacilityDto>> findBySyndicate(
            @PathVariable Long syndicateId) {
        return ResponseEntity.ok(facilityService.findBySyndicate(syndicateId));
    }

    @GetMapping("/search/total-amount")
    public ResponseEntity<List<FacilityDto>> findByTotalAmountGreaterThan(
            @RequestParam BigDecimal minAmount) {
        return ResponseEntity.ok(facilityService.findByTotalAmountGreaterThan(minAmount));
    }

    @GetMapping("/search/end-date")
    public ResponseEntity<List<FacilityDto>> findByEndDateAfter(
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(facilityService.findByEndDateAfter(date));
    }

    @GetMapping("/search/available-amount")
    public ResponseEntity<List<FacilityDto>> findByAvailableAmountGreaterThan(
            @RequestParam BigDecimal minAmount) {
        return ResponseEntity.ok(facilityService.findByAvailableAmountGreaterThan(minAmount));
    }

    @PutMapping("/{id}/available-amount")
    public ResponseEntity<FacilityDto> updateAvailableAmount(
            @PathVariable Long id,
            @RequestBody BigDecimal newAvailableAmount) {
        return ResponseEntity.ok(facilityService.updateAvailableAmount(id, newAvailableAmount));
    }

    @PutMapping("/{id}/share-pie")
    public ResponseEntity<FacilityDto> updateSharePie(
            @PathVariable Long id,
            @RequestBody SharePieDto sharePieDto) {
        return ResponseEntity.ok(facilityService.updateSharePie(id, sharePieDto));
    }
}

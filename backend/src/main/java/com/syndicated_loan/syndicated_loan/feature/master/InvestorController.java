package com.syndicated_loan.syndicated_loan.feature.master;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syndicated_loan.syndicated_loan.common.dto.InvestorDto;
import com.syndicated_loan.syndicated_loan.common.service.InvestorService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/investors")
public class InvestorController {

    private final InvestorService investorService;

    public InvestorController(InvestorService investorService) {
        this.investorService = investorService;
    }

    @GetMapping
    public ResponseEntity<List<InvestorDto>> findAll() {
        return ResponseEntity.ok(investorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestorDto> findById(@PathVariable Long id) {
        return investorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InvestorDto> create(@RequestBody InvestorDto dto) {
        return ResponseEntity.ok(investorService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestorDto> update(
            @PathVariable Long id,
            @RequestBody InvestorDto dto) {
        return ResponseEntity.ok(investorService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        investorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<InvestorDto>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minCapacity) {
        return ResponseEntity.ok(investorService.search(name, type, minCapacity));
    }
}

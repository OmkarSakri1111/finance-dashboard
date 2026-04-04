package com.financeapp.financeservice.controller;

import com.financeapp.financeservice.dto.FinanceDtos;
import com.financeapp.financeservice.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    // ── Records ───────────────────────────────────────────────────────────────

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<FinanceDtos.RecordResponse>> getAllRecords(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(financeService.getAllRecords(type, category, from, to));
    }

    @GetMapping("/records/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<FinanceDtos.RecordResponse> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(financeService.getRecordById(id));
    }

    @PostMapping("/records")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<FinanceDtos.RecordResponse> createRecord(
            @Valid @RequestBody FinanceDtos.RecordRequest request,
            Principal principal) {
        String email = (principal != null) ? principal.getName() : "system";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(financeService.createRecord(request, email));
    }

    @PutMapping("/records/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<FinanceDtos.RecordResponse> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinanceDtos.RecordRequest request) {
        return ResponseEntity.ok(financeService.updateRecord(id, request));
    }

    @DeleteMapping("/records/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteRecord(@PathVariable Long id) {
        financeService.deleteRecord(id);
        return ResponseEntity.ok(Map.of(
                "message", "Financial record deleted successfully",
                "id", id.toString()));
    }

    // ── Dashboard Summary ─────────────────────────────────────────────────────

    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<FinanceDtos.DashboardSummary> getDashboardSummary() {
        return ResponseEntity.ok(financeService.getDashboardSummary());
    }

    @GetMapping("/dashboard/category-breakdown")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> getCategoryBreakdown(
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(financeService.getCategoryBreakdown(type));
    }

    // ── Health ────────────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "finance-service"));
    }
}

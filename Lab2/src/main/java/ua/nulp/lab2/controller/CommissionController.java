package ua.nulp.lab2.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nulp.lab2.dto.commission.CommissionCreateRequest;
import ua.nulp.lab2.dto.commission.CommissionResponse;
import ua.nulp.lab2.service.CommissionService;

import java.util.List;

@RestController
@RequestMapping("/api/commissions")
public class CommissionController {
    private final CommissionService commissionService;

    public CommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    @PostMapping
    public ResponseEntity<CommissionResponse> createCommission(@RequestBody @Valid CommissionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commissionService.createCommission(request));
    }

    @GetMapping
    public ResponseEntity<List<CommissionResponse>> getAllCommissions() {
        return ResponseEntity.ok(commissionService.getAllCommissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommissionResponse> getCommissionById(@PathVariable Long id) {
        return commissionService.getCommissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

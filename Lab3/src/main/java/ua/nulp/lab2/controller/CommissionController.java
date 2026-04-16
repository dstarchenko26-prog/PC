package ua.nulp.lab2.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.nulp.lab2.dto.commission.CommissionResponse;
import ua.nulp.lab2.service.PaymentService;

@RestController
@RequestMapping("/api/commissions")
public class CommissionController {

    private final PaymentService paymentService;

    public CommissionController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ========================================================================
    // ЕНДПОІНТИ ПРОДАВЦЯ
    // ========================================================================

    // READ: Отримання історії списаних комісій для поточного продавця
    @GetMapping("/my")
    public ResponseEntity<Page<CommissionResponse>> getMyCommissions(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getMyCommissions(pageable));
    }
}
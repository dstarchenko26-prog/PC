package ua.nulp.lab2.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import ua.nulp.lab2.dto.payment.PaymentRequest;
import ua.nulp.lab2.dto.payment.PaymentResponse;
import ua.nulp.lab2.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // CREATE: Оплатити замовлення (може повернути FAILED або COMPLETED)
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody @Valid PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request));
    }

    // ACTION: Фінансове повернення (COMPLETED -> REFUNDED)
    @PostMapping("/order/{orderId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.refundPayment(orderId));
    }

    // READ: Отримати чек за ID замовлення
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }
}
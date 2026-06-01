package ua.nulp.order_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nulp.order_service.dto.payment.PaymentRequest;
import ua.nulp.order_service.dto.payment.PaymentResponse;
import ua.nulp.order_service.service.PaymentService;

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
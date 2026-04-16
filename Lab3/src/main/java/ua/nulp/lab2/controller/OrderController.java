package ua.nulp.lab2.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nulp.lab2.dto.order.*;
import ua.nulp.lab2.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ========================================================================
    // 1. ЕНДПОІНТИ ПОКУПЦЯ (Клієнтська частина)
    // ========================================================================

    // CREATE: Оформлення замовлення (розбиває кошик на кілька замовлень за продавцями)
    @PostMapping
    public ResponseEntity<List<OrderResponse>> createOrders(@RequestBody @Valid OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrders(request));
    }

    // UPDATE: Оновити склад замовлення до оплати (товари, кількість)
    // Працює тільки для статусу CREATED
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @RequestBody @Valid OrderUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    // READ: Отримати історію власних замовлень (Пагінація)
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getMyOrders(page, size));
    }

    // READ: Отримати деталі конкретного замовлення за ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    // ACTION: Запит на повернення товару (Тільки для DELIVERED)
    @PostMapping("/{id}/return")
    public ResponseEntity<OrderResponse> requestReturn(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.requestReturn(id));
    }

    // ========================================================================
    // 2. ЕНДПОІНТИ ПРОДАВЦЯ ТА АДМІНІСТРАТОРА
    // ========================================================================

    // READ: Отримати всі замовлення, що стосуються товарів цього продавця
    @GetMapping("/seller")
    public ResponseEntity<Page<OrderResponse>> getSellerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getOrdersForMySellerProfile(page, size));
    }

    // UPDATE: Зміна статусу замовлення (Тільки для ADMIN або для скасування покупцем)
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }
}
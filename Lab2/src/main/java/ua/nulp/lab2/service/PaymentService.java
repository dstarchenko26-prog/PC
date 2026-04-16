package ua.nulp.lab2.service;

import org.springframework.stereotype.Service;
import ua.nulp.lab2.dto.payment.PaymentCreateRequest;
import ua.nulp.lab2.dto.payment.PaymentResponse;
import ua.nulp.lab2.model.Order;
import ua.nulp.lab2.model.enums.OrderStatus;
import ua.nulp.lab2.model.Payment;
import ua.nulp.lab2.model.enums.PaymentStatus;
import ua.nulp.lab2.repository.OrderRepository;
import ua.nulp.lab2.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public PaymentResponse processPayment(PaymentCreateRequest request) {
        // Перевіряємо, чи існує замовлення
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Замовлення не знайдено"));

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.COMPLETED); // Імітація успішної оплати
        payment.setProcessedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Оновлюємо статус замовлення
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return mapToResponse(savedPayment);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<PaymentResponse> getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(this::mapToResponse);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setProcessedAt(payment.getProcessedAt());
        return response;
    }
}

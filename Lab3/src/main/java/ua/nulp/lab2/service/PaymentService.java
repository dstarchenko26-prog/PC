package ua.nulp.lab2.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nulp.lab2.dto.commission.CommissionResponse;
import ua.nulp.lab2.dto.payment.PaymentResponse;
import ua.nulp.lab2.exception.ResourceConflictException;
import ua.nulp.lab2.exception.ResourceNotFoundException;
import ua.nulp.lab2.model.*;
import ua.nulp.lab2.model.enums.OrderStatus;
import ua.nulp.lab2.model.enums.PaymentStatus;
import ua.nulp.lab2.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ua.nulp.lab2.dto.payment.PaymentRequest;
import ua.nulp.lab2.model.*;
import ua.nulp.lab2.model.enums.Role;
import ua.nulp.lab2.repository.*;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CommissionRepository commissionRepository;
    private final OrderRepository orderRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;

    // Ставка комісії маркетплейсу: 5%
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.05");

    public PaymentService(PaymentRepository paymentRepository, CommissionRepository commissionRepository,
                          OrderRepository orderRepository, SellerProfileRepository sellerProfileRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.commissionRepository = commissionRepository;
        this.orderRepository = orderRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.userRepository = userRepository;
    }

    // =================================================================================
    // 1. ПРОЦЕС ОПЛАТИ (PENDING -> COMPLETED або FAILED)
    // =================================================================================
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User buyer = userRepository.findByEmail(email).orElseThrow();

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Замовлення не знайдено"));

        if (!order.getBuyerId().equals(buyer.getId())) {
            throw new AccessDeniedException("Ви не можете оплатити чуже замовлення");
        }
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ResourceConflictException("Замовлення вже оплачено або скасовано");
        }

        // 1. Створюємо платіж у статусі PENDING
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        // 2. Симуляція відхилення банком (FAILED)
        if (!request.isSimulateSuccess()) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            throw new ResourceConflictException("Оплату відхилено банком (недостатньо коштів)");
        }

        // 3. Якщо успіх (COMPLETED)
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProcessedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Фінансова математика
        BigDecimal totalAmount = order.getTotalAmount();
        BigDecimal commissionAmount = totalAmount.multiply(COMMISSION_RATE);
        BigDecimal netProfit = totalAmount.subtract(commissionAmount);

        // Оновлюємо баланс продавця
        SellerProfile seller = sellerProfileRepository.findById(order.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Профіль продавця не знайдено"));
        seller.setBalance(seller.getBalance().add(netProfit));
        sellerProfileRepository.save(seller);

        // Зберігаємо інформацію про комісію
        Commission commission = new Commission();
        commission.setPaymentId(payment.getId());
        commission.setSellerId(seller.getId());
        commission.setCommissionRate(COMMISSION_RATE);
        commission.setAmountWithheld(commissionAmount);
        commission.setCreatedAt(LocalDateTime.now());
        commissionRepository.save(commission);

        return mapToPaymentResponse(payment);
    }

    // =================================================================================
    // 2. ФІНАНСОВЕ ПОВЕРНЕННЯ (COMPLETED -> REFUNDED)
    // =================================================================================
    @Transactional
    public PaymentResponse refundPayment(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Тільки адміністратор може проводити фінансові повернення");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Платіж не знайдено"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new ResourceConflictException("Неможливо повернути кошти: платіж не був успішним");
        }

        Order order = orderRepository.findById(orderId).orElseThrow();

        // Фінансова математика для повернення
        BigDecimal totalAmount = order.getTotalAmount();
        BigDecimal commissionAmount = totalAmount.multiply(COMMISSION_RATE);
        BigDecimal netProfit = totalAmount.subtract(commissionAmount);

        // 1. Списуємо гроші з балансу продавця
        SellerProfile seller = sellerProfileRepository.findById(order.getSellerId()).orElseThrow();
        seller.setBalance(seller.getBalance().subtract(netProfit));
        sellerProfileRepository.save(seller);

        // 2. Створюємо МІНУСОВУ комісію (для бухгалтерського балансу платформи)
        Commission refundCommission = new Commission();
        refundCommission.setPaymentId(payment.getId());
        refundCommission.setSellerId(seller.getId());
        refundCommission.setCommissionRate(COMMISSION_RATE);
        refundCommission.setAmountWithheld(commissionAmount.negate()); // Робимо число від'ємним!
        refundCommission.setCreatedAt(LocalDateTime.now());
        commissionRepository.save(refundCommission);

        // 3. Оновлюємо статус платежу
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setProcessedAt(LocalDateTime.now());

        return mapToPaymentResponse(paymentRepository.save(payment));
    }

    // =================================================================================
    // READ: Отримання інформації
    // =================================================================================

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Платіж не знайдено"));
        return mapToPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<CommissionResponse> getMyCommissions(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        SellerProfile seller = sellerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ви не є продавцем"));

        return commissionRepository.findBySellerId(seller.getId(), pageable)
                .map(this::mapToCommissionResponse);
    }

    // Маппери
    private PaymentResponse mapToPaymentResponse(Payment p) {
        PaymentResponse response = new PaymentResponse();
        response.setId(p.getId());
        response.setOrderId(p.getOrderId());
        response.setAmount(p.getAmount());
        response.setPaymentMethod(p.getPaymentMethod());
        response.setStatus(p.getStatus());
        response.setProcessedAt(p.getProcessedAt());
        return response;
    }

    private CommissionResponse mapToCommissionResponse(Commission c) {
        CommissionResponse response = new CommissionResponse();
        response.setId(c.getId());
        response.setPaymentId(c.getPaymentId());
        response.setCommissionRate(c.getCommissionRate());
        response.setAmountWithheld(c.getAmountWithheld());
        response.setCreatedAt(c.getCreatedAt());
        return response;
    }
}
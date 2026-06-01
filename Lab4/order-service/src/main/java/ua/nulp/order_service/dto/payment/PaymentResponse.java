package ua.nulp.order_service.dto.payment;

import lombok.Data;
import ua.nulp.order_service.model.enums.PaymentMethod;
import ua.nulp.order_service.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime processedAt;
}
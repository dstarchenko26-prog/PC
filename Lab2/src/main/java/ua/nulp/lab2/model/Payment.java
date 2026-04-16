package ua.nulp.lab2.model;

import lombok.Data;
import ua.nulp.lab2.model.enums.PaymentMethod;
import ua.nulp.lab2.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Payment {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime processedAt;
}
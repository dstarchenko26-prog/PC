package ua.nulp.lab2.model;

import jakarta.persistence.*;
        import lombok.Data;
import ua.nulp.lab2.model.enums.PaymentMethod;
import ua.nulp.lab2.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
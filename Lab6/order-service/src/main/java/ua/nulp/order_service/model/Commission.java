package ua.nulp.order_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "commissions")
public class Commission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "commission_rate", nullable = false, precision = 4, scale = 2)
    private BigDecimal commissionRate; // Напр. 0.05

    @Column(name = "amount_withheld", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountWithheld; // Сума, яку забирає маркетплейс

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
package ua.nulp.order_service.model;

import jakarta.persistence.*;
import lombok.Data;
import ua.nulp.order_service.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;


    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Жорсткий зв'язок з позиціями замовлення (CascadeType.ALL зберігає їх автоматично)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // Допоміжний метод для правильного зв'язування
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
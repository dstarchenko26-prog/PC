package ua.nulp.lab2.model;

import lombok.Data;
import java.math.BigDecimal;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Зв'язок назад до замовлення
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    // Фіксуємо ціну на момент покупки! (щоб зміна ціни товару в майбутньому не змінила історію замовлень)
    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;
}
package ua.nulp.lab2.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItem {
    private Long id;
    private Long productId;
    private Integer quantity;
    private BigDecimal priceAtPurchase; // Фіксуємо ціну на момент покупки
}

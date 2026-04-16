package ua.nulp.lab2.dto.order;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
}

package ua.nulp.lab2.dto.order;

import lombok.Data;
import ua.nulp.lab2.model.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long buyerId;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}

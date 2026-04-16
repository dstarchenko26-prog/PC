package ua.nulp.lab2.model;

import lombok.Data;
import ua.nulp.lab2.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Order {
    private Long id;
    private Long buyerId;
    private OrderStatus status;
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}

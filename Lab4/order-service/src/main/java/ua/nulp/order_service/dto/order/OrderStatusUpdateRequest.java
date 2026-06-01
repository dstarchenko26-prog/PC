package ua.nulp.order_service.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.nulp.order_service.model.enums.OrderStatus;

@Data
public class OrderStatusUpdateRequest {

    @NotNull(message = "Статус замовлення є обов'язковим")
    private OrderStatus status;
}

package ua.nulp.lab2.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.nulp.lab2.model.enums.OrderStatus;

@Data
public class OrderStatusUpdateRequest {

    @NotNull(message = "Статус замовлення є обов'язковим")
    private OrderStatus status;
}

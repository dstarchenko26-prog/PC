package ua.nulp.order_service.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderUpdateRequest {
    @NotEmpty(message = "Кошик не може бути порожнім")
    @Valid // Обов'язково для того, щоб Spring перевіряв кожен товар у списку
    private List<OrderItemRequest> items;
}
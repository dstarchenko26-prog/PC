package ua.nulp.lab2.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderCreateRequest {
    @NotNull(message = "ID покупця є обов'язковим")
    private Long buyerId;

    @NotEmpty(message = "Замовлення повинно містити хоча б один товар")
    @Valid // Валідація вкладених об'єктів
    private List<OrderItemRequest> items;
}
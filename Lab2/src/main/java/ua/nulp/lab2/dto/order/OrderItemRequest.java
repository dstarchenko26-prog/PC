package ua.nulp.lab2.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull(message = "ID товару є обов'язковим")
    private Long productId;

    @NotNull(message = "Кількість є обов'язковою")
    @Min(value = 1, message = "Кількість має бути мінімум 1")
    private Integer quantity;
}

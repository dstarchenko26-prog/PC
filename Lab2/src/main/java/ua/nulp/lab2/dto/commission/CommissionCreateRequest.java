package ua.nulp.lab2.dto.commission;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CommissionCreateRequest {
    @NotNull(message = "ID замовлення є обов'язковим")
    private Long orderId;

    @NotNull(message = "ID продавця є обов'язковим")
    private Long sellerId;

    @NotNull(message = "Ставка комісії є обов'язковою")
    @DecimalMin(value = "0.01", message = "Комісія не може бути меншою за 1%")
    @DecimalMax(value = "0.50", message = "Комісія не може перевищувати 50%")
    private BigDecimal commissionRate;
}

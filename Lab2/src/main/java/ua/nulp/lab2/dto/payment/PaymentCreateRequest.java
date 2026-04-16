package ua.nulp.lab2.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ua.nulp.lab2.model.enums.PaymentMethod;
import java.math.BigDecimal;

@Data
public class PaymentCreateRequest {
    @NotNull(message = "ID замовлення є обов'язковим")
    private Long orderId;

    @NotNull(message = "Сума платежу є обов'язковою")
    @Positive(message = "Сума повинна бути більшою за нуль")
    private BigDecimal amount;

    @NotNull(message = "Метод оплати є обов'язковим")
    private PaymentMethod paymentMethod;
}

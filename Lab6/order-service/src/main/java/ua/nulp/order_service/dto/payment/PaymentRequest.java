package ua.nulp.order_service.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.nulp.order_service.model.enums.PaymentMethod;

@Data
public class PaymentRequest {
    @NotNull(message = "ID замовлення є обов'язковим")
    private Long orderId;

    @NotNull(message = "Метод оплати є обов'язковим")
    private PaymentMethod paymentMethod;

    // Прапорець для тестування: якщо передати false, платіж відхилиться
    private boolean simulateSuccess = true;
}

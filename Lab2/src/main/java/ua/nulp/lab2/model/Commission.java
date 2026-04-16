package ua.nulp.lab2.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Commission {
    private Long id;
    private Long orderId;
    private Long sellerId;
    private BigDecimal commissionRate; // Наприклад, 0.05 (5%)
    private BigDecimal amountWithheld; // Обчислена сума комісії
}

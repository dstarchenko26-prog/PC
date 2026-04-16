package ua.nulp.lab2.dto.commission;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CommissionResponse {
    private Long id;
    private Long orderId;
    private Long sellerId;
    private BigDecimal commissionRate;
    private BigDecimal amountWithheld;
}

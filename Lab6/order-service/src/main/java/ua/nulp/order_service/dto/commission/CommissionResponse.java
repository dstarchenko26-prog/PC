package ua.nulp.order_service.dto.commission;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CommissionResponse {
    private Long id;
    private Long paymentId;
    private BigDecimal commissionRate;
    private BigDecimal amountWithheld;
    private LocalDateTime createdAt;
}

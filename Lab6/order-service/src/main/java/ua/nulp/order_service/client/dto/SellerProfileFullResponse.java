package ua.nulp.order_service.client.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ua.nulp.order_service.client.dto.SellerProfileResponse;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class SellerProfileFullResponse extends SellerProfileResponse {
    private BigDecimal balance; // Баланс бачить тільки власник
}

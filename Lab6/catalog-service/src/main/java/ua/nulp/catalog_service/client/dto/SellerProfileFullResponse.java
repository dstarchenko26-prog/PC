package ua.nulp.catalog_service.client.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class SellerProfileFullResponse extends SellerProfileResponse {
    private BigDecimal balance; // Баланс бачить тільки власник
}

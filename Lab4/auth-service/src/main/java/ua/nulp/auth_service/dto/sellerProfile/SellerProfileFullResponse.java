package ua.nulp.auth_service.dto.sellerProfile;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class SellerProfileFullResponse extends SellerProfileResponse {
    private BigDecimal balance; // Баланс бачить тільки власник
}

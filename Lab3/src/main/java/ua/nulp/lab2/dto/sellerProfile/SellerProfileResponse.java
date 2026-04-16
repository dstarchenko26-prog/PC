package ua.nulp.lab2.dto.sellerProfile;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SellerProfileResponse {
    private Long id;
    private Long userId;
    private String companyName;
    private String description;
    private Double rating;
}

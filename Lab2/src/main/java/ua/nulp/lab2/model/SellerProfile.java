package ua.nulp.lab2.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SellerProfile {
    private Long id;
    private Long userId; // Зв'язок з User
    private String companyName;
    private String description;
    private Double rating;
    private BigDecimal balance;
}

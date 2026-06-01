package ua.nulp.auth_service.dto.sellerProfile;

import lombok.Data;

@Data
public class SellerProfileResponse {
    private Long id;
    private Long userId;
    private String companyName;
    private String description;
    private Double rating;
}

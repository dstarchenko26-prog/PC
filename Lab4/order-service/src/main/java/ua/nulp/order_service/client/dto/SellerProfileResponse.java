package ua.nulp.order_service.client.dto;

import lombok.Data;

@Data
public class SellerProfileResponse {
    private Long id;
    private Long userId;
    private String companyName;
    private String description;
    private Double rating;
}

package ua.nulp.lab2.dto.sellerProfile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerProfileUpdateRequest {
    @NotBlank(message = "Назва компанії не може бути порожньою")
    @Size(min = 2, max = 100, message = "Назва компанії має містити від 2 до 100 символів")
    private String companyName;

    @Size(max = 1000, message = "Опис не повинен перевищувати 1000 символів")
    private String description;
}

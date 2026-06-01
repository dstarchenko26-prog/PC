package ua.nulp.auth_service.dto.sellerProfile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerProfileCreateRequest {

    @NotNull(message = "ID користувача є обов'язковим")
    private Long userId;

    @NotBlank(message = "Назва компанії не може бути порожньою")
    @Size(min = 2, max = 100, message = "Назва компанії повинна містити від 2 до 100 символів")
    private String companyName;

    @Size(max = 500, message = "Опис не може перевищувати 500 символів")
    private String description;
}

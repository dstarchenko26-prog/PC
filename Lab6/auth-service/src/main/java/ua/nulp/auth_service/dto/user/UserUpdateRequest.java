package ua.nulp.auth_service.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Ім'я користувача не може бути порожнім")
    @Size(min = 3, max = 50, message = "Ім'я повинно містити від 3 до 50 символів")
    private String username;

    @NotBlank(message = "Email є обов'язковим")
    @Email(message = "Некоректний формат email")
    private String email;
}

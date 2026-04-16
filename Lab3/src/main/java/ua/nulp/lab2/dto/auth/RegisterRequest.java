package ua.nulp.lab2.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ua.nulp.lab2.model.enums.Role;

@Data
public class RegisterRequest {
    @NotBlank(message = "Ім'я користувача не може бути порожнім")
    @Size(min = 3, max = 50, message = "Ім'я повинно містити від 3 до 50 символів")
    private String username;

    @NotBlank(message = "Email є обов'язковим")
    @Email(message = "Некоректний формат email")
    private String email;

    @NotBlank(message = "Пароль є обов'язковим")
    @Size(min = 6, message = "Пароль повинен містити мінімум 6 символів")
    private String password;

    @NotNull(message = "Роль є обов'язковою")
    private Role role;
}

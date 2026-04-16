package ua.nulp.lab2.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email є обов'язковим")
    @Email(message = "Некоректний формат email")
    private String email;

    @NotBlank(message = "Пароль є обов'язковим")
    private String password;
}

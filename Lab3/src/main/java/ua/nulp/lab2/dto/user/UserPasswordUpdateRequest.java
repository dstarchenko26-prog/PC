package ua.nulp.lab2.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPasswordUpdateRequest {
    @NotBlank(message = "Старий пароль є обов'язковим")
    private String oldPassword;

    @NotBlank(message = "Новий пароль є обов'язковим")
    @Size(min = 6, message = "Пароль повинен містити мінімум 6 символів")
    private String newPassword;
}

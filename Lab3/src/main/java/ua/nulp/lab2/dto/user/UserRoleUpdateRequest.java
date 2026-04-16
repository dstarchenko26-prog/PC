package ua.nulp.lab2.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.nulp.lab2.model.enums.Role;

@Data
public class UserRoleUpdateRequest {
    @NotNull(message = "Роль користувача є обов'язковою")
    private Role role;
}
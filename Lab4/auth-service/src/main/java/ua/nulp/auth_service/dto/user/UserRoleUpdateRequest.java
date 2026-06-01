package ua.nulp.auth_service.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.nulp.auth_service.model.enums.Role;

@Data
public class UserRoleUpdateRequest {
    @NotNull(message = "Роль користувача є обов'язковою")
    private Role role;
}
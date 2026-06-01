package ua.nulp.auth_service.dto.user;

import lombok.Data;
import ua.nulp.auth_service.model.enums.Role;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}

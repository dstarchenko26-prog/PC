package ua.nulp.lab2.model;

import lombok.Data;
import ua.nulp.lab2.model.enums.Role;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String email;
    private String passwordHash; // Поки зберігаємо як є, хешування додається на етапі Security
    private Role role;
    private LocalDateTime createdAt;
}
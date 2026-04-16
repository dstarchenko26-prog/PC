package ua.nulp.lab2.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nulp.lab2.dto.user.*;
import ua.nulp.lab2.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Оновлення базової інформації
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserInfo(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserInfo(id, request));
    }

    // Зміна пароля (повертаємо 204 No Content, оскільки немає сенсу повертати всього юзера)
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long id,
            @RequestBody @Valid UserPasswordUpdateRequest request) {
        userService.updatePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    // Зміна ролі
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable Long id,
            @RequestBody @Valid UserRoleUpdateRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }

    // Видалення користувача
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // 204 No Content - стандарт для успішного видалення
    }
}

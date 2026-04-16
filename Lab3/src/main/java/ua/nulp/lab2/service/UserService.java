package ua.nulp.lab2.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nulp.lab2.dto.user.*;
import ua.nulp.lab2.exception.ResourceConflictException;
import ua.nulp.lab2.exception.ResourceNotFoundException;
import ua.nulp.lab2.model.User;
import ua.nulp.lab2.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Додано для перевірки та хешування паролів

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToResponse);
    }

    // 1. Оновлення загальної інформації (PUT або PATCH)
    @Transactional
    public UserResponse updateUserInfo(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача з ID " + id + " не знайдено")); // Викличе 404

        // Додаткова перевірка, чи не зайнятий email іншим користувачем
        userRepository.findByEmail(request.getEmail())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(id)) {
                        throw new ResourceConflictException("Цей email вже використовується іншим користувачем"); // Викличе 409
                    }
                });

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        return mapToResponse(userRepository.save(user));
    }

    // 2. Оновлення пароля (PATCH)
    @Transactional
    public void updatePassword(Long id, UserPasswordUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача з ID " + id + " не знайдено")); // Викличе 404

        // Реальна перевірка через PasswordEncoder
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Старий пароль введено неправильно"); // Викличе 400 Bad Request
        }

        // Хешуємо новий пароль перед збереженням
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // 3. Оновлення ролі (PATCH)
    @Transactional
    public UserResponse updateRole(Long id, UserRoleUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача з ID " + id + " не знайдено")); // Викличе 404

        user.setRole(request.getRole());
        return mapToResponse(userRepository.save(user));
    }

    // 4. Видалення користувача
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Користувача з ID " + id + " не знайдено"); // Викличе 404
        }
        userRepository.deleteById(id);
    }

    // Допоміжний мапінг
    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
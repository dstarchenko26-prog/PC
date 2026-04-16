package ua.nulp.lab2.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.nulp.lab2.dto.auth.*;
import ua.nulp.lab2.model.User;
import ua.nulp.lab2.repository.UserRepository;
import ua.nulp.lab2.security.JwtService;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        // Перевіряємо, чи існує вже такий email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Користувач з таким email вже існує");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // Хешуємо пароль перед збереженням!
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Генеруємо токен для нового користувача
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(LoginRequest request) {
        // AuthenticationManager автоматично перевірить хеш пароля з тим, що у базі
        // Якщо пароль невірний, він викине виняток BadCredentialsException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Якщо автентифікація успішна, дістаємо користувача з бази
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Користувача не знайдено"));

        // Генеруємо та повертаємо новий токен
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}

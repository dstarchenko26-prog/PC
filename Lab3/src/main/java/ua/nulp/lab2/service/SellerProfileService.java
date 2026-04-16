package ua.nulp.lab2.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ua.nulp.lab2.dto.sellerProfile.*;
import ua.nulp.lab2.model.SellerProfile;
import ua.nulp.lab2.repository.SellerProfileRepository;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ua.nulp.lab2.exception.ResourceConflictException;
import ua.nulp.lab2.exception.ResourceNotFoundException;
import ua.nulp.lab2.model.enums.Role;
import ua.nulp.lab2.model.User;
import ua.nulp.lab2.repository.UserRepository;



@Service
public class SellerProfileService {
    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;

    public SellerProfileService(SellerProfileRepository sellerProfileRepository, UserRepository userRepository) {
        this.sellerProfileRepository = sellerProfileRepository;
        this.userRepository = userRepository;
    }

    // Створення профілю (POST)
    @Transactional
    public SellerProfileResponse createProfile(SellerProfileCreateRequest request) {
        // 1. Перевіряємо, чи існує користувач і чи він є продавцем
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Користувача з ID " + request.getUserId() + " не знайдено"));

        if (user.getRole() != Role.SELLER) {
            throw new IllegalArgumentException("Тільки користувачі з роллю SELLER можуть створити профіль продавця");
        }

        // 2. Перевіряємо, чи немає вже профілю у цього користувача
        if (sellerProfileRepository.existsByUserId(request.getUserId())) {
            throw new ResourceConflictException("Профіль продавця для цього користувача вже існує");
        }

        SellerProfile profile = new SellerProfile();
        profile.setUserId(request.getUserId());
        profile.setCompanyName(request.getCompanyName());
        profile.setDescription(request.getDescription());
        profile.setBalance(BigDecimal.ZERO);
        profile.setRating(0.0);

        return mapToResponse(sellerProfileRepository.save(profile));
    }

    // Отримання списку з пагінацією (GET ALL)
    @Transactional(readOnly = true)
    public Page<SellerProfileResponse> getAllProfiles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return sellerProfileRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // ПУБЛІЧНИЙ: Отримати за ID (повертає базовий DTO)
    @Transactional(readOnly = true)
    public SellerProfileResponse getPublicProfile(Long id) {
        return sellerProfileRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Профіль не знайдено"));
    }

    // ПРИВАТНИЙ: Отримати власний профіль (повертає Full DTO з балансом)
    @Transactional(readOnly = true)
    public SellerProfileFullResponse getMyProfile() {
        // Дістаємо email поточного юзера з Security Context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));

        SellerProfile profile = sellerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("У вас ще немає профілю продавця"));

        return mapToFullResponse(profile);
    }

    // Оновлення профілю (PUT)
    @Transactional
    public SellerProfileResponse updateProfile(Long id, SellerProfileUpdateRequest request) {
        SellerProfile profile = sellerProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Профіль продавця з ID " + id + " не знайдено"));

        profile.setCompanyName(request.getCompanyName());
        profile.setDescription(request.getDescription());

        return mapToResponse(sellerProfileRepository.save(profile));
    }

    // Видалення профілю (DELETE)
    @Transactional
    public void deleteProfile(Long id) {
        if (!sellerProfileRepository.existsById(id)) {
            throw new ResourceNotFoundException("Профіль продавця з ID " + id + " не знайдено");
        }
        sellerProfileRepository.deleteById(id);
    }

    private SellerProfileResponse mapToResponse(SellerProfile profile) {
        SellerProfileResponse response = new SellerProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setCompanyName(profile.getCompanyName());
        response.setDescription(profile.getDescription());
        response.setRating(profile.getRating());
        return response;
    }

    // Мапінг для повного перегляду (з балансом)
    private SellerProfileFullResponse mapToFullResponse(SellerProfile profile) {
        SellerProfileFullResponse res = new SellerProfileFullResponse();
        // Копіюємо базові поля
        res.setId(profile.getId());
        res.setUserId(profile.getUserId());
        res.setCompanyName(profile.getCompanyName());
        res.setDescription(profile.getDescription());
        res.setRating(profile.getRating());
        // Додаємо секретні поля
        res.setBalance(profile.getBalance());
        return res;
    }
}

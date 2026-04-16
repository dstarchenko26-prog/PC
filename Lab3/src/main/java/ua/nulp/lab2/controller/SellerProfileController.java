package ua.nulp.lab2.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nulp.lab2.dto.sellerProfile.*;
import ua.nulp.lab2.service.SellerProfileService;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/sellers")
public class SellerProfileController {

    private final SellerProfileService sellerProfileService;

    public SellerProfileController(SellerProfileService sellerProfileService) {
        this.sellerProfileService = sellerProfileService;
    }

    @PostMapping
    public ResponseEntity<SellerProfileResponse> createProfile(@RequestBody @Valid SellerProfileCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sellerProfileService.createProfile(request));
    }

    // Пагінація: /api/sellers?page=0&size=10
    @GetMapping
    public ResponseEntity<Page<SellerProfileResponse>> getAllProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(sellerProfileService.getAllProfiles(page, size));
    }

    // 1. Публічний доступ за ID (балансу немає)
    @GetMapping("/{id}")
    public ResponseEntity<SellerProfileResponse> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(sellerProfileService.getPublicProfile(id));
    }

    // 2. Приватний доступ до свого профілю (з балансом)
    // Цей шлях захищений анотацією authenticated у SecurityConfig
    @GetMapping("/me")
    public ResponseEntity<SellerProfileFullResponse> getMyProfile() {
        return ResponseEntity.ok(sellerProfileService.getMyProfile());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SellerProfileResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody @Valid SellerProfileUpdateRequest request) {
        return ResponseEntity.ok(sellerProfileService.updateProfile(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        sellerProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}



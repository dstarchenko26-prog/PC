package ua.nulp.lab2.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nulp.lab2.dto.sellerProfile.SellerProfileCreateRequest;
import ua.nulp.lab2.dto.sellerProfile.SellerProfileResponse;
import ua.nulp.lab2.service.SellerProfileService;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
public class SellerProfileController {

    private final SellerProfileService sellerProfileService;

    public SellerProfileController(SellerProfileService sellerProfileService) {
        this.sellerProfileService = sellerProfileService;
    }

    @PostMapping
    public ResponseEntity<SellerProfileResponse> createProfile(@RequestBody @Valid SellerProfileCreateRequest request) {
        SellerProfileResponse created = sellerProfileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<SellerProfileResponse>> getAllProfiles() {
        return ResponseEntity.ok(sellerProfileService.getAllProfiles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SellerProfileResponse> getProfileById(@PathVariable Long id) {
        return sellerProfileService.getProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

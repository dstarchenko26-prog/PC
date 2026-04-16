package ua.nulp.lab2.service;

import org.springframework.stereotype.Service;
import ua.nulp.lab2.dto.sellerProfile.SellerProfileCreateRequest;
import ua.nulp.lab2.dto.sellerProfile.SellerProfileResponse;
import ua.nulp.lab2.model.SellerProfile;
import ua.nulp.lab2.repository.SellerProfileRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SellerProfileService {
    private final SellerProfileRepository sellerProfileRepository;

    public SellerProfileService(SellerProfileRepository sellerProfileRepository) {
        this.sellerProfileRepository = sellerProfileRepository;
    }

    public SellerProfileResponse createProfile(SellerProfileCreateRequest request) {
        // У реальному проєкті тут була б перевірка:
        // чи існує такий User в UserRepository і чи має він роль SELLER

        SellerProfile profile = new SellerProfile();
        profile.setUserId(request.getUserId());
        profile.setCompanyName(request.getCompanyName());
        profile.setDescription(request.getDescription());

        // Ініціалізація системних полів за замовчуванням
        profile.setRating(0.0);
        profile.setBalance(BigDecimal.ZERO);

        SellerProfile savedProfile = sellerProfileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    public List<SellerProfileResponse> getAllProfiles() {
        return sellerProfileRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<SellerProfileResponse> getProfileById(Long id) {
        return sellerProfileRepository.findById(id)
                .map(this::mapToResponse);
    }

    private SellerProfileResponse mapToResponse(SellerProfile profile) {
        SellerProfileResponse response = new SellerProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setCompanyName(profile.getCompanyName());
        response.setDescription(profile.getDescription());
        response.setRating(profile.getRating());
        response.setBalance(profile.getBalance());
        return response;
    }
}

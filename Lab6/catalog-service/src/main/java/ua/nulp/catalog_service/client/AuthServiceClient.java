package ua.nulp.catalog_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ua.nulp.catalog_service.client.dto.SellerProfileFullResponse;
import ua.nulp.catalog_service.client.dto.SellerProfileResponse;
import ua.nulp.catalog_service.client.dto.UserResponse;

// URL вказує прямо на порт нашого Auth Service
@FeignClient(name = "auth-service", url = "${app.services.auth}")
public interface AuthServiceClient {

    @GetMapping("/api/sellers/{id}")
    SellerProfileResponse getProfileById(@PathVariable("id") Long id);

    @GetMapping("/api/sellers/me")
    SellerProfileFullResponse getMyProfile();

    @GetMapping("/api/users/me")
    UserResponse getCurrentUser();
}

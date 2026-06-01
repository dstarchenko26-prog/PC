package ua.nulp.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.nulp.order_service.client.dto.SellerProfileFullResponse;
import ua.nulp.order_service.client.dto.UserResponse;

import java.math.BigDecimal;

// URL вказує прямо на порт нашого Auth Service
@FeignClient(name = "auth-service", url = "${app.services.auth}")
public interface AuthServiceClient {

    @GetMapping("/api/sellers/me")
    SellerProfileFullResponse getMyProfile();

    @GetMapping("/api/users/me")
    UserResponse getCurrentUser();

    @PutMapping("/api/sellers/{id}/balance/add")
    void addSellerBalance(@PathVariable("id") Long id, @RequestParam("amount") BigDecimal amount);

    @PutMapping("/api/sellers/{id}/balance/sub")
    void subSellerBalance(@PathVariable("id") Long id, @RequestParam("amount") BigDecimal amount);
}

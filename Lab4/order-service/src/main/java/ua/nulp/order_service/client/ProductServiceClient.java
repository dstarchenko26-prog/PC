package ua.nulp.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.nulp.order_service.client.dto.ProductResponse;

@FeignClient(name = "catalog-service", url = "${app.services.catalog}")
public interface ProductServiceClient {
    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);

    @PutMapping("/api/products/{id}/reduce-stock")
    void reduceStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);

    @PutMapping("/api/products/{id}/restore-stock")
    void restoreStock(@PathVariable("id") Long id, @RequestParam("quantity") Integer quantity);

}
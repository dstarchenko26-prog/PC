package ua.nulp.lab2.service;

import ua.nulp.lab2.dto.product.ProductCreateRequest;
import ua.nulp.lab2.dto.product.ProductResponse;
import ua.nulp.lab2.model.Product;
import ua.nulp.lab2.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(ProductCreateRequest request) {
        // Мапінг DTO -> Сутність
        Product product = new Product();
        product.setSellerId(request.getSellerId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setIsActive(true); // За замовчуванням товар активний

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<ProductResponse> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToResponse);
    }

    // Допоміжний метод для мапінгу Сутність -> DTO
    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setSellerId(product.getSellerId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setIsActive(product.getIsActive());
        return response;
    }
}
package ua.nulp.catalog_service.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nulp.catalog_service.client.AuthServiceClient;
import ua.nulp.catalog_service.client.dto.SellerProfileResponse;
import ua.nulp.catalog_service.dto.RestPage;
import ua.nulp.catalog_service.dto.product.*;
import ua.nulp.catalog_service.exception.ResourceConflictException;
import ua.nulp.catalog_service.exception.ResourceNotFoundException;
import ua.nulp.catalog_service.model.Product;
import ua.nulp.catalog_service.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final AuthServiceClient authServiceClient;

    public ProductService(ProductRepository productRepository, AuthServiceClient authServiceClient) {
        this.productRepository = productRepository;
        this.authServiceClient = authServiceClient;
    }

    // CREATE
    @CacheEvict(value = "all_products", allEntries = true)
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        // Перевіряємо, чи існує профіль продавця
        SellerProfileResponse seller = authServiceClient.getProfileById(request.getSellerId());

        Product product = new Product();
        product.setSellerId(seller.getId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setIsActive(true);
        product.setRating(0.0);
        product.setCreatedAt(LocalDateTime.now());

        return mapToResponse(productRepository.save(product));
    }

    private Page<ProductResponse> getFilteredProducts(
            Long sellerId, Boolean isActive, String name,
            BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sort) {

        // Сортування за замовчуванням
        Sort sorting = Sort.by("createdAt").descending();

        if (sort != null && sort.contains(",")) {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, sortParams[0]);
        }

        Pageable pageable = PageRequest.of(page, size, sorting);

        return productRepository.findProductsWithFilters(
                        sellerId, isActive, name, minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    // 1. Публічна вітрина (тільки активні, будь-який продавець)
    @Cacheable(value = "all_products", key = "{#name, #minP, #maxP, #p, #s, #sort}")
    @Transactional(readOnly = true)
    public RestPage<ProductResponse> getAllActiveProducts(String name, BigDecimal minP, BigDecimal maxP, int p, int s, String sort) {
        // Отримуємо звичайний Page
        Page<ProductResponse> result = getFilteredProducts(null, true, name, minP, maxP, p, s, sort);

        // Повертаємо обгортку (тут спрацює конструктор RestPage(Page<T> page))
        return new RestPage<>(result);
    }

    // 2. Товари конкретного продавця (тільки активні)
    @Transactional(readOnly = true)
    public Page<ProductResponse> getActiveProductsBySeller(Long sellerId, String name, BigDecimal minP, BigDecimal maxP, int p, int s, String sort) {
        return getFilteredProducts(sellerId, true, name, minP, maxP, p, s, sort);
    }

    // 3. Мій кабінет (всі мої товари: і активні, і ні)
    @Transactional(readOnly = true)
    public Page<ProductResponse> getMyProducts(String name, BigDecimal minP, BigDecimal maxP, int p, int s, String sort) {
        SellerProfileResponse profile = authServiceClient.getMyProfile();
        // Тут isActive = null, щоб бачити все (і активні товари, і приховані)
        return getFilteredProducts(profile.getId(), null, name, minP, maxP, p, s, sort);
    }

    // READ (GET BY ID)
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар з ID " + id + " не знайдено"));

        // Опціонально: можна кидати 404, якщо товар не активний і запит робить звичайний юзер
        return mapToResponse(product);
    }

    // UPDATE (PUT)
    @CacheEvict(value = "all_products", allEntries = true)
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар з ID " + id + " не знайдено"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        return mapToResponse(productRepository.save(product));
    }

    // DELETE (DELETE)
    @CacheEvict(value = "all_products", allEntries = true)
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар з ID " + id + " не знайдено"));

        product.setIsActive(false);
        productRepository.save(product);
    }

    @CacheEvict(value = "all_products", allEntries = true)
    @Transactional
    public void reduceStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар з ID " + id + " не знайдено"));

        if (product.getStockQuantity() < quantity) {
            throw new ResourceConflictException("На складі недостатньо товару: " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    @CacheEvict(value = "all_products", allEntries = true)
    @Transactional
    public void restoreStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар з ID " + id + " не знайдено"));

        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setSellerId(product.getSellerId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setIsActive(product.getIsActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setRating(product.getRating());
        return response;
    }
}
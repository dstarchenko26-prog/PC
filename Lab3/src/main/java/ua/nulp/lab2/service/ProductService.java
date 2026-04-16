package ua.nulp.lab2.service;

import org.springframework.data.domain.Sort;
import ua.nulp.lab2.dto.product.*;
import ua.nulp.lab2.model.Product;
import ua.nulp.lab2.model.SellerProfile;
import ua.nulp.lab2.model.User;
import ua.nulp.lab2.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ua.nulp.lab2.exception.ResourceNotFoundException;
import ua.nulp.lab2.repository.SellerProfileRepository;

import java.time.LocalDateTime;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.nulp.lab2.repository.UserRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, SellerProfileRepository sellerProfileRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.userRepository = userRepository;
    }

    // CREATE
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        // Перевіряємо, чи існує профіль продавця
        if (!sellerProfileRepository.existsById(request.getSellerId())) {
            throw new ResourceNotFoundException("Профіль продавця з ID " + request.getSellerId() + " не знайдено");
        }

        Product product = new Product();
        product.setSellerId(request.getSellerId());
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
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllActiveProducts(String name, BigDecimal minP, BigDecimal maxP, int p, int s, String sort) {
        return getFilteredProducts(null, true, name, minP, maxP, p, s, sort);
    }

    // 2. Товари конкретного продавця (тільки активні)
    @Transactional(readOnly = true)
    public Page<ProductResponse> getActiveProductsBySeller(Long sellerId, String name, BigDecimal minP, BigDecimal maxP, int p, int s, String sort) {
        return getFilteredProducts(sellerId, true, name, minP, maxP, p, s, sort);
    }

    // 3. Мій кабінет (всі мої товари: і активні, і ні)
    @Transactional(readOnly = true)
    public Page<ProductResponse> getMyProducts(String name, BigDecimal minP, BigDecimal maxP, int p, int s, String sort) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Користувач не знайдений"));
        SellerProfile profile = sellerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Профіль не знайдений"));

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
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар з ID " + id + " не знайдено"));

        product.setIsActive(false);
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
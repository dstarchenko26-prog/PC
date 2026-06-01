package ua.nulp.catalog_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nulp.catalog_service.client.AuthServiceClient;
import ua.nulp.catalog_service.client.dto.UserResponse;
import ua.nulp.catalog_service.dto.review.ReviewCreateRequest;
import ua.nulp.catalog_service.dto.review.ReviewResponse;
import ua.nulp.catalog_service.exception.ForbiddenOperationException;
import ua.nulp.catalog_service.exception.ResourceConflictException;
import ua.nulp.catalog_service.exception.ResourceNotFoundException;
import ua.nulp.catalog_service.model.Product;
import ua.nulp.catalog_service.model.Review;
import ua.nulp.catalog_service.repository.ProductRepository;
import ua.nulp.catalog_service.repository.ReviewRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final AuthServiceClient authServiceClient;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository, AuthServiceClient authServiceClient) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.authServiceClient = authServiceClient;
    }

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        // 1. Отримуємо автора з токена
        UserResponse currentUser = authServiceClient.getCurrentUser();

        // 2. Перевіряємо, чи існує товар
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Товар не знайдено"));

        // 3. Захист від спаму: один користувач = один відгук на товар
        if (reviewRepository.existsByUserIdAndProductId(currentUser.getId(), product.getId())) {
            throw new ResourceConflictException("Ви вже залишали відгук на цей товар");
        }

        // 4. Створюємо відгук
        Review review = new Review();
        review.setProductId(product.getId());
        review.setUserId(currentUser.getId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // 5. ПЕРЕРАХУНОК РЕЙТИНГУ ТОВАРУ
        updateProductRating(product.getId());

        return mapToResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviews(int page, int size) {
        // Створюємо об'єкт пагінації із сортуванням за датою створення
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return reviewRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsForProduct(Long productId, int page, int size) {
        // За замовчуванням нові відгуки перші
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reviewRepository.findByProductId(productId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void deleteReview(Long id) {
        // 1. Шукаємо відгук
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Відгук не знайдено"));

        UserResponse currentUser = authServiceClient.getCurrentUser();

        // 3. ПЕРЕВІРКА ПРАВ:
        boolean isAuthor = review.getUserId().equals(currentUser.getId());
        // Перевіряємо роль як String, оскільки enum Role залишився в auth-service
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (!isAuthor && !isAdmin) {
            throw new ForbiddenOperationException("У вас немає прав для видалення цього відгуку");
        }

        // 4. Видалення та перерахунок
        Long productId = review.getProductId();
        reviewRepository.deleteById(id);
        updateProductRating(productId);
    }

    @Transactional
    public Optional<ReviewResponse> getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(this::mapToResponse);
    }

    // Приватний метод для синхронізації рейтингу
    private void updateProductRating(Long productId) {
        Double averageRating = reviewRepository.getAverageRatingByProductId(productId).orElse(0.0);

        // Округлюємо до 1 знака після коми (напр. 4.3)
        averageRating = Math.round(averageRating * 10.0) / 10.0;

        Product product = productRepository.findById(productId).orElseThrow();
        product.setRating(averageRating);
        productRepository.save(product);
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse res = new ReviewResponse();
        res.setId(review.getId());
        res.setProductId(review.getProductId());
        res.setBuyerId(review.getUserId());
        res.setRating(review.getRating());
        res.setComment(review.getComment());
        res.setCreatedAt(review.getCreatedAt());
        return res;
    }
}
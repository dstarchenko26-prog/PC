package ua.nulp.lab2.service;

import org.springframework.stereotype.Service;
import ua.nulp.lab2.dto.review.ReviewCreateRequest;
import ua.nulp.lab2.dto.review.ReviewResponse;
import ua.nulp.lab2.model.Product;
import ua.nulp.lab2.model.Review;
import ua.nulp.lab2.repository.ProductRepository;
import ua.nulp.lab2.repository.ReviewRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    public ReviewResponse createReview(ReviewCreateRequest request) {
        // Перевіряємо, чи існує товар
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Товар з ID " + request.getProductId() + " не знайдено"));

        Review review = new Review();
        review.setProductId(product.getId());
        review.setBuyerId(request.getBuyerId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        return mapToResponse(savedReview);
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<ReviewResponse> getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(this::mapToResponse);
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setProductId(review.getProductId());
        response.setBuyerId(review.getBuyerId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}

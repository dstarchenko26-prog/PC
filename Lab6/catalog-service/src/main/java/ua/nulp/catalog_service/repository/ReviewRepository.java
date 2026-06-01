package ua.nulp.catalog_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.nulp.catalog_service.model.Review;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Отримати всі відгуки до конкретного товару
    Page<Review> findByProductId(Long productId, Pageable pageable);

    // Перевірка: чи залишав вже цей користувач відгук на цей товар?
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // Магія бази даних: вираховуємо середній рейтинг для товару
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Optional<Double> getAverageRatingByProductId(@Param("productId") Long productId);
}
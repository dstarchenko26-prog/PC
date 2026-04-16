package ua.nulp.lab2.repository;

import org.springframework.stereotype.Repository;
import ua.nulp.lab2.model.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ReviewRepository {
    private final Map<Long, Review> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Review save(Review review) {
        if (review.getId() == null) {
            review.setId(idGenerator.getAndIncrement());
        }
        storage.put(review.getId(), review);
        return review;
    }

    public List<Review> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Review> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
}

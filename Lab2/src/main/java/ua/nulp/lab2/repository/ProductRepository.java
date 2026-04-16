package ua.nulp.lab2.repository;

import ua.nulp.lab2.model.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProductRepository {
    private final Map<Long, Product> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(idGenerator.getAndIncrement());
        }
        storage.put(product.getId(), product);
        return product;
    }

    public List<Product> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
}

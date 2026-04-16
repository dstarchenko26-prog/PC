package ua.nulp.lab2.repository;

import org.springframework.stereotype.Repository;
import ua.nulp.lab2.model.Order;
import ua.nulp.lab2.model.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderRepository {
    private final Map<Long, Order> storage = new ConcurrentHashMap<>();
    private final AtomicLong orderIdGenerator = new AtomicLong(1);
    private final AtomicLong itemIdGenerator = new AtomicLong(1);

    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(orderIdGenerator.getAndIncrement());
        }
        // Генеруємо ID для нових позицій
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getId() == null) {
                    item.setId(itemIdGenerator.getAndIncrement());
                }
            }
        }
        storage.put(order.getId(), order);
        return order;
    }

    public List<Order> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
}

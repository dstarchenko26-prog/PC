package ua.nulp.lab2.repository;

import org.springframework.stereotype.Repository;
import ua.nulp.lab2.model.Payment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PaymentRepository {
    private final Map<Long, Payment> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            payment.setId(idGenerator.getAndIncrement());
        }
        storage.put(payment.getId(), payment);
        return payment;
    }

    public List<Payment> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Payment> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
}

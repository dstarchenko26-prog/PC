package ua.nulp.lab2.repository;

import org.springframework.stereotype.Repository;
import ua.nulp.lab2.model.Commission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class CommissionRepository {
    private final Map<Long, Commission> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Commission save(Commission commission) {
        if (commission.getId() == null) {
            commission.setId(idGenerator.getAndIncrement());
        }
        storage.put(commission.getId(), commission);
        return commission;
    }

    public List<Commission> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Commission> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
}

package ua.nulp.lab2.repository;

import org.springframework.stereotype.Repository;
import ua.nulp.lab2.model.SellerProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SellerProfileRepository {
    private final Map<Long, SellerProfile> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public SellerProfile save(SellerProfile profile) {
        if (profile.getId() == null) {
            profile.setId(idGenerator.getAndIncrement());
        }
        storage.put(profile.getId(), profile);
        return profile;
    }

    public List<SellerProfile> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<SellerProfile> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
}

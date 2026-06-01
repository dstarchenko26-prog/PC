package ua.nulp.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nulp.auth_service.model.SellerProfile;

import java.util.Optional;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {

    // Допоміжні методи для бізнес-логіки
    Optional<SellerProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}

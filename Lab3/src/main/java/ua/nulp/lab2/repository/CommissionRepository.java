package ua.nulp.lab2.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nulp.lab2.model.Commission;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {
    // Метод для отримання історії комісій конкретного продавця
    Page<Commission> findBySellerId(Long sellerId, Pageable pageable);
}
package ua.nulp.lab2.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.nulp.lab2.model.Order;
import ua.nulp.lab2.model.enums.OrderStatus;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);
    Optional<Order> findByBuyerIdAndStatus(Long buyerId, OrderStatus status);
    Page<Order> findBySellerId(Long sellerId, Pageable pageable);
}


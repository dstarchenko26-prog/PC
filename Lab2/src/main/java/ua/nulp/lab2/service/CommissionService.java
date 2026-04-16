package ua.nulp.lab2.service;

import org.springframework.stereotype.Service;
import ua.nulp.lab2.dto.commission.CommissionCreateRequest;
import ua.nulp.lab2.dto.commission.CommissionResponse;
import ua.nulp.lab2.model.Commission;
import ua.nulp.lab2.model.Order;
import ua.nulp.lab2.repository.CommissionRepository;
import ua.nulp.lab2.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommissionService {
    private final CommissionRepository commissionRepository;
    private final OrderRepository orderRepository;

    public CommissionService(CommissionRepository commissionRepository, OrderRepository orderRepository) {
        this.commissionRepository = commissionRepository;
        this.orderRepository = orderRepository;
    }

    public CommissionResponse createCommission(CommissionCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Замовлення не знайдено"));

        Commission commission = new Commission();
        commission.setOrderId(request.getOrderId());
        commission.setSellerId(request.getSellerId());
        commission.setCommissionRate(request.getCommissionRate());

        // Розраховуємо утриману суму: Загальна сума замовлення * Ставка комісії
        BigDecimal withheld = order.getTotalAmount().multiply(request.getCommissionRate());
        commission.setAmountWithheld(withheld);

        Commission savedCommission = commissionRepository.save(commission);
        return mapToResponse(savedCommission);
    }

    public List<CommissionResponse> getAllCommissions() {
        return commissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<CommissionResponse> getCommissionById(Long id) {
        return commissionRepository.findById(id)
                .map(this::mapToResponse);
    }

    private CommissionResponse mapToResponse(Commission commission) {
        CommissionResponse response = new CommissionResponse();
        response.setId(commission.getId());
        response.setOrderId(commission.getOrderId());
        response.setSellerId(commission.getSellerId());
        response.setCommissionRate(commission.getCommissionRate());
        response.setAmountWithheld(commission.getAmountWithheld());
        return response;
    }
}

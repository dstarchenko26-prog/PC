package ua.nulp.lab2.service;

import org.springframework.stereotype.Service;
import ua.nulp.lab2.dto.order.*;
import ua.nulp.lab2.model.*;
import ua.nulp.lab2.repository.OrderRepository;
import ua.nulp.lab2.repository.ProductRepository;
import ua.nulp.lab2.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository; // Потрібен для отримання ціни

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public OrderResponse createOrder(OrderCreateRequest request) {
        Order order = new Order();
        order.setBuyerId(request.getBuyerId());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            // Шукаємо товар
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Товар з ID " + itemRequest.getProductId() + " не знайдено"));

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setQuantity(itemRequest.getQuantity());
            item.setPriceAtPurchase(product.getPrice()); // Фіксуємо ціну!

            // Обчислюємо суму позиції (Ціна * Кількість)
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal); // Додаємо до загальної суми

            items.add(item);
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<OrderResponse> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::mapToResponse);
    }

    // Мапінг Замовлення -> DTO
    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setBuyerId(order.getBuyerId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
            OrderItemResponse itemRes = new OrderItemResponse();
            itemRes.setId(item.getId());
            itemRes.setProductId(item.getProductId());
            itemRes.setQuantity(item.getQuantity());
            itemRes.setPriceAtPurchase(item.getPriceAtPurchase());
            return itemRes;
        }).collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }
}

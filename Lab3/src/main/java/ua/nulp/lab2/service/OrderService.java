package ua.nulp.lab2.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nulp.lab2.dto.order.*;
import ua.nulp.lab2.exception.ResourceConflictException;
import ua.nulp.lab2.exception.ResourceNotFoundException;
import ua.nulp.lab2.model.*;
import ua.nulp.lab2.model.enums.OrderStatus;
import ua.nulp.lab2.model.enums.Role;
import ua.nulp.lab2.repository.OrderRepository;
import ua.nulp.lab2.repository.ProductRepository;
import ua.nulp.lab2.repository.SellerProfileRepository;
import ua.nulp.lab2.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        UserRepository userRepository, SellerProfileRepository sellerProfileRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.sellerProfileRepository = sellerProfileRepository;
    }

    // =================================================================================
    // 1. CREATE: Створення замовлень (Розбиття кошика з фронтенду по продавцях)
    // =================================================================================

    @Transactional
    public List<OrderResponse> createOrders(OrderCreateRequest request) {
        User buyer = getCurrentAuthenticatedUser();

        // Групуємо товари за sellerId
        Map<Long, List<OrderItemRequest>> sellerItemsMap = new HashMap<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Товар не знайдено"));
            sellerItemsMap.computeIfAbsent(product.getSellerId(), k -> new ArrayList<>()).add(itemReq);
        }

        List<Order> createdOrders = new ArrayList<>();

        // Створюємо окреме замовлення для кожного продавця
        for (Map.Entry<Long, List<OrderItemRequest>> entry : sellerItemsMap.entrySet()) {
            Long sellerId = entry.getKey();
            List<OrderItemRequest> itemsForSeller = entry.getValue();

            Order order = new Order();
            order.setBuyerId(buyer.getId());
            order.setSellerId(sellerId);
            order.setStatus(OrderStatus.CREATED);
            order.setCreatedAt(LocalDateTime.now());

            BigDecimal totalAmount = BigDecimal.ZERO;

            for (OrderItemRequest itemReq : itemsForSeller) {
                Product product = productRepository.findById(itemReq.getProductId()).orElseThrow();

                if (!product.getIsActive() || product.getStockQuantity() < itemReq.getQuantity()) {
                    throw new ResourceConflictException("Недостатньо товару: " + product.getName());
                }

                // Списуємо зі складу (резервуємо товар)
                product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
                productRepository.save(product);

                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(product.getId());
                orderItem.setQuantity(itemReq.getQuantity());
                orderItem.setPriceAtPurchase(product.getPrice());

                totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
                order.addItem(orderItem);
            }

            order.setTotalAmount(totalAmount);
            createdOrders.add(orderRepository.save(order));
        }

        return createdOrders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ========================================================================
    // 2. UPDATE: МОДИФІКАЦІЯ ЗАМОВЛЕННЯ (Коригування до оплати)
    // ========================================================================

    @Transactional
    public OrderResponse updateOrder(Long orderId, OrderUpdateRequest request) {
        User buyer = getCurrentAuthenticatedUser();
        Order order = getOrderById(orderId);

        validateOrderOwnership(order, buyer);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ResourceConflictException("Можна змінювати тільки неоплачені замовлення (статус CREATED)");
        }

        // Повертаємо старі товари на склад
        restoreStockForOrder(order);
        order.getItems().clear();

        BigDecimal newTotalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Товар не знайдено"));

            // ВАЖЛИВО: Перевірка, що користувач не намагається вкинути сюди товар іншого продавця
            if (!product.getSellerId().equals(order.getSellerId())) {
                throw new ResourceConflictException("Ви не можете додати товар іншого продавця в це замовлення");
            }

            if (!product.getIsActive() || product.getStockQuantity() < itemReq.getQuantity()) {
                throw new ResourceConflictException("Недостатньо товару: " + product.getName());
            }

            // Знову списуємо зі складу
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            OrderItem newItem = new OrderItem();
            newItem.setProductId(product.getId());
            newItem.setQuantity(itemReq.getQuantity());
            newItem.setPriceAtPurchase(product.getPrice());

            newTotalAmount = newTotalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            order.addItem(newItem);
        }

        order.setTotalAmount(newTotalAmount);
        return mapToResponse(orderRepository.save(order));
    }

    // ========================================================================
    // 3. UPDATE: УПРАВЛІННЯ СТАТУСАМИ
    // ========================================================================

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        Order order = getOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        if (currentUser.getRole() != Role.ADMIN) {
            validateOrderOwnership(order, currentUser);
            if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CREATED) {
                throw new ResourceConflictException("Ви можете скасувати тільки неоплачене замовлення");
            }
            if (newStatus == OrderStatus.RETURN_REQUESTED && oldStatus != OrderStatus.DELIVERED) {
                throw new ResourceConflictException("Повернення можливе тільки для доставлених замовлень");
            }
            if (newStatus != OrderStatus.CANCELLED && newStatus != OrderStatus.RETURN_REQUESTED) {
                throw new AccessDeniedException("У вас немає прав встановлювати цей статус");
            }
        }

        boolean needsRestock = (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.REFUNDED);
        boolean alreadyRestocked = (oldStatus == OrderStatus.CANCELLED || oldStatus == OrderStatus.REFUNDED);

        if (needsRestock && !alreadyRestocked) {
            restoreStockForOrder(order);
        }

        order.setStatus(newStatus);
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse requestReturn(Long orderId) {
        User buyer = getCurrentAuthenticatedUser();
        Order order = getOrderById(orderId);
        validateOrderOwnership(order, buyer);

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ResourceConflictException("Повернення можливе тільки для доставлених замовлень");
        }

        order.setStatus(OrderStatus.RETURN_REQUESTED);
        return mapToResponse(orderRepository.save(order));
    }

    // ========================================================================
    // 4. READ: ОТРИМАННЯ ДАНИХ
    // ========================================================================

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(int page, int size) {
        User buyer = getCurrentAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByBuyerId(buyer.getId(), pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = getOrderById(orderId);
        User user = getCurrentAuthenticatedUser();

        // Читати може покупець, адмін, АБО продавець, якому належить це замовлення
        if (!order.getBuyerId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            SellerProfile profile = sellerProfileRepository.findByUserId(user.getId()).orElse(null);
            if (profile == null || !order.getSellerId().equals(profile.getId())) {
                throw new AccessDeniedException("У вас немає прав доступу до цього замовлення");
            }
        }
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersForMySellerProfile(int page, int size) {
        User currentUser = getCurrentAuthenticatedUser();
        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("У вас немає профілю продавця"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Тепер це дуже простий запит, бо sellerId є прямо в таблиці Orders!
        return orderRepository.findBySellerId(sellerProfile.getId(), pageable)
                .map(this::mapToResponse);
    }

    // ========================================================================
    // 5. ДОПОМІЖНІ МЕТОДИ
    // ========================================================================

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Замовлення не знайдено"));
    }

    private void validateOrderOwnership(Order order, User user) {
        if (!order.getBuyerId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("У вас немає прав доступу до цього замовлення");
        }
    }

    private void restoreStockForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            });
        }
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setBuyerId(order.getBuyerId());
        response.setSellerId(order.getSellerId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
            OrderItemResponse ir = new OrderItemResponse();
            ir.setId(item.getId());
            ir.setProductId(item.getProductId());
            ir.setQuantity(item.getQuantity());
            ir.setPriceAtPurchase(item.getPriceAtPurchase());
            return ir;
        }).collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }
}
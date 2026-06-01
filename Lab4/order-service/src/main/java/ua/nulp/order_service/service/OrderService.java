package ua.nulp.order_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nulp.order_service.client.AuthServiceClient;
import ua.nulp.order_service.client.ProductServiceClient;
import ua.nulp.order_service.client.dto.ProductResponse;
import ua.nulp.order_service.client.dto.SellerProfileFullResponse;
import ua.nulp.order_service.client.dto.SellerProfileResponse;
import ua.nulp.order_service.client.dto.UserResponse;
import ua.nulp.order_service.dto.order.*;
import ua.nulp.order_service.exception.ForbiddenOperationException;
import ua.nulp.order_service.exception.ResourceConflictException;
import ua.nulp.order_service.exception.ResourceNotFoundException;
import ua.nulp.order_service.model.*;
import ua.nulp.order_service.model.enums.OrderStatus;
import ua.nulp.order_service.repository.OrderRepository;

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
    private final AuthServiceClient authServiceClient;
    private final ProductServiceClient productServiceClient;

    public OrderService(OrderRepository orderRepository,
                        AuthServiceClient authServiceClient,
                        ProductServiceClient productServiceClient)
    {
        this.orderRepository = orderRepository;
        this.authServiceClient = authServiceClient;
        this.productServiceClient = productServiceClient;
    }

    // =================================================================================
    // 1. CREATE: Створення замовлень (Розбиття кошика з фронтенду по продавцях)
    // =================================================================================

    @Transactional
    public List<OrderResponse> createOrders(OrderCreateRequest request) {
        UserResponse buyer = authServiceClient.getCurrentUser();

        // Групуємо товари за sellerId
        Map<Long, List<OrderItemRequest>> sellerItemsMap = new HashMap<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            ProductResponse product = productServiceClient.getProductById(itemReq.getProductId());

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
                ProductResponse product = productServiceClient.getProductById(itemReq.getProductId());

                if (!product.getIsActive() || product.getStockQuantity() < itemReq.getQuantity()) {
                    throw new ResourceConflictException("Недостатньо товару: " + product.getName());
                }

                // Списуємо зі складу (резервуємо товар)
                productServiceClient.reduceStock(product.getId(), itemReq.getQuantity());

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
        UserResponse buyer = authServiceClient.getCurrentUser();
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
            ProductResponse product = productServiceClient.getProductById(itemReq.getProductId());

            // ВАЖЛИВО: Перевірка, що користувач не намагається вкинути сюди товар іншого продавця
            if (!product.getSellerId().equals(order.getSellerId())) {
                throw new ResourceConflictException("Ви не можете додати товар іншого продавця в це замовлення");
            }

            if (!product.getIsActive() || product.getStockQuantity() < itemReq.getQuantity()) {
                throw new ResourceConflictException("Недостатньо товару: " + product.getName());
            }

            // Знову списуємо зі складу
            productServiceClient.reduceStock(product.getId(), itemReq.getQuantity());

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
        UserResponse currentUser = authServiceClient.getCurrentUser();
        Order order = getOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        if (!currentUser.getRole().equals("ADMIN")) {
            validateOrderOwnership(order, currentUser);
            if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CREATED) {
                throw new ResourceConflictException("Ви можете скасувати тільки неоплачене замовлення");
            }
            if (newStatus == OrderStatus.RETURN_REQUESTED && oldStatus != OrderStatus.DELIVERED) {
                throw new ResourceConflictException("Повернення можливе тільки для доставлених замовлень");
            }
            if (newStatus != OrderStatus.CANCELLED && newStatus != OrderStatus.RETURN_REQUESTED) {
                throw new ForbiddenOperationException("У вас немає прав встановлювати цей статус");
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
        UserResponse buyer = authServiceClient.getCurrentUser();
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
        UserResponse buyer = authServiceClient.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByBuyerId(buyer.getId(), pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = getOrderById(orderId);
        UserResponse user = authServiceClient.getCurrentUser();

        // Читати може покупець, адмін, АБО продавець, якому належить це замовлення
        if (!order.getBuyerId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
            SellerProfileFullResponse profile = authServiceClient.getMyProfile();
            if (profile == null || !order.getSellerId().equals(profile.getId())) {
                throw new ForbiddenOperationException("У вас немає прав доступу до цього замовлення");
            }
        }
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersForMySellerProfile(int page, int size) {
        UserResponse currentUser = authServiceClient.getCurrentUser();
        SellerProfileFullResponse profile = authServiceClient.getMyProfile();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Тепер це дуже простий запит, бо sellerId є прямо в таблиці Orders!
        return orderRepository.findBySellerId(profile.getId(), pageable)
                .map(this::mapToResponse);
    }

    // ========================================================================
    // 5. ДОПОМІЖНІ МЕТОДИ
    // ========================================================================

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Замовлення не знайдено"));
    }

    private void validateOrderOwnership(Order order, UserResponse user) {
        if (!order.getBuyerId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
            throw new ForbiddenOperationException("У вас немає прав доступу до цього замовлення");
        }
    }

    private void restoreStockForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            productServiceClient.restoreStock(item.getProductId(), item.getQuantity());
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
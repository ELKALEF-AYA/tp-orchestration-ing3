package com.membership.order.application.service;

import com.membership.order.application.dto.*;
import com.membership.order.application.mapper.OrderMapper;
import com.membership.order.domain.entity.Order;
import com.membership.order.domain.entity.OrderItem;
import com.membership.order.domain.repository.OrderRepository;
import com.membership.order.domain.repository.OrderItemRepository;
import com.membership.order.infrastructure.client.ProductServiceClient;
import com.membership.order.infrastructure.client.UserServiceClient;
import com.membership.order.infrastructure.exception.InvalidOrderStateException;
import com.membership.order.infrastructure.exception.ResourceNotFoundException;
import com.membership.order.infrastructure.exception.ServiceUnavailableException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;

    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    // --- MÉTRIQUES ---
    private final Counter ordersCreatedCounter;

    private final Counter pendingCounter;
    private final Counter confirmedCounter;
    private final Counter shippedCounter;
    private final Counter deliveredCounter;
    private final Counter cancelledCounter;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderMapper orderMapper,
            UserServiceClient userServiceClient,
            ProductServiceClient productServiceClient,
            MeterRegistry meterRegistry
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMapper = orderMapper;
        this.userServiceClient = userServiceClient;
        this.productServiceClient = productServiceClient;

        this.ordersCreatedCounter = meterRegistry.counter("orders_created_total");

        this.pendingCounter = Counter.builder("orders_status_total")
                .tag("status", "PENDING")
                .description("Nombre de commandes passées au statut PENDING")
                .register(meterRegistry);

        this.confirmedCounter = Counter.builder("orders_status_total")
                .tag("status", "CONFIRMED")
                .description("Nombre de commandes passées au statut CONFIRMED")
                .register(meterRegistry);

        this.shippedCounter = Counter.builder("orders_status_total")
                .tag("status", "SHIPPED")
                .description("Nombre de commandes passées au statut SHIPPED")
                .register(meterRegistry);

        this.deliveredCounter = Counter.builder("orders_status_total")
                .tag("status", "DELIVERED")
                .description("Nombre de commandes passées au statut DELIVERED")
                .register(meterRegistry);

        this.cancelledCounter = Counter.builder("orders_status_total")
                .tag("status", "CANCELLED")
                .description("Nombre de commandes passées au statut CANCELLED")
                .register(meterRegistry);

        Gauge.builder("orders_total_amount_today", orderRepository,
                        repo -> Optional.ofNullable(repo.getTotalAmountToday())
                                .orElse(BigDecimal.ZERO)
                                .doubleValue())
                .register(meterRegistry);

        // Gauge : nombre de commandes par statut (état DB)
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            final Order.OrderStatus st = status;

            Gauge.builder("orders_by_status", orderRepository,
                            repo -> repo.countByStatus(st))
                    .tag("status", st.name())
                    .description("Nombre de commandes existantes en base par statut")
                    .register(meterRegistry);
        }
    }

    // ==================================================================
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponseDTO)
                .toList();
    }

    // ==================================================================
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return orderMapper.toResponseDTO(order);
    }

    // ==================================================================
    public List<OrderResponseDTO> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toResponseDTO)
                .toList();
    }

    // ==================================================================
    public List<OrderResponseDTO> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(orderMapper::toResponseDTO)
                .toList();
    }

    // ==================================================================
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        log.info("Création commande pour user {}", request.getUserId());

        // 1) Vérifier si user existe + actif
        if (!userServiceClient.isUserActive(request.getUserId())) {
            throw new IllegalArgumentException("Utilisateur inactif ou inexistant");
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getShippingAddress());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setItems(new ArrayList<>());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDTO : request.getItems()) {

            var product = productServiceClient.getProductById(itemDTO.getProductId());
            if (product == null || !product.getActive()) {
                throw new ResourceNotFoundException("Produit non trouvé ou inactif");
            }
            // 3c) Vérifier le stock (2 cas: rupture / insuffisant)
            int available = product.getStock();
            int requested = itemDTO.getQuantity();

            if (available == 0) {
                throw new IllegalArgumentException(
                        "Produit en rupture de stock : " + product.getName() + " (id=" + product.getId() + ")"
                );
            }

            if (available < requested) {
                throw new IllegalArgumentException(
                        "Stock insuffisant pour le produit " + product.getName()
                                + " (demandé=" + requested + ", disponible=" + available + ")"
                );
            }
            boolean updated = productServiceClient.updateStock(
                    product.getId(),
                    itemDTO.getQuantity(),
                    "SUBTRACT"
            );

            if (!updated) {
                throw new ServiceUnavailableException("Impossible de mettre à jour le stock du produit");
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

            order.addItem(item);
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        order.setTotalAmount(totalAmount);

        Order saved = orderRepository.save(order);
        pendingCounter.increment();

        ordersCreatedCounter.increment();

        return orderMapper.toResponseDTO(saved);
    }

    // ==================================================================
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, OrderStatusUpdateDTO dto) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        if (!order.isModifiable()) {
            throw new InvalidOrderStateException(id, order.getStatus().name(), "modifier");
        }

        if (order.getStatus() == dto.getStatus()) {
            return orderMapper.toResponseDTO(order);
        }

        order.setStatus(dto.getStatus());

        switch (dto.getStatus()) {
            case PENDING -> pendingCounter.increment();
            case CONFIRMED -> confirmedCounter.increment();
            case SHIPPED -> shippedCounter.increment();
            case DELIVERED -> deliveredCounter.increment();
            case CANCELLED -> cancelledCounter.increment();
        }

        Order saved = orderRepository.save(order);
        return orderMapper.toResponseDTO(saved);
    }

    // ==================================================================
    @Transactional
    public void cancelOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            return;
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
        cancelledCounter.increment();
    }

    // ==================================================================
    public boolean isProductUsedInAnyOrder(Long productId) {
        return orderItemRepository.existsByProductId(productId);
    }
}
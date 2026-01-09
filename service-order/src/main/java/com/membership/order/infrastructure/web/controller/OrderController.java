package com.membership.order.infrastructure.web.controller;

import com.membership.order.application.dto.OrderRequestDTO;
import com.membership.order.application.dto.OrderResponseDTO;
import com.membership.order.application.dto.OrderStatusUpdateDTO;
import com.membership.order.application.service.OrderService;
import com.membership.order.domain.entity.Order.OrderStatus;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour les commandes
 * Endpoints: /api/v1/orders
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/v1/orders
     * Créer une nouvelle commande
     */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO requestDTO) {
        log.info("POST /orders - Création commande");
        return ResponseEntity.ok(orderService.createOrder(requestDTO));
    }

    /**
     * GET /api/v1/orders/{id}
     * Récupérer une commande par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        log.info("GET /orders/{}", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * GET /api/v1/orders
     * Récupérer toutes les commandes
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        log.info("GET /orders");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * GET /api/v1/orders/user/{userId}
     * Récupérer l'historique d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(@PathVariable Long userId) {
        log.info("GET /orders/user/{}", userId);
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    /**
     * GET /api/v1/orders/status/{status}
     * Filtrer par statut: PENDING, SHIPPED, DELIVERED, CANCELLED
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@PathVariable String status) {
        log.info("GET /orders/status/{}", status);
        OrderStatus enumStatus = OrderStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(orderService.getOrdersByStatus(enumStatus));
    }

    /**
     * PUT /api/v1/orders/{id}/status
     * Mettre à jour le statut d'une commande
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDTO statusUpdateDTO) {

        log.info("PUT /orders/{}/status = {}", id, statusUpdateDTO.getStatus());
        return ResponseEntity.ok(orderService.updateOrderStatus(id, statusUpdateDTO));
    }

    /**
     * DELETE /api/v1/orders/{id}
     * Annuler une commande (soft delete via statut CANCELLED)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        log.info("DELETE /orders/{} - Annulation", id);
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/orders/exists/product/{productId}
     * Vérifier si un produit a été commandé
     */
    @GetMapping("/exists/product/{productId}")
    public ResponseEntity<Boolean> isProductUsedInAnyOrder(@PathVariable Long productId) {
        log.info("GET /orders/exists/product/{}", productId);
        return ResponseEntity.ok(orderService.isProductUsedInAnyOrder(productId));
    }
}
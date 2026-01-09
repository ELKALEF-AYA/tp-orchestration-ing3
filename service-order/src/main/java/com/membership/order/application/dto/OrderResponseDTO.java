package com.membership.order.application.dto;

import com.membership.order.domain.entity.Order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour les réponses contenant une commande complète.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemResponseDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Indique si la commande peut être modifiée.
     */
    public boolean isModifiable() {
        return status != OrderStatus.DELIVERED && status != OrderStatus.CANCELLED;
    }

    /**
     * Compte le nombre total d'articles dans la commande.
     */
    public int getTotalItemsCount() {
        return items != null ? items.stream()
                .mapToInt(OrderItemResponseDTO::getQuantity)
                .sum() : 0;
    }
}

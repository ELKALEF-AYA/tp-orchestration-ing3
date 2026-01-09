package com.membership.order.application.dto;

import com.membership.order.domain.entity.Order.OrderStatus;
import lombok.Data;

/**
 * DTO pour mettre à jour le statut d'une commande.
 */
@Data
public class OrderStatusUpdateDTO {
    private OrderStatus status;
}

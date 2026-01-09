package com.membership.order.application.mapper;

import com.membership.order.application.dto.*;
import com.membership.order.domain.entity.Order;
import com.membership.order.domain.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre les entités Order/OrderItem
 * et leurs DTOs correspondants.
 *
 * Responsabilités :
 * - Conversion Order -> OrderResponseDTO
 * - Conversion OrderItem -> OrderItemResponseDTO
 * - Conversion OrderItemRequestDTO -> OrderItem (avec ProductDTO)
 * - Gestion de la sécurité null
 */

@Component
public class OrderMapper {
    /**
     * Convertit une entité Order complète en OrderResponseDTO.
     * Inclut la conversion de tous les OrderItems associés.
     *
     * @param order l'entité Order à convertir
     * @return le DTO réponse, null si l'ordre est null
     */
    public OrderResponseDTO toResponseDTO(Order order) {
        if (order == null) return null;
            // Conversion des items de la commande
        List<OrderItemResponseDTO> itemsDTO = order.getItems().stream()
                .map(this::toItemResponseDTO)
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(itemsDTO)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    /**
     * Convertit une entité OrderItem en OrderItemResponseDTO.
     *
     * @param item l'entité OrderItem à convertir
     * @return le DTO réponse, null si l'item est null
     */

    public OrderItemResponseDTO toItemResponseDTO(OrderItem item) {
        if (item == null) return null;

        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    public OrderItem toItemEntity(OrderItemRequestDTO itemDTO, ProductDTO product) {
        if (itemDTO == null || product == null) return null;

        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

        return OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(itemDTO.getQuantity())
                .unitPrice(product.getPrice())
                .subtotal(subtotal)
                .build();
    }

    public List<OrderResponseDTO> toResponseList(List<Order> orders) {
        if (orders == null) return List.of();

        return orders.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}

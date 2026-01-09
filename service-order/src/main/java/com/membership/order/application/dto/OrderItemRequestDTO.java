package com.membership.order.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les items d'une commande dans les requêtes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequestDTO {

    @NotNull(message = "L'ID du produit ne peut pas être nul")
    @Positive(message = "L'ID du produit doit être positif")
    private Long productId;

    @NotNull(message = "La quantité ne peut pas être nulle")
    @Positive(message = "La quantité doit être positive")
    private Integer quantity;
}

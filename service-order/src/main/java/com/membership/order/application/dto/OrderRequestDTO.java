package com.membership.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la création d'une commande.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {

    @NotNull(message = "L'ID utilisateur ne peut pas être nul")
    @Positive(message = "L'ID utilisateur doit être positif")
    private Long userId;

    @NotBlank(message = "L'adresse de livraison ne peut pas être vide")
    private String shippingAddress;

    @NotEmpty(message = "La commande doit contenir au moins un article")
    @Valid
    private List<OrderItemRequestDTO> items;
}

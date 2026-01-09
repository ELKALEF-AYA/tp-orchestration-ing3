package com.example.product.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la mise à jour du stock d'un produit.
 *
 * Utilisé par l'endpoint PATCH /api/v1/products/{id}/stock
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdateDTO {

    @NotNull(message = "La quantité ne peut pas être nulle")
    private Integer quantity;

    /**
     * Type d'opération sur le stock.
     * - ADD : Ajouter au stock existant
     * - SUBTRACT : Soustraire du stock existant
     * - SET : Définir une nouvelle valeur absolue
     */
    private StockOperation operation;

    public enum StockOperation {
        ADD,        // Ajouter au stock (réapprovisionnement)
        SUBTRACT,   // Retirer du stock (commande)
        SET         // Définir le stock (inventaire)
    }
}
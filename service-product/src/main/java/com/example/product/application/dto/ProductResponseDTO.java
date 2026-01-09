package com.example.product.application.dto;

import com.example.product.domain.entity.Product.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les réponses contenant les informations d'un produit.
 *
 * Contient tous les champs y compris les métadonnées (id, dates).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private ProductCategory category;
    private String imageUrl;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Indique si le produit est disponible (actif et en stock).
     */
    public boolean isAvailable() {
        return active != null && active && stock != null && stock > 0;
    }

    /**
     * Indique si le stock est bas (< 5).
     */
    public boolean isLowStock() {
        return stock != null && stock < 5 && stock > 0;
    }
}
package com.membership.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour recevoir les informations d'un produit depuis le service Product.
 * Simplifié - contient uniquement les champs nécessaires.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private Boolean active;
    private LocalDateTime createdAt;
}

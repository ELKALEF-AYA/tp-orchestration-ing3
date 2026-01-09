package com.example.product.application.dto;

import com.example.product.domain.entity.Product.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour les requêtes de création et modification de produits.
 *
 * Séparation Request/Response pour :
 * - Ne pas exposer les champs internes (id, createdAt, etc.)
 * - Validation différente selon le contexte
 * - Évolution indépendante des contrats API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    @NotBlank(message = "Le nom du produit ne peut pas être vide")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    private String name;

    @NotBlank(message = "La description ne peut pas être vide")
    @Size(min = 10, max = 500, message = "La description doit contenir entre 10 et 500 caractères")
    private String description;

    @NotNull(message = "Le prix ne peut pas être nul")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @Digits(integer = 10, fraction = 2, message = "Le prix doit avoir maximum 2 décimales")
    private BigDecimal price;

    @NotNull(message = "Le stock ne peut pas être nul")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stock;

    @NotNull(message = "La catégorie ne peut pas être nulle")
    private ProductCategory category;

    @Size(max = 255, message = "L'URL de l'image ne peut pas dépasser 255 caractères")
    private String imageUrl;

    private Boolean active;
}
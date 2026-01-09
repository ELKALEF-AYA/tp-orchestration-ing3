package com.example.product.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité Product représentant un produit dans le catalogue.
 *
 * Best practices :
 * - Utilisation de Lombok pour réduire le boilerplate
 * - Validation avec Bean Validation (JSR-380)
 * - Audit automatique avec @CreationTimestamp et @UpdateTimestamp
 * - Builder pattern pour une construction flexible
 * - Enum pour les catégories (type-safe)
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du produit ne peut pas être vide")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "La description ne peut pas être vide")
    @Size(min = 10, max = 500, message = "La description doit contenir entre 10 et 500 caractères")
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @NotNull(message = "Le prix ne peut pas être nul")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @Digits(integer = 10, fraction = 2, message = "Le prix doit avoir maximum 2 décimales")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Le stock ne peut pas être nul")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @NotNull(message = "La catégorie ne peut pas être nulle")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ProductCategory category;

    @Size(max = 255, message = "L'URL de l'image ne peut pas dépasser 255 caractères")
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum pour les catégories de produits.
     * Type-safe et facilite la validation.
     */
    public enum ProductCategory {
        ELECTRONICS("Électronique"),
        BOOKS("Livres"),
        FOOD("Alimentation"),
        OTHER("Autre");

        private final String displayName;

        ProductCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
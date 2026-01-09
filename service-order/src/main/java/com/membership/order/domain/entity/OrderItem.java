package com.membership.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "L'ID produit ne peut pas être nul")
    @Positive(message = "L'ID produit doit être positif")
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @NotBlank(message = "Le nom du produit ne peut pas être vide")
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @NotNull(message = "La quantité ne peut pas être nulle")
    @Positive(message = "La quantité doit être positive")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "Le prix unitaire ne peut pas être nul")
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "Le sous-total ne peut pas être nul")
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Calcule automatiquement le sous-total (quantity * unitPrice).
     */
    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}

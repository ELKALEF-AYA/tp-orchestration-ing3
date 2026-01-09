package com.membership.order.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'ID utilisateur ne peut pas être nul")
    @Positive(message = "L'ID utilisateur doit être positif")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @NotNull(message = "Le statut ne peut pas être nul")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @NotNull(message = "Le montant total ne peut pas être nul")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @NotBlank(message = "L'adresse de livraison ne peut pas être vide")
    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    /**
     * Enum pour les statuts de commande.
     */
    public enum OrderStatus {
        PENDING("En attente"),
        CONFIRMED("Confirmée"),
        SHIPPED("Expédiée"),
        DELIVERED("Livrée"),
        CANCELLED("Annulée");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Méthode helper pour ajouter un item à la commande.
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Méthode helper pour retirer un item de la commande.
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    /**
     * Vérifie si la commande peut être modifiée.
     */
    public boolean isModifiable() {
        return status != OrderStatus.DELIVERED && status != OrderStatus.CANCELLED;
    }

    /**
     * Calcule le montant total à partir des items.
     */
    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}

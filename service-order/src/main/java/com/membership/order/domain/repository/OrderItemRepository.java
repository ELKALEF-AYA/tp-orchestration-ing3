package com.membership.order.domain.repository;

import com.membership.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository pour gérer les articles de commande (OrderItem) dans la base de données.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {


    boolean existsByProductId(Long productId);
}

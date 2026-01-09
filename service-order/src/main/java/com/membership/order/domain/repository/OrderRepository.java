package com.membership.order.domain.repository;

import com.membership.order.domain.entity.Order;
import com.membership.order.domain.entity.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    List<Order> findByUserId(Long userId);


    List<Order> findByStatus(OrderStatus status);


    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);


    long countByStatus(OrderStatus status);


    List<Order> findByCreatedAtAfter(LocalDateTime date);


    @Query("""
       SELECT COALESCE(SUM(o.totalAmount), 0)
       FROM Order o
       WHERE CAST(o.createdAt AS date) = CURRENT_DATE
       """)
    BigDecimal getTotalAmountToday();




    boolean existsByUserId(Long userId);


    long countByUserIdAndStatus(Long userId, OrderStatus status);
}

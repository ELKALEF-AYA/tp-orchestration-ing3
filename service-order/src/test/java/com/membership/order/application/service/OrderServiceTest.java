package com.membership.order.application.service;

import com.membership.order.application.dto.*;
import com.membership.order.application.mapper.OrderMapper;
import com.membership.order.domain.entity.Order;
import com.membership.order.domain.entity.Order.OrderStatus;
import com.membership.order.domain.entity.OrderItem;
import com.membership.order.domain.repository.OrderRepository;
import com.membership.order.domain.repository.OrderItemRepository;
import com.membership.order.infrastructure.client.ProductServiceClient;
import com.membership.order.infrastructure.client.UserServiceClient;
import com.membership.order.infrastructure.exception.InvalidOrderStateException;
import com.membership.order.infrastructure.exception.ResourceNotFoundException;
import com.membership.order.infrastructure.exception.ServiceUnavailableException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private ProductServiceClient productServiceClient;

    private OrderMapper orderMapper;
    private SimpleMeterRegistry meterRegistry;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
        meterRegistry = new SimpleMeterRegistry();

        orderService = new OrderService(
                orderRepository,
                orderItemRepository,
                orderMapper,
                userServiceClient,
                productServiceClient,
                meterRegistry
        );
    }

    // ----------------------------------------------------
    // GET ALL ORDERS
    // ----------------------------------------------------
    @Test
    void testGetAllOrders() {
        Order o1 = createOrder(1L, 1L, OrderStatus.PENDING);
        Order o2 = createOrder(2L, 2L, OrderStatus.CONFIRMED);

        when(orderRepository.findAll()).thenReturn(List.of(o1, o2));

        var result = orderService.getAllOrders();

        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }

    // ----------------------------------------------------
    // GET ORDER BY ID
    // ----------------------------------------------------
    @Test
    void testGetOrderByIdSuccess() {
        Order order = createOrder(1L, 1L, OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var result = orderService.getOrderById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void testGetOrderByIdNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(1L));
    }

    // ----------------------------------------------------
    // GET ORDERS BY USER
    // ----------------------------------------------------
    @Test
    void testGetOrdersByUser() {
        Long userId = 10L;

        Order o1 = createOrder(1L, userId, OrderStatus.PENDING);
        Order o2 = createOrder(2L, userId, OrderStatus.DELIVERED);

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(o1, o2));

        var result = orderService.getOrdersByUser(userId);

        assertEquals(2, result.size());
        verify(orderRepository).findByUserId(userId);
    }

    // ----------------------------------------------------
    // GET BY STATUS
    // ----------------------------------------------------
    @Test
    void testGetOrdersByStatus() {
        Order o1 = createOrder(1L, 1L, OrderStatus.PENDING);
        Order o2 = createOrder(2L, 3L, OrderStatus.PENDING);

        when(orderRepository.findByStatus(OrderStatus.PENDING))
                .thenReturn(List.of(o1, o2));

        var result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertEquals(2, result.size());
    }

    // ----------------------------------------------------
    // UPDATE ORDER STATUS
    // ----------------------------------------------------
    @Test
    void testUpdateOrderStatusSuccess() {
        Order order = createOrder(1L, 1L, OrderStatus.PENDING);
        OrderStatusUpdateDTO dto = new OrderStatusUpdateDTO();
        dto.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        var response = orderService.updateOrderStatus(1L, dto);

        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void testUpdateStatusNotModifiable() {
        Order order = createOrder(1L, 1L, OrderStatus.DELIVERED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderStatusUpdateDTO dto = new OrderStatusUpdateDTO();
        dto.setStatus(OrderStatus.SHIPPED);

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.updateOrderStatus(1L, dto));
    }

    @Test
    void testUpdateOrderStatusDeliveredCounterIncrements() {
        Order order = createOrder(1L, 1L, OrderStatus.CONFIRMED);
        OrderStatusUpdateDTO dto = new OrderStatusUpdateDTO();
        dto.setStatus(OrderStatus.DELIVERED);

        Counter deliveredCounter = meterRegistry.counter("orders_delivered_total");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        orderService.updateOrderStatus(1L, dto);

        assertEquals(1.0, deliveredCounter.count());
    }

    // ----------------------------------------------------
    // CREATE ORDER
    // ----------------------------------------------------
    @Test
    void testCreateOrderSuccess() {

        OrderRequestDTO request = OrderRequestDTO.builder()
                .userId(1L)
                .shippingAddress("Adresse test")
                .items(List.of(
                        OrderItemRequestDTO.builder()
                                .productId(10L)
                                .quantity(2)
                                .build()
                ))
                .build();

        ProductDTO product = ProductDTO.builder()
                .id(10L)
                .name("Produit A")
                .price(BigDecimal.valueOf(50))
                .stock(10)
                .active(true)
                .build();

        when(userServiceClient.isUserActive(1L)).thenReturn(true);
        when(productServiceClient.getProductById(10L)).thenReturn(product);
        when(productServiceClient.updateStock(10L, 2, "SUBTRACT")).thenReturn(true);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = orderService.createOrder(request);

        assertEquals(BigDecimal.valueOf(100), result.getTotalAmount());
    }

    @Test
    void testCreateOrderUserInactive() {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .userId(1L)
                .shippingAddress("Adresse test")
                .items(List.of())
                .build();

        when(userServiceClient.isUserActive(1L)).thenReturn(false);

        assertThrows(ServiceUnavailableException.class,
                () -> orderService.createOrder(request));
    }

    @Test
    void testCreateOrderInsufficientStock() {

        OrderRequestDTO request = OrderRequestDTO.builder()
                .userId(1L)
                .shippingAddress("Adresse test")
                .items(List.of(
                        OrderItemRequestDTO.builder()
                                .productId(10L)
                                .quantity(10)
                                .build()
                ))
                .build();

        ProductDTO product = ProductDTO.builder()
                .id(10L)
                .name("Produit A")
                .price(BigDecimal.valueOf(50))
                .stock(5)
                .active(true)
                .build();

        when(userServiceClient.isUserActive(1L)).thenReturn(true);
        when(productServiceClient.getProductById(10L)).thenReturn(product);

        assertThrows(RuntimeException.class,
                () -> orderService.createOrder(request));
    }

    @Test
    void testCreateOrderStockUpdateFails() {

        OrderRequestDTO request = OrderRequestDTO.builder()
                .userId(1L)
                .shippingAddress("Adresse")
                .items(List.of(
                        OrderItemRequestDTO.builder()
                                .productId(10L)
                                .quantity(1)
                                .build()
                ))
                .build();

        ProductDTO product = ProductDTO.builder()
                .id(10L)
                .name("Produit A")
                .price(BigDecimal.valueOf(10))
                .stock(5)
                .active(true)
                .build();

        when(userServiceClient.isUserActive(1L)).thenReturn(true);
        when(productServiceClient.getProductById(10L)).thenReturn(product);
        when(productServiceClient.updateStock(any(), any(), any()))
                .thenReturn(false);

        assertThrows(ServiceUnavailableException.class,
                () -> orderService.createOrder(request));
    }

    // ----------------------------------------------------
    // CANCEL ORDER
    // ----------------------------------------------------
    @Test
    void testCancelOrderSuccess() {
        Order order = createOrder(1L, 1L, OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        verify(orderRepository).save(order);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void testCancelOrderNotModifiable() {
        Order order = createOrder(1L, 1L, OrderStatus.DELIVERED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.cancelOrder(1L));
    }

    // ----------------------------------------------------
    // PRODUCT USED IN ANY ORDER
    // ----------------------------------------------------
    @Test
    void testIsProductUsedInAnyOrderTrue() {
        when(orderItemRepository.existsByProductId(10L)).thenReturn(true);

        assertTrue(orderService.isProductUsedInAnyOrder(10L));
    }

    @Test
    void testIsProductUsedInAnyOrderFalse() {
        when(orderItemRepository.existsByProductId(10L)).thenReturn(false);

        assertFalse(orderService.isProductUsedInAnyOrder(10L));
    }

    // ----------------------------------------------------
    // HELPER
    // ----------------------------------------------------
    private Order createOrder(Long id, Long userId, OrderStatus status) {
        return Order.builder()
                .id(id)
                .userId(userId)
                .orderDate(LocalDateTime.now())
                .status(status)
                .totalAmount(BigDecimal.TEN)
                .shippingAddress("Adresse")
                .items(new ArrayList<>())
                .build();
    }
}

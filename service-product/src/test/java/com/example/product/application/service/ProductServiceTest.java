package com.example.product.application.service;

import com.example.product.application.dto.ProductRequestDTO;
import com.example.product.application.dto.ProductResponseDTO;
import com.example.product.application.dto.StockUpdateDTO;
import com.example.product.application.dto.StockUpdateDTO.StockOperation;
import com.example.product.application.mapper.ProductMapper;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.Product.ProductCategory;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.infrastructure.client.OrderServiceClient;
import com.example.product.infrastructure.exception.InsufficientStockException;
import com.example.product.infrastructure.exception.ResourceAlreadyExistsException;
import com.example.product.infrastructure.exception.ResourceNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ProductService.
 *
 * Vérifie :
 * - La récupération et recherche de produits
 * - La création / mise à jour
 * - Les opérations de stock
 * - La suppression (bloquée si produit utilisé dans une commande)
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderServiceClient orderServiceClient;

    private ProductMapper productMapper;
    private MeterRegistry meterRegistry;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
        meterRegistry = new SimpleMeterRegistry();
        productService = new ProductService(productRepository, productMapper, meterRegistry, orderServiceClient);
    }

    @Test
    void testGetAllProducts() {
        // Given
        Product product1 = createTestProduct(1L, "Product 1", ProductCategory.ELECTRONICS, 10, true);
        Product product2 = createTestProduct(2L, "Product 2", ProductCategory.BOOKS, 20, true);
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        // When
        List<ProductResponseDTO> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void testGetProductById_Success() {
        // Given
        Long productId = 1L;
        Product product = createTestProduct(productId, "Test Product", ProductCategory.ELECTRONICS, 10, true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        ProductResponseDTO result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getName());
        verify(productRepository).findById(productId);
    }

    @Test
    void testGetProductById_NotFound() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(productId));
        verify(productRepository).findById(productId);
    }

    @Test
    void testCreateProduct_Success() {
        // Given
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("New Product")
                .description("This is a new test product description long enough")
                .price(new BigDecimal("99.99"))
                .stock(50)
                .category(ProductCategory.ELECTRONICS)
                .imageUrl("https://example.com/p.jpg")
                .build();

        Product savedProduct = createTestProduct(1L, "New Product", ProductCategory.ELECTRONICS, 50, true);

        when(productRepository.existsByName(requestDTO.getName())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        ProductResponseDTO result = productService.createProduct(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals("New Product", result.getName());
        verify(productRepository).existsByName(requestDTO.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testCreateProduct_AlreadyExists() {
        // Given
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("Existing Product")
                .description("This is a test product description long enough")
                .price(new BigDecimal("99.99"))
                .stock(50)
                .category(ProductCategory.ELECTRONICS)
                .build();

        when(productRepository.existsByName(requestDTO.getName())).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> productService.createProduct(requestDTO));
        verify(productRepository).existsByName(requestDTO.getName());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_Success() {
        // Given
        Long productId = 1L;
        Product existing = createTestProduct(productId, "Old Name", ProductCategory.ELECTRONICS, 10, true);

        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("Updated Name")
                .description("Updated description long enough")
                .price(new BigDecimal("129.99"))
                .stock(15)
                .category(ProductCategory.ELECTRONICS)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(productRepository.existsByNameAndIdNot(requestDTO.getName(), productId)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ProductResponseDTO result = productService.updateProduct(productId, requestDTO);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(productRepository).findById(productId);
        verify(productRepository).existsByNameAndIdNot(requestDTO.getName(), productId);
        verify(productRepository).save(existing);
    }

    @Test
    void testUpdateProduct_NameAlreadyUsedByAnotherProduct() {
        // Given
        Long productId = 1L;
        Product existing = createTestProduct(productId, "Old Name", ProductCategory.ELECTRONICS, 10, true);

        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("Duplicate Name")
                .description("Updated description long enough")
                .price(new BigDecimal("129.99"))
                .stock(15)
                .category(ProductCategory.ELECTRONICS)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(productRepository.existsByNameAndIdNot(requestDTO.getName(), productId)).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> productService.updateProduct(productId, requestDTO));

        verify(productRepository).findById(productId);
        verify(productRepository).existsByNameAndIdNot(requestDTO.getName(), productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateStock_Subtract_Success() {
        // Given
        Long productId = 1L;
        Product product = createTestProduct(productId, "Test Product", ProductCategory.ELECTRONICS, 50, true);

        StockUpdateDTO stockUpdateDTO = StockUpdateDTO.builder()
                .quantity(10)
                .operation(StockOperation.SUBTRACT)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ProductResponseDTO result = productService.updateStock(productId, stockUpdateDTO);

        // Then
        assertNotNull(result);
        assertEquals(40, result.getStock());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }

    @Test
    void testUpdateStock_Subtract_InsufficientStock() {
        // Given
        Long productId = 1L;
        Product product = createTestProduct(productId, "Test Product", ProductCategory.ELECTRONICS, 5, true);

        StockUpdateDTO stockUpdateDTO = StockUpdateDTO.builder()
                .quantity(10)
                .operation(StockOperation.SUBTRACT)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> productService.updateStock(productId, stockUpdateDTO));

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testUpdateStock_Add_Success() {
        // Given
        Long productId = 1L;
        Product product = createTestProduct(productId, "Test Product", ProductCategory.ELECTRONICS, 50, true);

        StockUpdateDTO stockUpdateDTO = StockUpdateDTO.builder()
                .quantity(25)
                .operation(StockOperation.ADD)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ProductResponseDTO result = productService.updateStock(productId, stockUpdateDTO);

        // Then
        assertNotNull(result);
        assertEquals(75, result.getStock());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }

    @Test
    void testSearchProductsByName() {
        // Given
        String searchTerm = "laptop";
        Product product1 = createTestProduct(1L, "Laptop Dell", ProductCategory.ELECTRONICS, 10, true);
        Product product2 = createTestProduct(2L, "Laptop HP", ProductCategory.ELECTRONICS, 10, true);

        when(productRepository.findByNameContainingIgnoreCase(searchTerm))
                .thenReturn(Arrays.asList(product1, product2));

        // When
        List<ProductResponseDTO> result = productService.searchProductsByName(searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findByNameContainingIgnoreCase(searchTerm);
    }

    @Test
    void testGetProductsByCategory_Success() {
        // Given
        Product product1 = createTestProduct(1L, "Book 1", ProductCategory.BOOKS, 10, true);
        Product product2 = createTestProduct(2L, "Book 2", ProductCategory.BOOKS, 10, true);

        when(productRepository.findByCategory(ProductCategory.BOOKS))
                .thenReturn(Arrays.asList(product1, product2));

        // When
        List<ProductResponseDTO> result = productService.getProductsByCategory("BOOKS");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findByCategory(ProductCategory.BOOKS);
    }

    @Test
    void testGetProductsByCategory_InvalidCategory() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> productService.getProductsByCategory("INVALID"));
        verify(productRepository, never()).findByCategory(any());
    }

    @Test
    void testGetAvailableProducts_FiltersInactive() {
        // Given
        Product activeInStock = createTestProduct(1L, "Active", ProductCategory.ELECTRONICS, 10, true);
        Product inactiveInStock = createTestProduct(2L, "Inactive", ProductCategory.ELECTRONICS, 10, false);

        when(productRepository.findByStockGreaterThan(0))
                .thenReturn(Arrays.asList(activeInStock, inactiveInStock));

        // When
        List<ProductResponseDTO> result = productService.getAvailableProducts();

        // Then
        assertEquals(1, result.size());
        assertEquals("Active", result.get(0).getName());
        verify(productRepository).findByStockGreaterThan(0);
    }

    @Test
    void testCountLowStockProducts() {
        // Given
        when(productRepository.countByStockLessThan(5)).thenReturn(3L);

        // When
        long count = productService.countLowStockProducts();

        // Then
        assertEquals(3L, count);
        verify(productRepository).countByStockLessThan(5);
    }

    @Test
    void testDeleteProduct_Success_WhenNotUsedInOrder() {
        // Given
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);
        when(orderServiceClient.isProductUsedInAnyOrder(productId)).thenReturn(false);

        // When
        assertDoesNotThrow(() -> productService.deleteProduct(productId));

        // Then
        verify(productRepository).existsById(productId);
        verify(orderServiceClient).isProductUsedInAnyOrder(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    void testDeleteProduct_Blocked_WhenUsedInOrder() {
        // Given
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);
        when(orderServiceClient.isProductUsedInAnyOrder(productId)).thenReturn(true);

        // When & Then
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> productService.deleteProduct(productId));

        // Petit check sur le message (adapte si tu as un autre texte exact)
        assertTrue(ex.getMessage().toLowerCase().contains("suppression"));

        verify(productRepository).existsById(productId);
        verify(orderServiceClient).isProductUsedInAnyOrder(productId);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteProduct_NotFound() {
        // Given
        Long productId = 999L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(productId));

        verify(productRepository).existsById(productId);
        verify(orderServiceClient, never()).isProductUsedInAnyOrder(anyLong());
        verify(productRepository, never()).deleteById(anyLong());
    }

    // Helper pour créer des produits de test
    private Product createTestProduct(Long id, String name, ProductCategory category, int stock, boolean active) {
        return Product.builder()
                .id(id)
                .name(name)
                .description("Test product description that is long enough")
                .price(new BigDecimal("99.99"))
                .stock(stock)
                .category(category)
                .active(active)
                .build();
    }
}
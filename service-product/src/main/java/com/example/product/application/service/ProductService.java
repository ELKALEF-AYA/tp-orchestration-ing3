package com.example.product.application.service;

import com.example.product.application.dto.ProductRequestDTO;
import com.example.product.application.dto.ProductResponseDTO;
import com.example.product.application.dto.StockUpdateDTO;
import com.example.product.application.mapper.ProductMapper;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.Product.ProductCategory;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.infrastructure.exception.InsufficientStockException;
import com.example.product.infrastructure.exception.ResourceAlreadyExistsException;
import com.example.product.infrastructure.exception.ResourceNotFoundException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.product.infrastructure.client.OrderServiceClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des produits.
 *
 * Responsabilités :
 * - Logique métier et règles de gestion
 * - Validation des opérations
 * - Gestion des transactions
 * - Métriques personnalisées
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final OrderServiceClient orderServiceClient;

    // Compteurs Prometheus par catégorie
    private final Counter electronicsCounter;
    private final Counter booksCounter;
    private final Counter foodCounter;
    private final Counter otherCounter;

    public ProductService(ProductRepository productRepository,
                          ProductMapper productMapper,
                          MeterRegistry meterRegistry,
                          OrderServiceClient orderServiceClient) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.orderServiceClient = orderServiceClient;

        // Initialisation des compteurs de produits créés par catégorie
        this.electronicsCounter = Counter.builder("products.created.total")
                .tag("category", "ELECTRONICS")
                .description("Nombre de produits électroniques créés")
                .register(meterRegistry);

        this.booksCounter = Counter.builder("products.created.total")
                .tag("category", "BOOKS")
                .description("Nombre de livres créés")
                .register(meterRegistry);

        this.foodCounter = Counter.builder("products.created.total")
                .tag("category", "FOOD")
                .description("Nombre de produits alimentaires créés")
                .register(meterRegistry);

        this.otherCounter = Counter.builder("products.created.total")
                .tag("category", "OTHER")
                .description("Nombre d'autres produits créés")
                .register(meterRegistry);

        // produits out-of-stock (état DB)
        Gauge.builder("products_out_of_stock", productRepository,
                        repo -> repo.countByStockAndActiveTrue(0))
                .description("Nombre de produits actifs en rupture (stock=0)")
                .register(meterRegistry);

        // total de produits existants en base par catégorie (état DB)
        for (ProductCategory category : ProductCategory.values()) {
            final ProductCategory cat = category;

            Gauge.builder("products_existing_total", productRepository,
                            repo -> repo.countByCategory(cat))
                    .tag("category", cat.name())
                    .description("Nombre total de produits existants en base par catégorie")
                    .register(meterRegistry);
        }
    }

    /**
     * Récupère tous les produits.
     */
    public List<ProductResponseDTO> getAllProducts() {
        log.info("Récupération de tous les produits");
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un produit par son ID.
     */
    public ProductResponseDTO getProductById(Long id) {
        log.info("Récupération du produit avec l'ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", id));

        return productMapper.toResponseDTO(product);
    }

    /**
     * Crée un nouveau produit.
     */
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        log.info("Création d'un nouveau produit: {}", requestDTO.getName());

        // Vérifier que le produit n'existe pas déjà
        if (productRepository.existsByName(requestDTO.getName())) {
            throw new ResourceAlreadyExistsException("Produit", "nom", requestDTO.getName());
        }

        // Convertir DTO en entité
        Product product = productMapper.toEntity(requestDTO);

        // Sauvegarder
        Product savedProduct = productRepository.save(product);

        // Incrémenter le compteur de la catégorie correspondante
        incrementCategoryCounter(savedProduct.getCategory());

        log.info("Produit créé avec succès, ID: {}", savedProduct.getId());
        return productMapper.toResponseDTO(savedProduct);
    }

    /**
     * Met à jour un produit existant.
     */
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO requestDTO) {
        log.info("Mise à jour du produit avec l'ID: {}", id);

        // Vérifier que le produit existe
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", id));

        // Vérifier qu'aucun autre produit n'a le même nom
        if (productRepository.existsByNameAndIdNot(requestDTO.getName(), id)) {
            throw new ResourceAlreadyExistsException("Produit", "nom", requestDTO.getName());
        }

        // Mettre à jour l'entité
        productMapper.updateEntityFromDTO(existingProduct, requestDTO);

        // Sauvegarder
        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Produit mis à jour avec succès, ID: {}", id);
        return productMapper.toResponseDTO(updatedProduct);
    }

    /**
     * Supprime un produit.
     *
     * Note: Dans un contexte réel, vérifier si le produit est dans des commandes.
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Suppression du produit avec l'ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produit", id); // doit renvoyer 404 (via ton handler)
        }

        boolean used;
        try {
            used = orderServiceClient.isProductUsedInAnyOrder(id);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Order service indisponible, impossible de vérifier si le produit est utilisé.");
        }

        if (used) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Suppression impossible : le produit est déjà utilisé dans au moins une commande.");
        }

        productRepository.deleteById(id);
        log.info("Produit supprimé avec succès, ID: {}", id);
    }

    /**
     * Recherche des produits par nom.
     */
    public List<ProductResponseDTO> searchProductsByName(String name) {
        log.info("Recherche de produits par nom: {}", name);

        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filtre les produits par catégorie.
     */
    public List<ProductResponseDTO> getProductsByCategory(String categoryName) {
        log.info("Récupération des produits de la catégorie: {}", categoryName);

        try {
            ProductCategory category = ProductCategory.valueOf(categoryName.toUpperCase());
            return productRepository.findByCategory(category)
                    .stream()
                    .map(productMapper::toResponseDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Catégorie invalide: " + categoryName +
                    ". Catégories valides: ELECTRONICS, BOOKS, FOOD, OTHER");
        }
    }

    /**
     * Récupère tous les produits disponibles (en stock).
     */
    public List<ProductResponseDTO> getAvailableProducts() {
        log.info("Récupération des produits disponibles");

        return productRepository.findByStockGreaterThan(0)
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour le stock d'un produit.
     */
    @Transactional
    public ProductResponseDTO updateStock(Long id, StockUpdateDTO stockUpdateDTO) {
        log.info("Mise à jour du stock du produit ID: {}, opération: {}, quantité: {}",
                id, stockUpdateDTO.getOperation(), stockUpdateDTO.getQuantity());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", id));

        int newStock;
        StockUpdateDTO.StockOperation operation = stockUpdateDTO.getOperation();
        if (operation == null) {
            operation = StockUpdateDTO.StockOperation.SET; // Par défaut
        }

        switch (operation) {
            case ADD:
                newStock = product.getStock() + stockUpdateDTO.getQuantity();
                break;
            case SUBTRACT:
                newStock = product.getStock() - stockUpdateDTO.getQuantity();
                if (newStock < 0) {
                    throw new InsufficientStockException(id,
                            stockUpdateDTO.getQuantity(), product.getStock());
                }
                break;
            case SET:
                newStock = stockUpdateDTO.getQuantity();
                if (newStock < 0) {
                    throw new IllegalArgumentException("Le stock ne peut pas être négatif");
                }
                break;
            default:
                throw new IllegalArgumentException("Opération de stock invalide");
        }

        product.setStock(newStock);
        Product updatedProduct = productRepository.save(product);

        log.info("Stock mis à jour avec succès. Nouveau stock: {}", newStock);
        return productMapper.toResponseDTO(updatedProduct);
    }

    /**
     * Incrémente le compteur de produits créés pour une catégorie donnée.
     */
    private void incrementCategoryCounter(ProductCategory category) {
        if (category == null) return;

        switch (category) {
            case ELECTRONICS:
                electronicsCounter.increment();
                break;
            case BOOKS:
                booksCounter.increment();
                break;
            case FOOD:
                foodCounter.increment();
                break;
            case OTHER:
                otherCounter.increment();
                break;
        }
    }

    /**
     * Compte le nombre de produits avec un stock bas (< 5).
     * Utilisé par le health check.
     */
    public long countLowStockProducts() {
        return productRepository.countByStockLessThan(5);
    }
}
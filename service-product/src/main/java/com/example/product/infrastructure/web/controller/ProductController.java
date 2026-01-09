package com.example.product.infrastructure.web.controller;

import com.example.product.application.dto.ProductRequestDTO;
import com.example.product.application.dto.ProductResponseDTO;
import com.example.product.application.dto.StockUpdateDTO;
import com.example.product.application.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des produits.
 *
 * Endpoints :
 * - GET    /api/v1/products              : Liste tous les produits
 * - GET    /api/v1/products/{id}         : Détail d'un produit
 * - POST   /api/v1/products              : Créer un produit
 * - PUT    /api/v1/products/{id}         : Modifier un produit
 * - DELETE /api/v1/products/{id}         : Supprimer un produit
 * - GET    /api/v1/products/search       : Recherche par nom
 * - GET    /api/v1/products/category/{c} : Filtrer par catégorie
 * - GET    /api/v1/products/available    : Produits en stock
 * - PATCH  /api/v1/products/{id}/stock   : Mettre à jour le stock
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "API de gestion du catalogue produits")
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/v1/products
     * Récupère la liste de tous les produits.
     */
    @Operation(summary = "Récupérer tous les produits",
            description = "Retourne la liste complète de tous les produits du catalogue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponseDTO.class)))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        log.info("GET /api/v1/products - Récupération de tous les produits");

        List<ProductResponseDTO> products = productService.getAllProducts();

        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/v1/products/{id}
     * Récupère un produit par son ID.
     */
    @Operation(summary = "Récupérer un produit par ID",
            description = "Retourne un produit spécifique basé sur son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produit trouvé",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé",
                    content = @Content)
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDTO> getProductById(
            @Parameter(description = "ID du produit", required = true)
            @PathVariable Long id) {

        log.info("GET /api/v1/products/{} - Récupération du produit", id);

        ProductResponseDTO product = productService.getProductById(id);

        return ResponseEntity.ok(product);
    }

    /**
     * POST /api/v1/products
     * Crée un nouveau produit.
     */
    @Operation(summary = "Créer un nouveau produit",
            description = "Crée un nouveau produit dans le catalogue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produit créé avec succès",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Le produit existe déjà",
                    content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Parameter(description = "Données du produit à créer", required = true)
            @Valid @RequestBody ProductRequestDTO productRequestDTO) {

        log.info("POST /api/v1/products - Création d'un produit: {}", productRequestDTO.getName());

        ProductResponseDTO createdProduct = productService.createProduct(productRequestDTO);

        // Best practice REST : retourner l'URI de la ressource créée
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdProduct);
    }

    /**
     * PUT /api/v1/products/{id}
     * Met à jour complètement un produit existant.
     */
    @Operation(summary = "Mettre à jour un produit",
            description = "Met à jour complètement les informations d'un produit existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produit mis à jour avec succès",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflit avec un produit existant",
                    content = @Content)
    })
    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @Parameter(description = "ID du produit", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nouvelles données du produit", required = true)
            @Valid @RequestBody ProductRequestDTO productRequestDTO) {

        log.info("PUT /api/v1/products/{} - Mise à jour du produit", id);

        ProductResponseDTO updatedProduct = productService.updateProduct(id, productRequestDTO);

        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * DELETE /api/v1/products/{id}
     * Supprime un produit.
     */
    @Operation(summary = "Supprimer un produit",
            description = "Supprime définitivement un produit du catalogue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produit supprimé avec succès",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID du produit", required = true)
            @PathVariable Long id) {

        log.info("DELETE /api/v1/products/{} - Suppression du produit", id);

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/products/search?name={name}
     * Recherche des produits par nom.
     */
    @Operation(summary = "Rechercher des produits par nom",
            description = "Recherche des produits dont le nom contient la chaîne spécifiée")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recherche effectuée avec succès",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponseDTO.class)))
    })
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(
            @Parameter(description = "Nom ou partie du nom à rechercher", required = true)
            @RequestParam String name) {

        log.info("GET /api/v1/products/search?name={} - Recherche de produits", name);

        List<ProductResponseDTO> products = productService.searchProductsByName(name);

        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/v1/products/category/{category}
     * Filtre les produits par catégorie.
     */
    @Operation(summary = "Filtrer les produits par catégorie",
            description = "Retourne tous les produits d'une catégorie spécifique (ELECTRONICS, BOOKS, FOOD, OTHER)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produits filtrés avec succès",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Catégorie invalide",
                    content = @Content)
    })
    @GetMapping(value = "/category/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(
            @Parameter(description = "Catégorie (ELECTRONICS, BOOKS, FOOD, OTHER)", required = true)
            @PathVariable String category) {

        log.info("GET /api/v1/products/category/{} - Filtrage par catégorie", category);

        List<ProductResponseDTO> products = productService.getProductsByCategory(category);

        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/v1/products/available
     * Récupère tous les produits disponibles (en stock).
     */
    @Operation(summary = "Récupérer les produits disponibles",
            description = "Retourne tous les produits actifs avec un stock > 0")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produits disponibles récupérés",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponseDTO.class)))
    })
    @GetMapping(value = "/available", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductResponseDTO>> getAvailableProducts() {
        log.info("GET /api/v1/products/available - Récupération des produits disponibles");

        List<ProductResponseDTO> products = productService.getAvailableProducts();

        return ResponseEntity.ok(products);
    }

    /**
     * PATCH /api/v1/products/{id}/stock
     * Met à jour le stock d'un produit.
     */
    @Operation(summary = "Mettre à jour le stock d'un produit",
            description = "Permet d'ajouter, retirer ou définir le stock d'un produit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock mis à jour avec succès",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Opération invalide ou stock insuffisant",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé",
                    content = @Content)
    })
    @PatchMapping(value = "/{id}/stock",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponseDTO> updateStock(
            @Parameter(description = "ID du produit", required = true)
            @PathVariable Long id,
            @Parameter(description = "Données de mise à jour du stock", required = true)
            @Valid @RequestBody StockUpdateDTO stockUpdateDTO) {

        log.info("PATCH /api/v1/products/{}/stock - Mise à jour du stock", id);

        ProductResponseDTO updatedProduct = productService.updateStock(id, stockUpdateDTO);

        return ResponseEntity.ok(updatedProduct);
    }
}
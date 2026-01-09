package com.example.product.application.mapper;

import com.example.product.application.dto.ProductRequestDTO;
import com.example.product.application.dto.ProductResponseDTO;
import com.example.product.domain.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre les entités Product et les DTOs.
 *
 * Pattern : Séparation de la logique de conversion.
 * Alternative : MapStruct pour génération automatique.
 */
@Component
public class ProductMapper {

    /**
     * Convertit un ProductRequestDTO en entité Product.
     *
     * @param dto Le DTO de requête
     * @return L'entité Product
     */
    public Product toEntity(ProductRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }

    /**
     * Convertit une entité Product en ProductResponseDTO.
     *
     * @param product L'entité Product
     * @return Le DTO de réponse
     */
    public ProductResponseDTO toResponseDTO(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * Met à jour une entité Product existante avec les données d'un DTO.
     *
     * @param product L'entité à mettre à jour
     * @param dto Le DTO contenant les nouvelles valeurs
     */
    public void updateEntityFromDTO(Product product, ProductRequestDTO dto) {
        if (product == null || dto == null) {
            return;
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());

        if (dto.getActive() != null) {
            product.setActive(dto.getActive());
        }
    }
}
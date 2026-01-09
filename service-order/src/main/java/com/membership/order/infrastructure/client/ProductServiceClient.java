package com.membership.order.infrastructure.client;

import com.membership.order.application.dto.ProductDTO;
import com.membership.order.application.dto.StockUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.product.url:http://localhost:8082}")
    private String productServiceUrl;


    public ProductDTO getProductById(Long productId) {
        try {
            log.debug("Récupération du produit ID: {} depuis le service Product", productId);
            
            String url = productServiceUrl + "/api/v1/products/" + productId;
            ProductDTO product = restTemplate.getForObject(url, ProductDTO.class);
            
            log.debug("Produit récupéré: {}", product != null ? product.getName() : "null");
            return product;
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Produit ID: {} non trouvé", productId);
            return null;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du produit ID: {}", productId, e);
            throw new RuntimeException("Service Product indisponible", e);
        }
    }


    public boolean isProductAvailable(Long productId, Integer quantity) {
        ProductDTO product = getProductById(productId);
        
        if (product == null) {
            log.warn("Produit ID: {} n'existe pas", productId);
            return false;
        }
        
        if (!Boolean.TRUE.equals(product.getActive())) {
            log.warn("Produit ID: {} n'est pas actif", productId);
            return false;
        }
        
        if (product.getStock() == null || product.getStock() < quantity) {
            log.warn("Produit ID: {} - Stock insuffisant. Disponible: {}, Demandé: {}", 
                    productId, product.getStock(), quantity);
            return false;
        }
        
        return true;
    }

    /**
     * Met à jour le stock d'un produit.
     * 
     * @param productId L'ID du produit
     * @param quantity La quantité (positive pour ajouter, négative pour retirer)
     * @param operation L'opération (ADD, SUBTRACT, SET)
     * @return true si la mise à jour a réussi
     */
    public boolean updateStock(Long productId, Integer quantity, String operation) {
        try {
            log.debug("Mise à jour du stock du produit ID: {} - Quantité: {}, Opération: {}",
                    productId, quantity, operation);

            String url = productServiceUrl + "/api/v1/products/" + productId + "/stock";

            StockUpdateDTO stockUpdate = StockUpdateDTO.builder()
                    .quantity(quantity)
                    .operation(operation)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<StockUpdateDTO> entity = new HttpEntity<>(stockUpdate, headers);


            restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.PATCH,
                    entity,
                    Void.class
            );

            log.debug("Stock du produit ID: {} mis à jour avec succès", productId);
            return true;

        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du stock du produit ID: {}", productId, e);
            return false;
        }
    }

    /**
     * Vérifie si le service Product est disponible.
     * 
     * @return true si le service répond
     */
    public boolean isServiceAvailable() {
        try {
            String url = productServiceUrl + "/actuator/health";
            restTemplate.getForObject(url, String.class);
            return true;
        } catch (Exception e) {
            log.warn("Service Product indisponible: {}", e.getMessage());
            return false;
        }
    }
}

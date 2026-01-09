package com.example.product.infrastructure.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OrderServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.order.url:http://localhost:8083}")
    private String orderServiceUrl;

    /**
     * Vérifie si un produit est déjà référencé dans au moins une commande.
     */
    public boolean isProductUsedInAnyOrder(Long productId) {
        try {
            String url = orderServiceUrl + "/api/v1/orders/exists/product/" + productId;
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (RestClientException ex) {
            // si ORDER est down, on bloque la suppression
            throw new IllegalStateException(
                    "Le service Order est indisponible, impossible de valider la suppression du produit.", ex);
        }
    }
}
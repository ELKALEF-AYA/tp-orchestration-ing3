package com.membership.order.infrastructure.health;

import com.membership.order.infrastructure.client.ProductServiceClient;
import com.membership.order.infrastructure.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Vérifie la santé des microservices dépendants (UserService, ProductService)
 * Endpoint: GET /actuate/health
 * Retourne: UP/DOWN avec détails de chaque service
 */
@Component
@RequiredArgsConstructor
public class ExternalServicesHealthIndicator implements HealthIndicator {

    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    /**
     * Vérifie si UserService et ProductService sont disponibles
     *
     * Cas:
     * - Les 2 UP → Health.up() + détails
     * - Les 2 DOWN → Health.down() + CRITICAL
     * - 1 UP, 1 DOWN → Health.down() + WARNING
     * - Exception → Health.down() + erreur
     *
     * @return État de santé avec détails
     */
    @Override
    public Health health() {
        try {
            boolean userServiceUp = userServiceClient.isServiceAvailable();
            boolean productServiceUp = productServiceClient.isServiceAvailable();

            // Les 2 services disponibles
            if (userServiceUp && productServiceUp) {
                return Health.up()
                        .withDetail("userService", "UP")
                        .withDetail("productService", "UP")
                        .withDetail("status", "Tous les services externes sont disponibles")
                        .build();
            }
            // Les 2 services indisponibles
            else if (!userServiceUp && !productServiceUp) {
                return Health.down()
                        .withDetail("userService", "DOWN")
                        .withDetail("productService", "DOWN")
                        .withDetail("status", "CRITICAL - Tous les services externes sont indisponibles")
                        .build();
            }
            // 1 service UP, 1 service DOWN
            else {
                return Health.down()
                        .withDetail("userService", userServiceUp ? "UP" : "DOWN")
                        .withDetail("productService", productServiceUp ? "UP" : "DOWN")
                        .withDetail("status", "WARNING - Un service externe est indisponible")
                        .build();
            }
        }
        // Erreur lors de la vérification
        catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "Erreur lors de la vérification des services externes")
                    .build();
        }
    }
}
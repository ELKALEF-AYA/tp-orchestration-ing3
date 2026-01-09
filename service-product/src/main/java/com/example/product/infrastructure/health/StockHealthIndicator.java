package com.example.product.infrastructure.health;

import com.example.product.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health Indicator personnalisé pour vérifier le stock des produits.
 *
 * Vérifie le nombre de produits avec un stock bas (< 5).
 * - UP : Si moins de 10 produits ont un stock bas
 * - DOWN : Si 10 produits ou plus ont un stock bas (alerte critique)
 *
 * Accessible via : GET /actuator/health
 */
@Component
@RequiredArgsConstructor
public class StockHealthIndicator implements HealthIndicator {

    private final ProductService productService;

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int CRITICAL_LOW_STOCK_COUNT = 10;

    @Override
    public Health health() {
        try {
            long lowStockCount = productService.countLowStockProducts();

            if (lowStockCount >= CRITICAL_LOW_STOCK_COUNT) {
                return Health.down()
                        .withDetail("lowStockProducts", lowStockCount)
                        .withDetail("threshold", LOW_STOCK_THRESHOLD)
                        .withDetail("status", "CRITICAL - Réapprovisionnement urgent nécessaire")
                        .build();
            } else if (lowStockCount > 0) {
                return Health.up()
                        .withDetail("lowStockProducts", lowStockCount)
                        .withDetail("threshold", LOW_STOCK_THRESHOLD)
                        .withDetail("status", "WARNING - Certains produits ont un stock bas")
                        .build();
            } else {
                return Health.up()
                        .withDetail("lowStockProducts", 0)
                        .withDetail("status", "OK - Tous les stocks sont suffisants")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
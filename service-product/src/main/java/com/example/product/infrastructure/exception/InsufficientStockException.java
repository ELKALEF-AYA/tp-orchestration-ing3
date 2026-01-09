package com.example.product.infrastructure.exception;

/**
 * Exception levée lorsque le stock est insuffisant pour une opération.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(Long productId, Integer requested, Integer available) {
        super(String.format(
                "Stock insuffisant pour le produit ID %d. Demandé: %d, Disponible: %d",
                productId, requested, available
        ));
    }
}
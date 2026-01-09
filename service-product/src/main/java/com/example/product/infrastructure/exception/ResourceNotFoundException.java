package com.example.product.infrastructure.exception;

/**
 * Exception levée lorsqu'une ressource demandée n'existe pas.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s avec l'ID %d non trouvé", resourceName, id));
    }
}
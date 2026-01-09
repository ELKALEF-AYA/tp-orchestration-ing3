package com.example.product.infrastructure.exception;

/**
 * Exception levée lorsqu'on tente de créer une ressource qui existe déjà.
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String resourceName, String field, String value) {
        super(String.format("%s avec %s '%s' existe déjà", resourceName, field, value));
    }
}
package com.membership.order.infrastructure.exception;


public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s avec l'ID %d non trouvé", resourceName, id));
    }
}

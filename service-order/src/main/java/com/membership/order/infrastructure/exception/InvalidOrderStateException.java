package com.membership.order.infrastructure.exception;


public class InvalidOrderStateException extends RuntimeException {
    
    public InvalidOrderStateException(String message) {
        super(message);
    }

    public InvalidOrderStateException(Long orderId, String currentStatus, String operation) {
        super(String.format("Impossible de %s la commande ID %d avec le statut %s", 
                operation, orderId, currentStatus));
    }
}

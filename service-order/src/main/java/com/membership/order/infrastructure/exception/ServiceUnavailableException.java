package com.membership.order.infrastructure.exception;

/**
 * Exception levée quand un microservice dépendant est indisponible
 * Cas d'usage: UserService ou ProductService ne répond pas
 * Code HTTP retourné: 503 Service Unavailable
 */
public class ServiceUnavailableException extends RuntimeException {

    /**
     * Constructeur simple avec message personnalisé
     *
     * @param message Description de l'erreur
     */
    public ServiceUnavailableException(String message) {
        super(message);
    }

    /**
     * Constructeur avec nom du service et cause (exception originale)
     * Formatte automatiquement le message: "Le service X est actuellement indisponible"
     *
     * @param serviceName Nom du service indisponible (ex: "UserService", "ProductService")
     * @param cause Exception qui a provoqué l'indisponibilité
     */
    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super(String.format("Le service %s est actuellement indisponible", serviceName), cause);
    }
}
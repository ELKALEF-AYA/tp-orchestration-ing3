package com.example.product.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Structure de réponse d'erreur standardisée.
 *
 * Respecte les bonnes pratiques REST en fournissant :
 * - Un timestamp pour le debugging
 * - Un statut HTTP
 * - Un message clair
 * - Le chemin de la requête
 * - Les erreurs de validation détaillées
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> details;

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
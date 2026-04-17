package com.sigma.sahur.studio.exception;

/**
 * Exception levée lorsqu'une ressource demandée est introuvable en base de données.
 * Traduite en réponse HTTP 404 Not Found par le {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Crée une exception de ressource introuvable avec le message fourni.
     *
     * @param message description de la ressource manquante
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

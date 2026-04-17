package com.sigma.sahur.studio.exception;

/**
 * Exception levée lors d'un conflit de données (doublon d'e-mail, chevauchement de créneau, etc.).
 * Traduite en réponse HTTP 409 Conflict par le {@link GlobalExceptionHandler}.
 */
public class ConflictException extends RuntimeException {

    /**
     * Crée une exception de conflit avec le message fourni.
     *
     * @param message description du conflit détecté
     */
    public ConflictException(String message) {
        super(message);
    }
}

package com.sigma.sahur.studio.exception;

/**
 * Exception levée lors d'une violation de règle métier.
 * Traduite en réponse HTTP 400 Bad Request par le {@link GlobalExceptionHandler}.
 */
public class BusinessException extends RuntimeException {

    /**
     * Crée une exception métier avec le message d'erreur fourni.
     *
     * @param message description de la règle métier violée
     */
    public BusinessException(String message) {
        super(message);
    }
}

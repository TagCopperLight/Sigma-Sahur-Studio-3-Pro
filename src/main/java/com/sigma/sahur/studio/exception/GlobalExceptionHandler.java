package com.sigma.sahur.studio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions REST.
 * Intercepte les exceptions applicatives et les convertit en réponses JSON structurées
 * avec le code HTTP approprié.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les ressources introuvables.
     *
     * @param ex exception levée lorsqu'une entité n'existe pas en base
     * @return réponse 404 avec le message d'erreur
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Gère les violations de règles métier.
     *
     * @param ex exception levée lors d'une règle métier non respectée
     * @return réponse 400 avec le message d'erreur
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Gère les conflits de données (doublon, chevauchement de créneau, etc.).
     *
     * @param ex exception levée lors d'un conflit détecté
     * @return réponse 409 avec le message d'erreur
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Gère les erreurs de validation des champs de la requête ({@code @Valid}).
     * Concatène tous les messages de validation des champs en erreur.
     *
     * @param ex exception levée par Spring lors de la validation du corps de la requête
     * @return réponse 400 avec la liste des messages de validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Gère toutes les exceptions non anticipées.
     *
     * @param ex exception non gérée par les autres handlers
     * @return réponse 500 avec un message générique
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue");
    }

    /**
     * Construit le corps de réponse JSON standardisé.
     *
     * @param status code HTTP à retourner
     * @param message message d'erreur lisible
     * @return réponse HTTP avec le corps JSON contenant status, error, message et timestamp
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}

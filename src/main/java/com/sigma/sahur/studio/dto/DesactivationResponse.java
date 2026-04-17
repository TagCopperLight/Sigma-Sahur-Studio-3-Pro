package com.sigma.sahur.studio.dto;

import lombok.Getter;

import java.util.List;

/**
 * Réponse à une tentative de désactivation d'un photographe.
 * Si {@code requiresConfirmation} est {@code true}, le frontend doit afficher une modale
 * listant les séances futures impactées, puis relancer la requête avec {@code ?force=true}
 * pour confirmer et annuler ces séances.
 */
@Getter
public class DesactivationResponse {

    /**
     * Indique si une confirmation de l'utilisateur est nécessaire avant de procéder.
     * {@code true} lorsque des séances futures actives seront annulées par la désactivation.
     */
    private final boolean requiresConfirmation;

    /** Message descriptif destiné à l'interface utilisateur. */
    private final String message;

    /** Liste des séances futures qui seront annulées si la désactivation est confirmée. */
    private final List<SeanceResponse> seancesFuturesImpactees;

    /** État du photographe après l'opération (ou avant si confirmation requise). */
    private final PhotographeResponse photographe;

    /**
     * Construit la réponse de désactivation.
     *
     * @param requiresConfirmation {@code true} si une confirmation est attendue
     * @param message              message destiné au frontend
     * @param seancesFuturesImpactees séances futures qui seraient annulées
     * @param photographe          représentation du photographe concerné
     */
    public DesactivationResponse(boolean requiresConfirmation, String message,
                                  List<SeanceResponse> seancesFuturesImpactees,
                                  PhotographeResponse photographe) {
        this.requiresConfirmation = requiresConfirmation;
        this.message = message;
        this.seancesFuturesImpactees = seancesFuturesImpactees;
        this.photographe = photographe;
    }
}

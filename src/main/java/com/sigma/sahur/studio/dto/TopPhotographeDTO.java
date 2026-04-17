package com.sigma.sahur.studio.dto;

import lombok.Getter;

/**
 * DTO utilisé pour les classements statistiques des photographes.
 * La signification du champ {@code valeur} dépend du contexte :
 * nombre de séances terminées ou durée cumulée en minutes.
 */
@Getter
public class TopPhotographeDTO {

    /** Identifiant du photographe classé. */
    private final Long photographeId;

    /** Nom du photographe classé. */
    private final String photographeNom;

    /**
     * Valeur du critère de classement.
     * Peut représenter un nombre de séances ou une durée cumulée en minutes
     * selon la requête statistique utilisée.
     */
    private final Long valeur;

    /**
     * Construit le DTO avec les données du classement.
     *
     * @param photographeId  identifiant du photographe
     * @param photographeNom nom du photographe
     * @param valeur         valeur du critère de classement
     */
    public TopPhotographeDTO(Long photographeId, String photographeNom, Long valeur) {
        this.photographeId = photographeId;
        this.photographeNom = photographeNom;
        this.valeur = valeur;
    }
}

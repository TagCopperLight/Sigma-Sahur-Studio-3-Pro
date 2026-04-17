package com.sigma.sahur.studio.dto;

import com.sigma.sahur.studio.model.Client;
import lombok.Getter;

/**
 * Représentation en lecture seule d'un client, retournée par l'API.
 */
@Getter
public class ClientResponse {

    /** Identifiant unique du client. */
    private final Long id;

    /** Nom de famille ou dénomination abrégée. */
    private final String nom;

    /** Prénom du client particulier ; {@code null} pour les entreprises. */
    private final String prenom;

    /** Raison sociale de l'entreprise ; {@code null} pour les particuliers. */
    private final String raisonSociale;

    /** Adresse postale complète. */
    private final String adresse;

    /** Adresse e-mail de contact. */
    private final String email;

    /** Nom d'affichage calculé (raison sociale ou "Prénom Nom"). */
    private final String nomComplet;

    /**
     * Construit le DTO à partir de l'entité {@link Client}.
     *
     * @param c entité client source
     */
    public ClientResponse(Client c) {
        this.id = c.getId();
        this.nom = c.getNom();
        this.prenom = c.getPrenom();
        this.raisonSociale = c.getRaisonSociale();
        this.adresse = c.getAdresse();
        this.email = c.getEmail();
        this.nomComplet = c.getNomComplet();
    }
}
